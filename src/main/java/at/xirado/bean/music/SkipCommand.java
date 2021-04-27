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

public class SkipCommand extends Command
{

    public SkipCommand(JDA jda)
    {
        super(jda);
        this.invoke = "skip";
        this.description = "Vote to skip the currently playing song";
        this.usage = "skip";
        this.commandType = CommandType.MUSIC;
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
        final AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if (event.getAuthor().getIdLong() == handler.getRequester()) {
            context.replySuccess("Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**");
            handler.getPlayer().stopTrack();
        }
        else {
            final int listeners = (int)event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().stream().filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened()).count();
            String msg;
            if (handler.getVotes().contains(event.getAuthor().getId())) {
                msg = context.WARNING_EMOTE + " You already voted to skip this song `[";
            }
            else {
                msg = context.SUCCESS_EMOTE + " You voted to skip the song `[";
                handler.getVotes().add(event.getAuthor().getId());
            }
            final int skippers = (int)event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().stream().filter(m -> handler.getVotes().contains(m.getUser().getId())).count();
            final int required = (int)Math.ceil(listeners * 0.55);
            msg = msg + skippers + " votes, " + required + "/" + listeners + " needed]`";
            if (skippers >= required) {
                final User u = event.getJDA().getUserById(handler.getRequester());
                msg = msg + "\n" + context.SUCCESS_EMOTE + " Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**" + ((handler.getRequester() == 0L) ? "" : (" (requested by " + ((u == null) ? "someone" : ("**" + u.getName() + "**")) + ")"));
                handler.getPlayer().stopTrack();
            }
            context.reply(msg);
        }
    }
}
