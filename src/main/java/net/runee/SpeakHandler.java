package net.runee;

import jouvieje.bass.Bass;
import jouvieje.bass.defines.BASS_ACTIVE;
import jouvieje.bass.defines.BASS_ERROR;
import jouvieje.bass.defines.BASS_RECORD;
import jouvieje.bass.defines.BASS_STREAM;
import jouvieje.bass.structures.HRECORD;
import jouvieje.bass.utils.Pointer;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.runee.errors.BassException;
import net.runee.misc.MemoryQueue;
import net.runee.misc.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static jouvieje.bass.defines.BASS_ACTIVE.*;
import static jouvieje.bass.defines.BASS_ERROR.BASS_ERROR_HANDLE;

public class SpeakHandler implements AudioSendHandler, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(SpeakHandler.class);
    public static final int FRAME_MILLIS = 20;
    public static final int MAX_LAG = 200;
    private static List<SpeakHandler> activeHandlers = new ArrayList<>();

    private final Object memoryQueueLock = new Object();
    private int recordingDevice;
    private HRECORD recordingStream;
    private MemoryQueue memoryQueue;
    private byte[] buffer;

    public SpeakHandler() {
        this.recordingDevice = -1;
        this.buffer = new byte[INPUT_FORMAT.getChannels() * (int) (INPUT_FORMAT.getSampleRate() * (FRAME_MILLIS / 1000f)) * (INPUT_FORMAT.getSampleSizeInBits() / 8)];
    }

    public void openRecordingDevice(int recordingDevice, boolean setPlaying) throws BassException {
        Utils.closeQuiet(this);

        this.recordingDevice = recordingDevice;
        memoryQueue = new MemoryQueue();

        HRECORD recordingStream = null;
        for (SpeakHandler handler : activeHandlers) {
            if (handler.recordingDevice == recordingDevice) {
                recordingStream = handler.recordingStream;
                break;
            }
        }

        if (recordingStream != null) {
            this.recordingStream = recordingStream;
        } else {
            try {
                if (!Bass.BASS_RecordInit(recordingDevice)) {
                    Utils.checkBassError();
                }
                int flags = BASS_STREAM.BASS_STREAM_AUTOFREE;
                if(!setPlaying) {
                    flags |= BASS_RECORD.BASS_RECORD_PAUSE;
                }
                this.recordingStream = Bass.BASS_RecordStart((int) INPUT_FORMAT.getSampleRate(), INPUT_FORMAT.getChannels(), flags, SpeakHandler::RECORDPROC, null);
                Utils.checkBassError();
            } catch (BassException ex) {
                Utils.closeQuiet(this);
                throw ex;
            }
        }

        activeHandlers.add(this);
    }

    public void setPlaying(boolean playing) throws BassException {
        if(recordingStream == null) {
            throw new IllegalStateException("No open stream, call openRecordingDevice first");
        }
        switch (Bass.BASS_ChannelIsActive(this.recordingStream.asInt())) {
            case BASS_ACTIVE_PLAYING:
            case BASS_ACTIVE_STALLED:
                if(!playing) {
                    Bass.BASS_ChannelPause(this.recordingStream.asInt());
                }
                break;
            case BASS_ACTIVE_PAUSED:
            case BASS_ACTIVE_STOPPED:
                if(playing) {
                    Bass.BASS_ChannelPlay(this.recordingStream.asInt(), false);
                }
                break;
            default:
                throw new IndexOutOfBoundsException();
        }
        try {
            Utils.checkBassError();
        } catch(BassException ex) {
            if(ex.getError() == BASS_ERROR_HANDLE) {
                // TODO invalid handle:
                // not sure why, but after moving around guilds/channels a few times (with follow-audio on), the stream handle randomly becomes invalid (probably a synchronization issue)
                // as a workaround, let's open a new stream
                logger.warn("Workaround: Restarting recording stream", ex);
                openRecordingDevice(recordingDevice, playing);
            }
        }
    }

    public float getLag() {
        return (memoryQueue.size() / (float) buffer.length) * FRAME_MILLIS;
    }

    private static boolean RECORDPROC(HRECORD handle, ByteBuffer buffer, int length, Pointer user) {
        List<SpeakHandler> handlers = getActiveHandlers(handle);
        byte[] sampleBuffer = new byte[2];
        int numSamplesToWrite = length / sampleBuffer.length;
        for (int s = 0; s < numSamplesToWrite; s++) {
            short sample = buffer.getShort();
            sampleBuffer[0] = (byte) ((sample >> 8) & 0xff);
            sampleBuffer[1] = (byte) (sample & 0xff);
            for (SpeakHandler handler : handlers) {
                synchronized (handler.memoryQueueLock) {
                    handler.memoryQueue.enqueue(sampleBuffer, 0, sampleBuffer.length);
                }
            }
        }
        return true;
    }

    private static List<SpeakHandler> getActiveHandlers(HRECORD handle) {
        List<SpeakHandler> matchingHandlers = new ArrayList<>();
        for (SpeakHandler handler : new ArrayList<>(activeHandlers)) {
            if (handle.equals(handler.recordingStream)) {
                matchingHandlers.add(handler);
            }
        }
        return matchingHandlers;
    }

    @Override
    public boolean canProvide() {
        float lag = getLag();
        if (lag >= MAX_LAG) {
            synchronized (memoryQueueLock) {
                memoryQueue.clear();
            }
            logger.warn("SpeakHandler is " + (int)lag + " ms behind! Clearing queue...");
        }
        return memoryQueue.size() >= buffer.length;
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        int numBytesRead;
        synchronized (memoryQueueLock) {
            numBytesRead = memoryQueue.dequeue(buffer, 0, buffer.length);
        }
        return ByteBuffer.wrap(buffer, 0, numBytesRead);
    }

    @Override
    public boolean isOpus() {
        return false;
    }

    @Override
    public void close() throws IOException {
        activeHandlers.remove(this);
        if (recordingStream != null) {
            Bass.BASS_ChannelStop(recordingStream.asInt());
        }
        if (recordingDevice >= 0) {
            Bass.BASS_RecordSetDevice(recordingDevice);
            Bass.BASS_RecordFree();
        }
        memoryQueue = null;
        recordingStream = null;
        // TODO invalid handle:
        // not sure why, but after moving around guilds/channels a few times (with follow-audio on), the stream handle becomes invalid  (probably a synchronization issue)
        // as a workaround, keep the recording device last used
        //recordingDevice = -1;
        Utils.checkBassError();
    }
}