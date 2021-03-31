package net.runee.misc.discord;

public enum CommandCategory {
    BOT,
    BOT_USER,
    SETTINGS;

    private String displayName;

    CommandCategory() {
        this(null);
    }

    CommandCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        if(displayName != null) {
            return displayName;
        }

        String[] words = name().replace("_", " ").split(" ", -1);
        for (int i = 0; i < words.length; i++) {
            final String word = words[i];
            words[i] = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
        }
        return String.join(" ", words);
    }
}
