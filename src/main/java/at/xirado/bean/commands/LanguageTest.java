package at.xirado.bean.commands;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.translation.ServerLanguage;
import net.dv8tion.jda.api.JDA;

import java.util.Arrays;
import java.util.Locale;

public class LanguageTest extends Command
{
    public LanguageTest(JDA jda)
    {
        super(jda);
        this.invoke = "language";
        this.global = false;
        this.enabledGuilds = Arrays.asList(713469621532885002L);
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        Locale language = ServerLanguage.getLanguage(event.getGuild());
        event.reply("Server-Locale: "+language.getDisplayLanguage(language));
    }
}
