package at.xirado.bean.language;

import net.dv8tion.jda.api.entities.Guild;

import java.util.Locale;

public class ServerLanguage
{

    public static Locale getLanguage(Guild guild)
    {
        return guild.getLocale();
    }

}
