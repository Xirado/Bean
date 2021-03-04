// 
// Decompiled by Procyon v0.5.36
// 

package at.xirado.bean.music;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.JDA;

public class SkipToCommand extends Command
{

    public SkipToCommand(JDA jda)
    {
        super(jda);
        this.invoke = "skipto";
        this.usage = "skipto [Number in Queue]";
        this.description = "Skips to another song in the queue";
        this.commandType = CommandType.MUSIC;

    }

    @Override
    public void executeCommand(final CommandEvent event) {
        if(!event.isDJ())
        {
            event.replyError("You need to be a DJ to do this!");
            return;
        }
        int index = 0;
        try {
            index = Integer.parseInt(event.getArguments().getAsString(0));
        }
        catch (NumberFormatException e) {
            event.replyError(" `" + event.getArguments().getAsString(0) + "` is not a valid integer!");
            return;
        }
        final AudioHandler handler = ResultHandler.getHandler(event.getGuild());
        if (index < 1 || index > handler.getQueue().size()) {
            event.replyError("Position must be a valid integer between 1 and " + handler.getQueue().size() + "!");
            return;
        }
        handler.getQueue().skip(index - 1);
        event.replySuccess("Skipped to **" + handler.getQueue().get(0).getTrack().getInfo().title + "**");
        handler.getPlayer().stopTrack();
    }
}
