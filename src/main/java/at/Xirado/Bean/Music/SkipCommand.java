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
    public void execute(final CommandEvent event) {
        final AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if (event.getAuthor().getIdLong() == handler.getRequester()) {
            event.replySuccess("Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**");
            handler.getPlayer().stopTrack();
        }
        else {
            final int listeners = (int)event.getSelfMember().getVoiceState().getChannel().getMembers().stream().filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened()).count();
            String msg;
            if (handler.getVotes().contains(event.getAuthor().getId())) {
                msg = event.WARNING_EMOTE + " You already voted to skip this song `[";
            }
            else {
                msg = event.SUCCESS_EMOTE + " You voted to skip the song `[";
                handler.getVotes().add(event.getAuthor().getId());
            }
            final int skippers = (int)event.getSelfMember().getVoiceState().getChannel().getMembers().stream().filter(m -> handler.getVotes().contains(m.getUser().getId())).count();
            final int required = (int)Math.ceil(listeners * 0.55);
            msg = msg + skippers + " votes, " + required + "/" + listeners + " needed]`";
            if (skippers >= required) {
                final User u = event.getJDA().getUserById(handler.getRequester());
                msg = msg + "\n" + event.SUCCESS_EMOTE + " Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**" + ((handler.getRequester() == 0L) ? "" : (" (requested by " + ((u == null) ? "someone" : ("**" + u.getName() + "**")) + ")"));
                handler.getPlayer().stopTrack();
            }
            event.reply(msg);
        }
    }
}
