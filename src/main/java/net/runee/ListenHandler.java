package net.runee;

import jouvieje.bass.Bass;
import jouvieje.bass.structures.HSTREAM;
import jouvieje.bass.utils.Pointer;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.runee.errors.BassException;
import net.runee.misc.MemoryQueue;
import net.runee.misc.Utils;
import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static net.dv8tion.jda.api.audio.AudioSendHandler.INPUT_FORMAT;

public class ListenHandler implements AudioReceiveHandler, Closeable {
    public static final int MAX_LAG = 200;
    public static final int PLAYBACK_FLAGS = 0; //BASS_DEVICE.BASS_DEVICE_3D;
    private static List<ListenHandler> activeHandlers = new ArrayList<>();

    private final Object memoryQueueLock = new Object();
    private int playbackDevice;
    private HSTREAM playbackStream;
    private MemoryQueue memoryQueue;
    private boolean closed;

    public ListenHandler() {
        this.playbackDevice = -1;
    }

    public void openPlaybackDevice(int playbackDevice) throws BassException {
        Utils.closeQuiet(this);

        this.playbackDevice = playbackDevice;
        memoryQueue = new MemoryQueue();

        HSTREAM playbackStream = null;
        for (ListenHandler handler : activeHandlers) {
            if (!handler.closed && handler.playbackDevice == playbackDevice) {
                playbackStream = handler.playbackStream;
                break;
            }
        }

        if (playbackStream != null) {
            this.playbackStream = playbackStream;
        } else {
            try {
                if (!Bass.BASS_Init(playbackDevice, (int) OUTPUT_FORMAT.getSampleRate(), PLAYBACK_FLAGS, null, null)) {
                    Utils.checkBassError();
                }
                this.playbackStream = Bass.BASS_StreamCreate((int) OUTPUT_FORMAT.getSampleRate(), OUTPUT_FORMAT.getChannels(), PLAYBACK_FLAGS, ListenHandler::STREAMPROC, null);
                Utils.checkBassError();
                Bass.BASS_ChannelPlay(this.playbackStream.asInt(), false);
            } catch (BassException ex) {
                Utils.closeQuiet(this);
                throw ex;
            }
        }

        closed = false;
        activeHandlers.add(this);
    }

    @Override
    public boolean canReceiveCombined() {
        return !closed;
    }

    @Override
    public void handleCombinedAudio(@Nonnull CombinedAudio combinedAudio) {
        byte[] data = combinedAudio.getAudioData(1);
        synchronized (memoryQueueLock) {
            memoryQueue.enqueue(data, 0, data.length);
        }
    }

    private static int STREAMPROC(HSTREAM handle, ByteBuffer buffer, int length, Pointer user) {
        List<ListenHandler> handlers = getActiveHandlers(handle);

        // FIXME distorted - why??
        // TODO check if mixing is done right

        int bytesPerSample = OUTPUT_FORMAT.getSampleSizeInBits() / 8;

        int maxSampleCount = 0; // biggest sample count in handlers
        for (ListenHandler handler : handlers) {
            maxSampleCount = Math.max(maxSampleCount, handler.memoryQueue.size() / bytesPerSample);
        }
        int numSamplesRead = length / bytesPerSample; // amount of samples to read

        byte[] sampleBuffer = new byte[bytesPerSample];
        for (int s = 0; s < numSamplesRead; s++) {
            int mixedSample = 0;
            for (ListenHandler handler : handlers) {
                int sample;
                synchronized (handler.memoryQueueLock) {
                    int sampleCount = handler.memoryQueue.size() / bytesPerSample;
                    if (s == maxSampleCount - sampleCount) {
                        handler.memoryQueue.dequeue(sampleBuffer, 0, sampleBuffer.length);
                        sample = sampleBuffer[0] << 8 | sampleBuffer[1];
                    } else {
                        sample = 0;
                    }
                }
                mixedSample += sample;
            }
            if (!handlers.isEmpty()) {
                mixedSample /= handlers.size();
            }
            mixedSample = Math.max(Short.MIN_VALUE, Math.min(mixedSample, Short.MAX_VALUE));
            buffer.putShort((short) mixedSample);
        }

        for (ListenHandler handler : handlers) {
            float lag = handler.memoryQueue.size() / (INPUT_FORMAT.getChannels() * INPUT_FORMAT.getSampleRate() * (1f / 1000f) * (INPUT_FORMAT.getSampleSizeInBits() / 8));
            if (lag >= MAX_LAG) {
                synchronized (handler.memoryQueueLock) {
                    handler.memoryQueue.clear();
                }
                System.out.println("WARNING: ListenHandler is " + lag + " ms behind! Clearing queue...");
            }
        }

        return numSamplesRead * bytesPerSample;
    }

    private static List<ListenHandler> getActiveHandlers(HSTREAM handle) {
        List<ListenHandler> matchingHandlers = new ArrayList<>();
        for (ListenHandler handler : new ArrayList<>(activeHandlers)) {
            if (handle.equals(handler.playbackStream)) {
                matchingHandlers.add(handler);
            }
        }
        return matchingHandlers;
    }

    @Override
    public void close() throws IOException {
        if (playbackStream != null) {
            Bass.BASS_ChannelStop(playbackStream.asInt());
        }
        if (playbackDevice >= 0) {
            Bass.BASS_SetDevice(playbackDevice);
            Bass.BASS_Free();
            Utils.checkBassError();
        }
        memoryQueue = null;
        //buffer = null;
        playbackStream = null;
        playbackDevice = -1;
    }
}