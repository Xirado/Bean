package at.xirado.bean.commandmanager;

import at.xirado.bean.translation.I18n;
import net.dv8tion.jda.api.entities.Guild;

public enum CommandType
{

	MODERATION("\uD83D\uDEE1", ""),
	ADMIN("\uD83C\uDF9B", ""),
	FUN("\uD83C\uDFB1", ""),
	MUSIC("\uD83C\uDFB5", I18n.getForLanguage("en_US").get("commands.dj_privileges_info")),
	UTILITIES("⚒", ""),
	BEAN("<:Bean:776425797903974431>", ""),
	EXCLUDED("", ""),
	GAMES("\uD83E\uDD47", "");

	private final String emoji;
	private final String notes;
	private Guild g = null;
	CommandType(String emoji, String notes)
	{
		this.emoji = emoji;
		this.notes = notes;
	}

	public void setGuild(Guild g)
	{
		this.g = g;
	}

	public String getEmoji() {
		return emoji;
	}
	public String getNotes() { return notes;}
}
