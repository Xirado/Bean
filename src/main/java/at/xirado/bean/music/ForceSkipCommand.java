// 
// Decompiled by Procyon v0.5.36
// 

package at.xirado.bean.music;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.CommandType;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

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
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {

        if(context.getMember().getVoiceState() != null)
        {
            if(!context.getMember().getVoiceState().inVoiceChannel())
            {
                context.replyError("You need to be in a Voicechannel to do this!");
                return;
            }
        }else
        {
            context.replyError("You need to be in a Voicechannel to do this!");
            return;
        }

        if(event.getGuild().getSelfMember().getVoiceState() == null || !event.getGuild().getSelfMember().getVoiceState().inVoiceChannel())
        {
            context.replyError("There is no music playing!");
            return;
        }
        final AudioHandler handler = ResultHandler.getHandler(event.getGuild());
        if (event.getAuthor().getIdLong() == handler.getRequester()) {
            context.replySuccess("Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**");
            handler.getPlayer().stopTrack();
            return;
        }
        if(!context.isDJ())
        {
            context.replyError("You need to be a DJ to do this!");
            return;
        }
        final User u = event.getJDA().getUserById(handler.getRequester());
        context.replySuccess("Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title + "** (requested by " + ((u == null) ? "someone" : ("**" + u.getAsTag() + "**")) + ")");
        handler.getPlayer().stopTrack();
    }
}
