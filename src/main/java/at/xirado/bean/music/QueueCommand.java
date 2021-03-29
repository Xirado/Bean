// 
// Decompiled by Procyon v0.5.36
// 

package at.xirado.bean.music;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.main.DiscordBot;
import com.jagrosh.jdautilities.menu.Paginator;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class QueueCommand extends Command
{
    private static final String REPEAT = "\ud83d\udd01";

    public QueueCommand(JDA jda)
    {
        super(jda);
        this.invoke = "queue";
        this.aliases = Arrays.asList("q");
        this.description = "Shows the queue";
        this.usage = "queue (Page)";
        this.commandType = CommandType.MUSIC;
    }


    @Override
    public void executeCommand(final CommandEvent event) {
        Bot bot = DiscordBot.instance.musicinstance;
        Paginator.Builder builder = new Paginator.Builder().setColumns(1).setFinalAction(m -> {
            try {
                m.clearReactions().queue();
            }
            catch (PermissionException ex) {}
        }).setItemsPerPage(10).waitOnSinglePage(false).useNumberedItems(true).showPageNumbers(true).wrapPageEnds(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(1L, TimeUnit.MINUTES);
        int pagenum = 1;

        try {
            pagenum = Integer.parseInt(event.getArguments().toString(0));
        }
        catch (NumberFormatException ex) {}
        final AudioHandler ah = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        final List<QueuedTrack> list = ah.getQueue().getList();
        if (list.isEmpty()) {
            final Message nowp = ah.getNowPlaying(event.getJDA());
            final Message nonowp = ah.getNoMusicPlaying(event.getJDA());
            final Message built = new MessageBuilder().setContent(CommandEvent.WARNING_EMOTE + " There is no music in the queue!").setEmbed(((nowp == null) ? nonowp : nowp).getEmbeds().get(0)).build();
            event.reply(built, m -> {
                if (nowp != null) {
                    bot.getNowplayingHandler().setLastNPMessage(m);
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
        final long fintotal = total;                                                                                        // REPEAT MODE!!!!!
        builder.setText((i1, i2) -> this.getQueueTitle(ah, event.SUCCESS_EMOTE, songs.length, fintotal, false)).setItems(songs)
                .setUsers(event.getAuthor()).setColor(event.getSelfMember().getColor());
        builder.build().paginate(event.getChannel(), pagenum);
    }

    private String getQueueTitle(final AudioHandler ah, final String success, final int songslength, final long total, final boolean repeatmode) {
        final StringBuilder sb = new StringBuilder();
        if (ah.getPlayer().getPlayingTrack() != null) {
            sb.append(ah.getPlayer().isPaused() ? "\u23f8" : "\u25b6").append(" **").append(ah.getPlayer().getPlayingTrack().getInfo().title).append("**\n");
        }
        return FormatUtil.filter(sb.append(success).append(" Current Queue | ").append(songslength).append(" entries | `").append(FormatUtil.formatTime(total)).append("` ").append(repeatmode ? "| \ud83d\udd01" : "").toString());
    }
}
