package at.xirado.bean.command;

public enum CommandCategory
{

    MODERATION("Moderation", "\uD83D\uDEE1", ""),
    ADMIN("Admin", "\uD83C\uDF9B", ""),
    FUN("Fun", "\uD83C\uDFB1", ""),
    MUSIC("Music", "\uD83C\uDFB5", ""),
    UTILITIES("Utilities", "⚒", ""),
    BEAN("Bean", "<:Bean:776425797903974431>", ""),
    NONE("None", "", ""),
    GAMES("Games", "\uD83E\uDD47", "");

    private final String friendlyName;
    private final String emoji;
    private final String notes;

    CommandCategory(String friendlyName, String emoji, String notes)
    {
        this.friendlyName = friendlyName;
        this.emoji = emoji;
        this.notes = notes;
    }

    public String getFriendlyName()
    {
        return friendlyName;
    }

    public String getEmoji()
    {
        return emoji;
    }

    public String getNotes()
    {
        return notes;
    }
}
