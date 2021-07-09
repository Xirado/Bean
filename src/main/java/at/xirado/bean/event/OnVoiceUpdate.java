package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.MusicUtil;
import at.xirado.bean.music.AudioScheduler;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class OnVoiceUpdate extends ListenerAdapter
{

    /**
     * How long the bot stays in a VoiceChannel after every member left
     * The bot will pause the currently playing track instantly and will clear the queue and disconnect
     * after the specified duration.
     */
    public static final long TIME_UNTIL_AUTO_DISCONNECT = TimeUnit.MINUTES.toSeconds(3);


    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event)
    {
        if (event.getMember().equals(event.getGuild().getSelfMember()))
        {
            if (!event.getGuild().getSelfMember().getVoiceState().isGuildDeafened())
                event.getGuild().deafen(event.getGuild().getSelfMember(), true).queue();
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

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event)
    {
        if (!event.getMember().equals(event.getGuild().getSelfMember()))
            return;
        GuildAudioPlayer audioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        AudioPlayer player = audioPlayer.getPlayer();
        AudioScheduler scheduler = audioPlayer.getScheduler();
        if (event.getChannelLeft() instanceof StageChannel stageChannel)
        {
            if (stageChannel.getStageInstance() != null)
            {
                if (stageChannel.getStageInstance().getTopic().startsWith("Now playing: "))
                {
                    stageChannel.getStageInstance().delete().queue();
                }
            }
        }
        scheduler.getQueue().clear();
        if (player.getPlayingTrack() != null)
            player.stopTrack();
        player.setPaused(false);
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event)
    {

        if (!event.getMember().equals(event.getGuild().getSelfMember()))
            return;
        GuildAudioPlayer audioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        AudioPlayer player = audioPlayer.getPlayer();
        player.setPaused(false);
        if (event.getChannelLeft() instanceof StageChannel stageChannel)
        {
            if (stageChannel.getStageInstance() != null)
            {
                if (stageChannel.getStageInstance().getTopic().startsWith("Now playing: "))
                {
                    stageChannel.getStageInstance().delete().queue();
                }
            }
        }
        if (event.getChannelJoined() instanceof StageChannel channel)
        {
            event.getGuild().requestToSpeak();
            if (channel.getStageInstance() == null && player.getPlayingTrack() != null)
                channel.createStageInstance(MusicUtil.getStageTopicString(player.getPlayingTrack())).queue();
        }
        if (event.getChannelJoined().getMembers().size() == 1)
        {
            AudioManager manager = event.getGuild().getAudioManager();
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
                    e ->
                    {
                        player.setPaused(false);
                    },
                    TIME_UNTIL_AUTO_DISCONNECT,
                    TimeUnit.SECONDS,
                    () ->
                    {
                        if (manager.isConnected() && manager.getConnectedChannel().getMembers().size() > 1)
                        {
                            return;
                        }
                        if (manager.isConnected() && manager.getConnectedChannel().getIdLong() == channelId)
                        {
                            manager.closeAudioConnection();
                        }
                    }
            );
        }
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event)
    {
        if (event.getChannelLeft() == null)
            return;
        if (event.getMember().equals(event.getGuild().getSelfMember()))
            return;
        if (event.getGuild().getAudioManager().isConnected())
        {
            AudioManager manager = event.getGuild().getAudioManager();
            if (manager.getConnectedChannel().equals(event.getChannelLeft()))
            {
                if (event.getChannelLeft().getMembers().size() == 1 && event.getChannelLeft().getMembers().get(0).equals(event.getGuild().getSelfMember()))
                {
                    GuildAudioPlayer audioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
                    AudioPlayer player = audioPlayer.getPlayer();
                    if (player.getPlayingTrack() != null)
                        player.setPaused(true);
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
                                if (manager.isConnected() && manager.getConnectedChannel().getMembers().size() > 1)
                                {
                                    return;
                                }
                                if (manager.isConnected() && manager.getConnectedChannel().getIdLong() == channelId)
                                {
                                    manager.closeAudioConnection();
                                }
                            }
                    );
                }
            }
        }
    }
}
