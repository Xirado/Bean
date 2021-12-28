package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.MusicUtil;
import at.xirado.bean.music.AudioScheduler;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.internal.JDAImpl;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class VoiceUpdateListener extends ListenerAdapter
{

    /**
     * How long the bot stays in a VoiceChannel after every member left
     * The bot will pause the currently playing track instantly and will clear the queue and disconnect
     * after the specified duration.
     */
    public static final long TIME_UNTIL_AUTO_DISCONNECT = TimeUnit.MINUTES.toSeconds(1);


    /**
     * For when the bot joins a channel
     * @param event
     */
    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event)
    {
        if (GuildJoinListener.isGuildBanned(event.getGuild().getIdLong()))
            return;
        if (event.getMember().equals(event.getGuild().getSelfMember()))
        {
            if (!event.getGuild().getSelfMember().getVoiceState().isGuildDeafened())
                try
                {
                    event.getGuild().deafen(event.getGuild().getSelfMember(), true).queue(s -> {}, e -> {});
                } catch (InsufficientPermissionException ignored) {}
            if (event.getChannelJoined() instanceof StageChannel stageChannel)
            {
                GuildAudioPlayer audioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
                if (stageChannel.getStageInstance() == null)
                {
                    if (audioPlayer.getPlayer().getPlayingTrack() != null)
                        stageChannel.createStageInstance(MusicUtil.getStageTopicString(audioPlayer.getPlayer().getPlayingTrack())).queue();
                } else
                {
                    if (audioPlayer.getPlayer().getPlayingTrack() != null)
                        stageChannel.getStageInstance().getManager().setTopic(MusicUtil.getStageTopicString(audioPlayer.getPlayer().getPlayingTrack())).queue();
                }
            }
        }
    }

    /**
     * For when the bot leaves a channel
     * @param event
     */
    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event)
    {
        if (GuildJoinListener.isGuildBanned(event.getGuild().getIdLong()))
            return;
        if (!event.getMember().equals(event.getGuild().getSelfMember()))
            return;
        GuildAudioPlayer audioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        if (event.getChannelLeft() instanceof StageChannel stageChannel)
        {
            if (stageChannel.getStageInstance() != null)
            {
                if (stageChannel.getStageInstance().getTopic().startsWith("Playing "))
                {
                    stageChannel.getStageInstance().delete().queue(s -> {}, e -> {});
                }
            }
        }
        audioPlayer.destroy();
    }

    /**
     * For when the bot gets moved to another channel
     * @param event
     */
    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event)
    {
        if (GuildJoinListener.isGuildBanned(event.getGuild().getIdLong()))
            return;
        if (!event.getMember().equals(event.getGuild().getSelfMember()))
            return;
        GuildAudioPlayer audioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        LavalinkPlayer player = audioPlayer.getPlayer();
        player.setPaused(false);
        if (event.getChannelLeft() instanceof StageChannel stageChannel)
            if (stageChannel.getStageInstance() != null)
                if (stageChannel.getStageInstance().getTopic().startsWith("Playing "))
                    stageChannel.getStageInstance().delete().queue();
        if (event.getChannelJoined() instanceof StageChannel channel)
        {
            event.getGuild().requestToSpeak();
            if (channel.getStageInstance() == null && player.getPlayingTrack() != null)
                channel.createStageInstance(MusicUtil.getStageTopicString(player.getPlayingTrack())).queue();
        }
        if (event.getChannelJoined().getMembers().size() == 1)
        {
            GuildVoiceState voiceState = event.getGuild().getSelfMember().getVoiceState();
            final long channelId = event.getChannelJoined().getIdLong();
            if (player.getPlayingTrack() != null)
                player.setPaused(true);
            Bean.getInstance().getEventWaiter().waitForEvent(
                    GenericGuildVoiceUpdateEvent.class,
                    e ->
                    {
                        if (e.getChannelJoined() == null)
                            return false;
                        if (e.getChannelJoined().getIdLong() != channelId)
                            return false;
                        return !e.getMember().equals(e.getGuild().getSelfMember());
                    },
                    e -> player.setPaused(false),
                    TIME_UNTIL_AUTO_DISCONNECT,
                    TimeUnit.SECONDS,
                    () ->
                    {
                        if (voiceState.getChannel() != null && voiceState.getChannel().getMembers().size() > 1)
                            return;
                        if (voiceState.getChannel() != null && voiceState.getChannel().getIdLong() == channelId)
                            audioPlayer.destroy();
                    }
            );
        }
    }

    /**
     * For when a member gets moved or leaves a channel
     * @param event
     */
    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event)
    {
        if (GuildJoinListener.isGuildBanned(event.getGuild().getIdLong()))
            return;
        if (event.getChannelLeft() == null)
            return;
        if (event.getMember().equals(event.getGuild().getSelfMember()))
            return;
        GuildVoiceState state = event.getGuild().getSelfMember().getVoiceState();
        if (state.getChannel() != null)
        {
            if (state.getChannel().equals(event.getChannelLeft()))
            {
                if (event.getChannelLeft().getMembers().size() == 1)
                {
                    GuildAudioPlayer audioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
                    LavalinkPlayer player = audioPlayer.getPlayer();
                    if (player.getPlayingTrack() != null) {
                        player.setPaused(true);
                    } else {
                        audioPlayer.destroy();
                        return;
                    }
                    final long channelId = event.getChannelLeft().getIdLong();
                    Bean.getInstance().getEventWaiter().waitForEvent(
                            GenericGuildVoiceUpdateEvent.class,
                            e ->
                            {
                                if (e.getChannelJoined() == null)
                                    return false;
                                if (e.getChannelJoined().getIdLong() != channelId)
                                    return false;
                                return !e.getMember().equals(e.getGuild().getSelfMember());
                            },
                            e ->
                            {
                                player.setPaused(false);
                            },
                            TIME_UNTIL_AUTO_DISCONNECT,
                            TimeUnit.SECONDS,
                            () ->
                            {
                                if (state.getChannel() != null && state.getChannel().getMembers().size() > 1)
                                    return;
                                if (state.getChannel() != null && state.getChannel().getIdLong() == channelId)
                                {
                                    audioPlayer.destroy();
                                }
                            }
                    );
                }
            }
        }
    }
}
