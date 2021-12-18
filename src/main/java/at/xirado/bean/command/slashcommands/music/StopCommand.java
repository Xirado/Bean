package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.misc.objects.TrackInfo;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class StopCommand extends SlashCommand
{
    public StopCommand()
    {
        setCommandData(new CommandData("stop", "Disconnects the bot and clears the queue."));
        addCommandFlags(CommandFlag.MUST_BE_IN_SAME_VC, CommandFlag.MUST_BE_IN_VC);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        AudioManager audioManager = event.getGuild().getAudioManager();
        if (!audioManager.isConnected())
        {
            event.replyEmbeds(EmbedUtil.warningEmbed("I am not connected to a voice channel!")).queue();
            return;
        }
        GuildAudioPlayer player = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        if (player.getPlayer().getPlayingTrack() == null || connectedUsers(audioManager.getConnectedChannel()) == 1)
        {
            String name = audioManager.getConnectedChannel().getName();
            event.getGuild().getAudioManager().closeAudioConnection();
            event.replyEmbeds(EmbedUtil.defaultEmbed("Disconnected from **"+name+"**!")).queue();
            return;
        }
        if (!ctx.getGuildData().isDJ(event.getMember()))
        {
            boolean allowedToStop = true;
            long userId = event.getUser().getIdLong();
            List<AudioTrack> tracks = new ArrayList<>(player.getScheduler().getQueue());
            tracks.add(player.getPlayer().getPlayingTrack());
            for (AudioTrack track : tracks)
            {
                TrackInfo trackInfo = track.getUserData(TrackInfo.class);
                if (trackInfo.getRequesterIdLong() != userId)
                {
                    allowedToStop = false;
                    break;
                }
            }
            if (!allowedToStop)
            {
                event.replyEmbeds(EmbedUtil.errorEmbed("You need to be a DJ to do this!")).queue();
                return;
            }
        }

        String name = audioManager.getConnectedChannel().getName();
        event.getGuild().getAudioManager().closeAudioConnection();
        event.replyEmbeds(EmbedUtil.defaultEmbed("Disconnected from **"+name+"**!")).queue();
    }

    private int connectedUsers(@Nonnull VoiceChannel channel)
    {
        int nonBots = 0;
        for (Member member : channel.getMembers())
        {
            if (!member.getUser().isBot())
                nonBots++;
        }
        return nonBots;
    }
}
