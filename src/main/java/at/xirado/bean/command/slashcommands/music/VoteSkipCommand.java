package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.Util;
import at.xirado.bean.misc.objects.TrackInfo;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class VoteSkipCommand extends SlashCommand
{
    public VoteSkipCommand()
    {
        setCommandData(new CommandData("voteskip", "Votes to skip the currently playing track."));
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.MUST_BE_IN_SAME_VC);
    }
    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        GuildAudioPlayer guildAudioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        if (event.getMember().getVoiceState().isDeafened())
        {
            ctx.sendSimpleEmbed("You can't do this since you're deafened!");
            return;
        }
        AudioTrack track = guildAudioPlayer.getPlayer().getPlayingTrack();
        if (track == null)
        {
            ctx.sendSimpleEmbed("There is no music to skip!");
            return;
        }
        TrackInfo trackInfo = track.getUserData(TrackInfo.class);
        long requester = trackInfo.getRequesterIdLong();
        if (event.getUser().getIdLong() == requester)
        {
            if (guildAudioPlayer.getScheduler().isRepeat())
                guildAudioPlayer.getScheduler().setRepeat(false);
            guildAudioPlayer.getScheduler().nextTrack();
            AudioTrack nextTrack = guildAudioPlayer.getPlayer().getPlayingTrack();
            if (nextTrack == null)
            {
                ctx.sendSimpleEmbed("**Skipped!**");
                return;
            }
            ctx.sendSimpleEmbed("**Skipped!** Now playing " + Util.titleMarkdown(nextTrack));
            return;
        }
        int listeners = (int)event.getMember().getVoiceState().getChannel().getMembers().stream()
                .filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened()).count();
        if (trackInfo.getVoteSkips().contains(event.getUser().getIdLong()))
        {
            ctx.sendSimpleEmbed("You already voted to skip this song!");
            return;
        }
        trackInfo.addVoteSkip(event.getMember().getIdLong());
        int skippers = (int)event.getMember().getVoiceState().getChannel().getMembers().stream()
                .filter(m -> trackInfo.getVoteSkips().contains(m.getIdLong())).count();
        int required = (int)Math.ceil(listeners * .55);
        if (skippers >= required)
        {
            if (guildAudioPlayer.getScheduler().isRepeat())
                guildAudioPlayer.getScheduler().setRepeat(false);
            guildAudioPlayer.getScheduler().nextTrack();
            AudioTrack nextTrack = guildAudioPlayer.getPlayer().getPlayingTrack();
            if (nextTrack == null)
            {
                ctx.sendSimpleEmbed("**Skipped!**");
                return;
            }
            ctx.sendSimpleEmbed("**Skipped!** Now playing " + Util.titleMarkdown(nextTrack));
        } else {
            ctx.sendSimpleEmbed("Voted to skip: **"+(required-skippers)+"** more votes needed");

        }

    }
}
