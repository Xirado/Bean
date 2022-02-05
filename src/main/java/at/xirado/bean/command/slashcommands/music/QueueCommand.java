package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.interactions.ButtonPaginator;
import at.xirado.bean.misc.FormatUtil;
import at.xirado.bean.misc.Util;
import at.xirado.bean.misc.objects.TrackInfo;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class QueueCommand extends SlashCommand
{
    public QueueCommand()
    {
        setCommandData(Commands.slash("queue", "Shows the current queue.")
                .addOption(OptionType.INTEGER, "page", "Page of the queue.", false)
        );
    }


    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx)
    {
        ButtonPaginator.Builder builder = new ButtonPaginator.Builder(event.getJDA())
                .setEventWaiter(Bean.getInstance().getEventWaiter())
                .setItemsPerPage(10)
                .setTimeout(1, TimeUnit.MINUTES);
        int page = 1;
        if (event.getOption("page") != null) page = (int) event.getOption("page").getAsLong();
        GuildAudioPlayer guildAudioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        Queue<AudioTrack> queue = guildAudioPlayer.getScheduler().getQueue();
        if (queue.isEmpty())
        {
            if (guildAudioPlayer.getPlayer().getPlayingTrack() != null)
            {
                if (guildAudioPlayer.getScheduler().isRepeat())
                    ctx.sendSimpleEmbed("\uD83D\uDD01 **Currently playing** " + Util.titleMarkdown(guildAudioPlayer.getPlayer().getPlayingTrack()));
                else
                    ctx.sendSimpleEmbed("**Currently playing** " + Util.titleMarkdown(guildAudioPlayer.getPlayer().getPlayingTrack()));
            }
            else
                ctx.replyError("There is no music playing!").queue();
            return;
        }
        String[] tracks = queue.stream().map(x -> Util.titleMarkdown(x, false) + " (<@" + x.getUserData(TrackInfo.class).getRequesterIdLong() + ">)").toArray(String[]::new);
        builder.setTitle(getQueueTitle(guildAudioPlayer))
                .setItems(tracks)
                .addAllowedUsers(event.getUser().getIdLong())
                .setColor(Color.decode("#452350"));
        int finalPage = page;
        event.deferReply().queue(hook -> builder.build().paginate(hook.sendMessage(""), finalPage));
    }

    private String getQueueTitle(GuildAudioPlayer player)
    {
        final StringBuilder sb = new StringBuilder();
        if (player.getPlayer().getPlayingTrack() != null)
        {
            sb.append(player.getPlayer().isPaused() ? "\u23f8" : "\u25b6").append(player.getScheduler().isRepeat() ? "\uD83D\uDD01" : "").append(" ").append(Util.titleMarkdown(player.getPlayer().getPlayingTrack())).append("\n");
        }
        int entries = player.getScheduler().getQueue().size();
        long duration = 0;
        for (AudioTrack track : player.getScheduler().getQueue())
        {
            duration += track.getDuration();
        }
        return FormatUtil.filter(sb.append(entries).append(entries == 1 ? " entry | `" : " entries | `").append(FormatUtil.formatTime(duration)).append("`").toString());
    }
}
