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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkipCommand extends SlashCommand
{
    public SkipCommand()
    {
        setCommandData(new CommandData("skip", "Skips the currently playing track."));
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.MUST_BE_IN_SAME_VC);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        GuildAudioPlayer guildAudioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        AudioTrack track = guildAudioPlayer.getPlayer().getPlayingTrack();
        if (track == null)
        {
            ctx.sendSimpleEmbed("There is no music to skip!");
            return;
        }
        if (track.getUserData(TrackInfo.class).getRequesterIdLong() == event.getUser().getIdLong())
        {
            if (guildAudioPlayer.getScheduler().isRepeat())
            {
                guildAudioPlayer.getScheduler().setRepeat(false);
            }
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
        if (!ctx.getGuildData().isDJ(event.getMember()))
        {
            event.replyEmbeds(EmbedUtil.errorEmbed("You need to be a DJ to do this!")).queue();
            return;
        }
        if (guildAudioPlayer.getScheduler().isRepeat())
        {
            guildAudioPlayer.getScheduler().setRepeat(false);
        }
        guildAudioPlayer.getScheduler().nextTrack();
        AudioTrack nextTrack = guildAudioPlayer.getPlayer().getPlayingTrack();
        if (nextTrack == null)
        {
            ctx.sendSimpleEmbed("**Skipped!**");
            return;
        }
        ctx.sendSimpleEmbed("**Skipped!** Now playing " + Util.titleMarkdown(nextTrack));
    }
}
