namespace DASB {
    public enum CommandFeedback {
        /// <summary>
        /// React with check sign
        /// </summary>
        Default,
        /// <summary>
        /// Don't add anything to the command execution
        /// </summary>
        Handled,
        /// <summary>
        /// React with a warning sign
        /// </summary>
        Warning,
        /// <summary>
        /// React with no entry sign
        /// </summary>
        NoEntry,
        /// <summary>
        /// Request a guild context
        /// </summary>
        GuildContextRequired,
    }
}
