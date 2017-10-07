using Discord.Audio;

namespace DASB {
    public class VoiceSet {
        public IAudioClient audioClient;
        public AudioOutStream speakStream;
        public readonly object speakStream_writeLock = new object();
    }
}
