package at.xirado.bean.commands;

import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.commandutil.CommandFlag;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class LanguageTest extends Command
{
    public LanguageTest()
    {
        super("test", "langtest", "test");
        setCommandFlags(CommandFlag.PRIVATE_COMMAND, CommandFlag.DEVELOPER_ONLY);
        addAllowedGuilds(815597207617142814L);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        context.reply("SUPERPOWER!!!!");
    }
}
