package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import at.xirado.bean.misc.MusicUtil;
import at.xirado.bean.misc.objects.TrackInfo;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class MusicPlayerButtonListener extends ListenerAdapter
{

    public static final List<String> BUTTON_IDS = List.of("player_previous", "player_play", "player_next", "player_repeat", "player_shuffle");

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event)
    {
        if (event.getGuild() == null)
            return;

        if (!BUTTON_IDS.contains(event.getComponentId()))
            return;

        Member selfMember = event.getGuild().getSelfMember();
        Member member = event.getMember();

        if (member.getVoiceState().getChannel() == null)
            return;

        if (member.getVoiceState().getChannel().getIdLong() != selfMember.getVoiceState().getChannel().getIdLong())
            return;

        GuildAudioPlayer guildAudioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        GuildData guildData = GuildManager.getGuildData(event.getGuild());


        switch (event.getComponentId())
        {
            case "player_previous" -> {
                if (!guildData.isDJ(member))
                {
                    event.reply("You must be a DJ to do this!").setEphemeral(true).queue();
                    return;
                }
                Bean.getInstance().getLavalink().getExistingLink(event.getGuild()).getPlayer().seekTo(0L);
            }
            case "player_play" -> {
                if (!guildData.isDJ(member))
                {
                    event.reply("You must be a DJ to do this!").setEphemeral(true).queue();
                    return;
                }
                guildAudioPlayer.getPlayer().setPaused(!guildAudioPlayer.getPlayer().isPaused());
            }
            case "player_next" -> {
                boolean isDj = guildData.isDJ(member);
                boolean isRequester = isRequester(member, guildAudioPlayer.getPlayer().getPlayingTrack());
                if (!isDj && !isRequester)
                    if (!processVoteSkip(event, guildAudioPlayer))
                        return;

                guildAudioPlayer.getScheduler().nextTrack();
            }
            case "player_repeat" -> {
                if (!guildData.isDJ(member))
                {
                    event.reply("You must be a DJ to do this!").setEphemeral(true).queue();
                    return;
                }
                guildAudioPlayer.getScheduler().setRepeat(!guildAudioPlayer.getScheduler().isRepeat());
            }
            case "player_shuffle" -> {
                if (!guildData.isDJ(member))
                {
                    event.reply("You must be a DJ to do this!").setEphemeral(true).queue();
                    return;
                }

                boolean isShuffle = guildAudioPlayer.getScheduler().isShuffle();

                guildAudioPlayer.getScheduler().setShuffle(!isShuffle);

                if (guildAudioPlayer.getScheduler().isShuffle())
                {
                    AudioTrack current = guildAudioPlayer.getPlayer().getPlayingTrack();
                    if (current != null)
                    {
                        TrackInfo trackInfo = current.getUserData(TrackInfo.class);
                        String playlistUrl = trackInfo.getPlaylistUrl();
                        Queue<AudioTrack> queue = guildAudioPlayer.getScheduler().getQueue();
                        List<AudioTrack> fromThisPlaylist = queue
                                .stream().filter(track -> track.getUserData(TrackInfo.class).getPlaylistUrl().equals(playlistUrl))
                                .collect(Collectors.toList());
                        Collections.shuffle(fromThisPlaylist);

                        List<AudioTrack> newQueue = new ArrayList<>(fromThisPlaylist);

                        fromThisPlaylist.forEach(queue::remove);
                        List<AudioTrack> rest = new ArrayList<>(queue);
                        newQueue.addAll(rest);
                        queue.clear();
                        queue.addAll(newQueue);
                    }
                }
            }
        }
        event.deferEdit().setEmbeds(MusicUtil.getPlayerEmbed(guildAudioPlayer.getPlayer().getPlayingTrack())).setActionRows(MusicUtil.getPlayerButtons(guildAudioPlayer.getPlayer().isPaused(), guildAudioPlayer.getScheduler().isRepeat(), guildAudioPlayer.getScheduler().isShuffle())).queue();
    }

    private boolean isRequester(Member member, AudioTrack track)
    {
        if (track == null)
            return false;
        return track.getUserData(TrackInfo.class).getRequesterIdLong() == member.getIdLong();
    }

    private boolean processVoteSkip(ButtonInteractionEvent event, GuildAudioPlayer player)
    {
        AudioTrack track = player.getPlayer().getPlayingTrack();

        TrackInfo trackInfo = track.getUserData(TrackInfo.class);

        int listeners = (int) event.getMember().getVoiceState().getChannel().getMembers().stream()
                .filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened()).count();

        if (trackInfo.getVoteSkips().contains(event.getUser().getIdLong()))
        {
            event.reply("You already voted to skip this song!").setEphemeral(true).queue();
            return false;
        }
        trackInfo.addVoteSkip(event.getMember().getIdLong());

        int skippers = (int) event.getMember().getVoiceState().getChannel().getMembers().stream()
                .filter(m -> trackInfo.getVoteSkips().contains(m.getIdLong())).count();

        int required = (int) Math.ceil(listeners * .55);

        if (skippers >= required)
        {
            if (player.getScheduler().isRepeat())
                player.getScheduler().setRepeat(false);
            return true;
        }
        else
        {
            event.reply("Voted to skip: **" + (required - skippers) + "** more votes needed").setEphemeral(true).queue();
            return false;
        }
    }
}
