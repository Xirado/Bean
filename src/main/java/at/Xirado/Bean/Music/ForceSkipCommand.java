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

import java.util.Arrays;

public class ForceSkipCommand extends Command
{


    public ForceSkipCommand(JDA jda)
    {
        super(jda);
        this.invoke = "forceskip";
        this.usage = "forceskip";
        this.description = "Force-skips the currently playing song";
        this.commandType = CommandType.MUSIC;
        this.aliases = Arrays.asList("fskip", "fs");
    }

    @Override
    public void executeCommand(CommandEvent event) {

        if(event.getMember().getVoiceState() != null)
        {
            if(!event.getMember().getVoiceState().inVoiceChannel())
            {
                event.replyError("You need to be in a Voicechannel to do this!");
                return;
            }
        }else
        {
            event.replyError("You need to be in a Voicechannel to do this!");
            return;
        }

        if(event.getGuild().getSelfMember().getVoiceState() == null || !event.getGuild().getSelfMember().getVoiceState().inVoiceChannel())
        {
            event.replyError("There is no music playing!");
            return;
        }
        final AudioHandler handler = ResultHandler.getHandler(event.getGuild());
        if (event.getAuthor().getIdLong() == handler.getRequester()) {
            event.replySuccess("Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**");
            handler.getPlayer().stopTrack();
            return;
        }
        if(!event.isDJ())
        {
            event.replyError("You need to be a DJ to do this!");
            return;
        }
        final User u = event.getJDA().getUserById(handler.getRequester());
        event.replySuccess("Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title + "** (requested by " + ((u == null) ? "someone" : ("**" + u.getAsTag() + "**")) + ")");
        handler.getPlayer().stopTrack();
    }
}
