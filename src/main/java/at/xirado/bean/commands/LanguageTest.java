package at.xirado.bean.commands;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;

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
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
    }
}
