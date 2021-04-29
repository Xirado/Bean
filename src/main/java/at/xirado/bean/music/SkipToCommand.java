// 
// Decompiled by Procyon v0.5.36
// 

package at.xirado.bean.music;

import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SkipToCommand extends Command
{

    public SkipToCommand()
    {
        super("skipto", "Skips to another song in the queue", "skipto [index]");
        setCommandCategory(CommandCategory.MUSIC);

    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        if(!context.isDJ())
        {
            context.replyError("You need to be a DJ to do this!");
            return;
        }
        int index;
        try {
            index = Integer.parseInt(context.getArguments().toString(0));
        }
        catch (NumberFormatException e) {
            context.replyError(" `" + context.getArguments().toString(0) + "` is not a valid integer!");
            return;
        }
        final AudioHandler handler = ResultHandler.getHandler(event.getGuild());
        if (index < 1 || index > handler.getQueue().size()) {
            context.replyError("Position must be a valid integer between 1 and " + handler.getQueue().size() + "!");
            return;
        }
        handler.getQueue().skip(index - 1);
        context.replySuccess("Skipped to **" + handler.getQueue().get(0).getTrack().getInfo().title + "**");
        handler.getPlayer().stopTrack();
    }
}
