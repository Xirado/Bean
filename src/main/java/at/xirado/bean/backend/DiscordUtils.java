package at.xirado.bean.backend;

public class DiscordUtils {
    public static final String ICON_URL = "https://cdn.discordapp.com/icons/%s/%s.%s";


    public static String getIconUrl(String guildId, String iconHash) {
        return iconHash == null ? null : String.format(ICON_URL, guildId, iconHash, iconHash.startsWith("a_") ? "gif" : "png");
    }
}
