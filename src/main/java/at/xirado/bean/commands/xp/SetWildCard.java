package at.xirado.bean.commands.xp;

import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SetWildCard extends Command
{
    public SetWildCard(String name, String description, String usage)
    {
        super(name, description, usage);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {

    }
}
