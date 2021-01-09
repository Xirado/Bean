// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;

public class PlaynextCmd extends DJCommand
{
    private final String loadingEmoji;
    
    public PlaynextCmd(final Bot bot) {
        super(bot);
        this.loadingEmoji = bot.getConfig().getLoading();
        this.name = "playnext";
        this.arguments = "<title|URL>";
        this.help = "plays a single song next";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = false;
    }
    
    @Override
    public void doCommand(final CommandEvent event) {
        if (event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
            event.replyWarning("Please include a song title or URL!");
            return;
        }
        final String args = (event.getArgs().startsWith("<") && event.getArgs().endsWith(">")) ? event.getArgs().substring(1, event.getArgs().length() - 1) : (event.getArgs().isEmpty() ? event.getMessage().getAttachments().get(0).getUrl() : event.getArgs());
        event.reply(this.loadingEmoji + " Loading... `[" + args + "]`", m -> this.bot.getPlayerManager().loadItemOrdered(event.getGuild(), args, new ResultHandler(m, event, false)));
    }
    
    private class ResultHandler implements AudioLoadResultHandler
    {
        private final Message m;
        private final CommandEvent event;
        private final boolean ytsearch;
        
        private ResultHandler(final Message m, final CommandEvent event, final boolean ytsearch) {
            this.m = m;
            this.event = event;
            this.ytsearch = ytsearch;
        }
        
        private void loadSingle(final AudioTrack track) {
            if (PlaynextCmd.this.bot.getConfig().isTooLong(track)) {
                this.m.editMessage(FormatUtil.filter(this.event.getClient().getWarning() + " This track (**" + track.getInfo().title + "**) is longer than the allowed maximum: `" + FormatUtil.formatTime(track.getDuration()) + "` > `" + FormatUtil.formatTime(PlaynextCmd.this.bot.getConfig().getMaxSeconds() * 1000L) + "`")).queue();
                return;
            }
            final AudioHandler handler = (AudioHandler)this.event.getGuild().getAudioManager().getSendingHandler();
            final int pos = handler.addTrackToFront(new QueuedTrack(track, this.event.getAuthor())) + 1;
            final String addMsg = FormatUtil.filter(this.event.getClient().getSuccess() + " Added **" + track.getInfo().title + "** (`" + FormatUtil.formatTime(track.getDuration()) + "`) " + ((pos == 0) ? "to begin playing" : (" to the queue at position " + pos)));
            this.m.editMessage(addMsg).queue();
        }
        
        @Override
        public void trackLoaded(final AudioTrack track) {
            this.loadSingle(track);
        }
        
        @Override
        public void playlistLoaded(final AudioPlaylist playlist) {
            AudioTrack single;
            if (playlist.getTracks().size() == 1 || playlist.isSearchResult()) {
                single = ((playlist.getSelectedTrack() == null) ? playlist.getTracks().get(0) : playlist.getSelectedTrack());
            }
            else if (playlist.getSelectedTrack() != null) {
                single = playlist.getSelectedTrack();
            }
            else {
                single = playlist.getTracks().get(0);
            }
            this.loadSingle(single);
        }
        
        @Override
        public void noMatches() {
            if (this.ytsearch) {
                this.m.editMessage(FormatUtil.filter(this.event.getClient().getWarning() + " No results found for `" + this.event.getArgs() + "`.")).queue();
            }
            else {
                PlaynextCmd.this.bot.getPlayerManager().loadItemOrdered(this.event.getGuild(), "ytsearch:" + this.event.getArgs(), new ResultHandler(this.m, this.event, true));
            }
        }
        
        @Override
        public void loadFailed(final FriendlyException throwable) {
            if (throwable.severity == FriendlyException.Severity.COMMON) {
                this.m.editMessage(this.event.getClient().getError() + " Error loading: " + throwable.getMessage()).queue();
            }
            else {
                this.m.editMessage(this.event.getClient().getError() + " Error loading track.").queue();
            }
        }
    }
}
