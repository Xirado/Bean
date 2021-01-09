// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.TimeUnit;

public class SearchCmd extends MusicCommand
{
    protected String searchPrefix;
    private final OrderedMenu.Builder builder;
    private final String searchingEmoji;
    
    public SearchCmd(final Bot bot) {
        super(bot);
        this.searchPrefix = "ytsearch:";
        this.searchingEmoji = bot.getConfig().getSearching();
        this.name = "search";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.arguments = "<query>";
        this.help = "searches Youtube for a provided query";
        this.beListening = true;
        this.bePlaying = false;
        this.botPermissions = new Permission[] { Permission.MESSAGE_EMBED_LINKS };
        this.builder = new OrderedMenu.Builder().allowTextInput(true).useNumbers().useCancelButton(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(1L, TimeUnit.MINUTES);
    }
    
    @Override
    public void doCommand(final CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a query.");
            return;
        }
        event.reply(this.searchingEmoji + " Searching... `[" + event.getArgs() + "]`", m -> this.bot.getPlayerManager().loadItemOrdered(event.getGuild(), this.searchPrefix + event.getArgs(), new ResultHandler(m, event)));
    }
    
    private class ResultHandler implements AudioLoadResultHandler
    {
        private final Message m;
        private final CommandEvent event;
        
        private ResultHandler(final Message m, final CommandEvent event) {
            this.m = m;
            this.event = event;
        }
        
        @Override
        public void trackLoaded(final AudioTrack track) {
            if (SearchCmd.this.bot.getConfig().isTooLong(track)) {
                this.m.editMessage(FormatUtil.filter(this.event.getClient().getWarning() + " This track (**" + track.getInfo().title + "**) is longer than the allowed maximum: `" + FormatUtil.formatTime(track.getDuration()) + "` > `" + SearchCmd.this.bot.getConfig().getMaxTime() + "`")).queue();
                return;
            }
            final AudioHandler handler = (AudioHandler)this.event.getGuild().getAudioManager().getSendingHandler();
            final int pos = handler.addTrack(new QueuedTrack(track, this.event.getAuthor())) + 1;
            this.m.editMessage(FormatUtil.filter(this.event.getClient().getSuccess() + " Added **" + track.getInfo().title + "** (`" + FormatUtil.formatTime(track.getDuration()) + "`) " + ((pos == 0) ? "to begin playing" : (" to the queue at position " + pos)))).queue();
        }
        
        @Override
        public void playlistLoaded(final AudioPlaylist playlist) {
            SearchCmd.this.builder.setColor(this.event.getSelfMember().getColor()).setText(FormatUtil.filter(this.event.getClient().getSuccess() + " Search results for `" + this.event.getArgs() + "`:")).setChoices(new String[0]).setSelection((msg, i) -> {
                AudioTrack track = playlist.getTracks().get(i - 1);
                if (SearchCmd.this.bot.getConfig().isTooLong(track)) {
                    this.event.replyWarning("This track (**" + track.getInfo().title + "**) is longer than the allowed maximum: `" + FormatUtil.formatTime(track.getDuration()) + "` > `" + SearchCmd.this.bot.getConfig().getMaxTime() + "`");
                    return;
                }
                else {
                    AudioHandler handler = (AudioHandler)this.event.getGuild().getAudioManager().getSendingHandler();
                    int pos = handler.addTrack(new QueuedTrack(track, this.event.getAuthor())) + 1;
                    CommandEvent event = this.event;
                    StringBuilder sb = new StringBuilder().append("Added **").append(FormatUtil.filter(track.getInfo().title)).append("** (`").append(FormatUtil.formatTime(track.getDuration())).append("`) ");
                    String string;
                    if (pos == 0) {
                        string = "to begin playing";
                    }
                    else {
                        string = " to the queue at position " + pos;
                    }
                    event.replySuccess(sb.append(string).toString());
                    return;
                }
            }).setCancel(msg -> {}).setUsers(this.event.getAuthor());
            for (int j = 0; j < 4 && j < playlist.getTracks().size(); ++j) {
                final AudioTrack track2 = playlist.getTracks().get(j);
                SearchCmd.this.builder.addChoices("`[" + FormatUtil.formatTime(track2.getDuration()) + "]` [**" + track2.getInfo().title + "**](" + track2.getInfo().uri + ")");
            }
            SearchCmd.this.builder.build().display(this.m);
        }
        
        @Override
        public void noMatches() {
            this.m.editMessage(FormatUtil.filter(this.event.getClient().getWarning() + " No results found for `" + this.event.getArgs() + "`.")).queue();
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
