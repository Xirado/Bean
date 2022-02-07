package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.misc.Util;
import at.xirado.bean.misc.objects.TrackInfo;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClearCommand extends SlashCommand
{
    public ClearCommand()
    {
        setCommandData(Commands.slash("clear", "Clears the queue"));
        addCommandFlags(CommandFlag.MUST_BE_IN_SAME_VC, CommandFlag.MUST_BE_IN_VC, CommandFlag.REQUIRES_LAVALINK_NODE);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx)
    {
        GuildVoiceState state = event.getGuild().getSelfMember().getVoiceState();
        if (state.getChannel() == null)
        {
            event.replyEmbeds(EmbedUtil.warningEmbed("I am not connected to a voice channel!")).queue();
            return;
        }
        GuildAudioPlayer player = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        if (player.getScheduler().getQueue().isEmpty())
        {
            event.replyEmbeds(EmbedUtil.warningEmbed("The queue is already empty!")).queue();
            return;
        }

        if (Util.getListeningUsers(state.getChannel()) == 1)
        {
            player.getScheduler().getQueue().clear();
            event.replyEmbeds(EmbedUtil.defaultEmbed("Cleared the queue!")).queue();
            return;
        }

        if (!ctx.getGuildData().isDJ(event.getMember()))
        {
            boolean allowedToStop = true;
            long userId = event.getUser().getIdLong();
            List<AudioTrack> tracks = new ArrayList<>(player.getScheduler().getQueue());
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

        player.getScheduler().getQueue().clear();
        event.replyEmbeds(EmbedUtil.defaultEmbed("Cleared the queue!")).queue();
    }
}
