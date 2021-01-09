// 
// Decompiled by Procyon v0.5.36
// 

package at.Xirado.Bean.Music;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public class ForceSkipCommand extends Command
{


    public ForceSkipCommand(JDA jda)
    {
        super(jda);
        this.invoke = "forceskip";
        this.usage = "forceskip";
        this.description = "Force-skips the currently playing song";
        this.commandType = CommandType.MUSIC;
        this.aliases = new String[]{"fskip", "fs"};
    }

    @Override
    public void execute(CommandEvent event) {
        if(!event.isDJ())
        {
            event.replyError("You need to be a DJ to do this!");
            return;
        }
        final AudioHandler handler = ResultHandler.getHandler(event.getGuild());
        final User u = event.getJDA().getUserById(handler.getRequester());
        event.replySuccess("Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title + "** (requested by " + ((u == null) ? "someone" : ("**" + u.getAsTag() + "**")) + ")");
        handler.getPlayer().stopTrack();
    }
}
