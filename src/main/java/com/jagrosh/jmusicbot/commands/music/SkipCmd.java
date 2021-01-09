// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import net.dv8tion.jda.api.entities.User;

public class SkipCmd extends MusicCommand
{
    public SkipCmd(final Bot bot) {
        super(bot);
        this.name = "skip";
        this.help = "votes to skip the current song";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
    }
    
    @Override
    public void doCommand(final CommandEvent event) {
        final AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if (event.getAuthor().getIdLong() == handler.getRequester()) {
            event.reply(event.getClient().getSuccess() + " Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**");
            handler.getPlayer().stopTrack();
        }
        else {
            final int listeners = (int)event.getSelfMember().getVoiceState().getChannel().getMembers().stream().filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened()).count();
            String msg;
            if (handler.getVotes().contains(event.getAuthor().getId())) {
                msg = event.getClient().getWarning() + " You already voted to skip this song `[";
            }
            else {
                msg = event.getClient().getSuccess() + " You voted to skip the song `[";
                handler.getVotes().add(event.getAuthor().getId());
            }
            final int skippers = (int)event.getSelfMember().getVoiceState().getChannel().getMembers().stream().filter(m -> handler.getVotes().contains(m.getUser().getId())).count();
            final int required = (int)Math.ceil(listeners * 0.55);
            msg = msg + skippers + " votes, " + required + "/" + listeners + " needed]`";
            if (skippers >= required) {
                final User u = event.getJDA().getUserById(handler.getRequester());
                msg = msg + "\n" + event.getClient().getSuccess() + " Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**" + ((handler.getRequester() == 0L) ? "" : (" (requested by " + ((u == null) ? "someone" : ("**" + u.getName() + "**")) + ")"));
                handler.getPlayer().stopTrack();
            }
            event.reply(msg);
        }
    }
}
