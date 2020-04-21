package net.runee.errors;

import java.io.IOException;

import static jouvieje.bass.defines.BASS_ERROR.*;

public class BassException extends IOException {
    private int error;

    public BassException(int error) {
        this.error = error;
    }

    public int getError() {
        return error;
    }

    @Override
    public String getMessage() {
        switch (error) {
            case BASS_OK:
                return "All is OK";
            case BASS_ERROR_MEM:
                return "Memory error";
            case BASS_ERROR_FILEOPEN:
                return "Can't open the file";
            case BASS_ERROR_DRIVER:
                return "Can't find a free/valid driver";
            case BASS_ERROR_BUFLOST:
                return "The sample buffer was lost";
            case BASS_ERROR_HANDLE:
                return "Invalid handle";
            case BASS_ERROR_FORMAT:
                return "Unsupported sample format";
            case BASS_ERROR_POSITION:
                return "Invalid playback position";
            case BASS_ERROR_INIT:
                return "BASS_Init has not been successfully called";
            case BASS_ERROR_START:
                return "BASS_Start has not been successfully called";
            case BASS_ERROR_ALREADY:
                return "Already initialized/paused/whatever";
            case BASS_ERROR_NOCHAN:
                return "Can't get a free channel";
            case BASS_ERROR_ILLTYPE:
                return "An illegal type was specified";
            case BASS_ERROR_ILLPARAM:
                return "An illegal parameter was specified";
            case BASS_ERROR_NO3D:
                return "No 3D support";
            case BASS_ERROR_NOEAX:
                return "No EAX support";
            case BASS_ERROR_DEVICE:
                return "Illegal device number";
            case BASS_ERROR_NOPLAY:
                return "Not playing";
            case BASS_ERROR_FREQ:
                return "Illegal sample rate";
            case BASS_ERROR_NOTFILE:
                return "The stream is not a file stream";
            case BASS_ERROR_NOHW:
                return "No hardware voices available";
            case BASS_ERROR_EMPTY:
                return "The MOD music has no sequence data";
            case BASS_ERROR_NONET:
                return "No internet connection could be opened";
            case BASS_ERROR_CREATE:
                return "Couldn't create the file";
            case BASS_ERROR_NOFX:
                return "Effects are not available";
            case BASS_ERROR_NOTAVAIL:
                return "Requested data is not available";
            case BASS_ERROR_DECODE:
                return "The channel is a 'decoding channel'";
            case BASS_ERROR_DX:
                return "A sufficient DirectX version is not installed";
            case BASS_ERROR_TIMEOUT:
                return "Connection timedout";
            case BASS_ERROR_FILEFORM:
                return "Unsupported file format";
            case BASS_ERROR_SPEAKER:
                return "Unavailable speaker";
            case BASS_ERROR_VERSION:
                return "Invalid BASS version";
            case BASS_ERROR_CODEC:
                return "Codec is not available/supported";
            case BASS_ERROR_ENDED:
                return "The channel/file has ended";
            case BASS_ERROR_BUSY:
                return "The device is busy";
            case BASS_ERROR_UNKNOWN:
                return "Some mystery error";
            case BASS_ERROR_ACM_CANCEL:
                return "BassEnc: ACM codec selection cancelled";
            case BASS_ERROR_CAST_DENIED:
                return "BassEnc: Access denied (invalid password)";
            default:
                return "Bass error " + error;
        }
    }
}
