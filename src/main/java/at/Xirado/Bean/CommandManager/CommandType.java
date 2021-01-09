package at.Xirado.Bean.CommandManager;

public enum CommandType
{

	MODERATION("\uD83D\uDEE1", ""),
	ADMIN("\uD83C\uDF9B", ""),
	FUN("\uD83C\uDFB1", ""),
	MUSIC("\uD83C\uDFB5", "\n**Note:** To get DJ privileges, you either need to have a role called \"DJ\", have Manage Channel permissions or be alone in the channel with the bot"),
	UTILITIES("⚒", ""),
	BEAN("<:Bean:776425797903974431>", ""),
	EXCLUDED("", ""),
	GAMES("\uD83E\uDD47", "");

	private final String emoji;
	private final String notes;
	CommandType(String emoji, String notes)
	{
		this.emoji = emoji;
		this.notes = notes;
	}

	public String getEmoji() {
		return emoji;
	}
	public String getNotes() { return notes;}
}
