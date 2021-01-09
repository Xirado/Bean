// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class QueueCmd extends MusicCommand
{
    private static final String REPEAT = "\ud83d\udd01";
    private final Paginator.Builder builder;
    
    public QueueCmd(final Bot bot) {
        super(bot);
        this.name = "queue";
        this.help = "shows the current queue";
        this.arguments = "[pagenum]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
        this.botPermissions = new Permission[] { Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS };
        this.builder = new Paginator.Builder().setColumns(1).setFinalAction(m -> {
            try {
                m.clearReactions().queue();
            }
            catch (PermissionException ex) {}
        }).setItemsPerPage(10).waitOnSinglePage(false).useNumberedItems(true).showPageNumbers(true).wrapPageEnds(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(1L, TimeUnit.MINUTES);
    }
    
    @Override
    public void doCommand(final CommandEvent event) {
        int pagenum = 1;
        try {
            pagenum = Integer.parseInt(event.getArgs());
        }
        catch (NumberFormatException ex) {}
        final AudioHandler ah = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        final List<QueuedTrack> list = ah.getQueue().getList();
        if (list.isEmpty()) {
            final Message nowp = ah.getNowPlaying(event.getJDA());
            final Message nonowp = ah.getNoMusicPlaying(event.getJDA());
            final Message built = new MessageBuilder().setContent(event.getClient().getWarning() + " There is no music in the queue!").setEmbed(((nowp == null) ? nonowp : nowp).getEmbeds().get(0)).build();
            event.reply(built, m -> {
                if (nowp != null) {
                    this.bot.getNowplayingHandler().setLastNPMessage(m);
                }
            });
            return;
        }
        final String[] songs = new String[list.size()];
        long total = 0L;
        for (int i = 0; i < list.size(); ++i) {
            total += list.get(i).getTrack().getDuration();
            songs[i] = list.get(i).toString();
        }
        final Settings settings = event.getClient().getSettingsFor(event.getGuild());
        final long fintotal = total;
        this.builder.setText((i1, i2) -> this.getQueueTitle(ah, event.getClient().getSuccess(), songs.length, fintotal, settings.getRepeatMode())).setItems(songs)
                .setUsers(event.getAuthor()).setColor(event.getSelfMember().getColor());
        this.builder.build().paginate(event.getChannel(), pagenum);
    }
    
    private String getQueueTitle(final AudioHandler ah, final String success, final int songslength, final long total, final boolean repeatmode) {
        final StringBuilder sb = new StringBuilder();
        if (ah.getPlayer().getPlayingTrack() != null) {
            sb.append(ah.getPlayer().isPaused() ? "\u23f8" : "\u25b6").append(" **").append(ah.getPlayer().getPlayingTrack().getInfo().title).append("**\n");
        }
        return FormatUtil.filter(sb.append(success).append(" Current Queue | ").append(songslength).append(" entries | `").append(FormatUtil.formatTime(total)).append("` ").append(repeatmode ? "| \ud83d\udd01" : "").toString());
    }
}
