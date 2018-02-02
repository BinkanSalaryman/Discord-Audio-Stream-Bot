using Discord.Audio;
using System.Collections.Concurrent;

namespace DASB {
    public class VoiceSet {
        public IAudioClient audioClient;
        public AudioOutStream speakStream;
        public ConcurrentDictionary<ulong, AudioInStream> listenStreams = new ConcurrentDictionary<ulong, AudioInStream>();
    }
}
