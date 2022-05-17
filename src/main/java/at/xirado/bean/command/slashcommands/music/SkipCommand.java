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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

public class SkipCommand extends SlashCommand {
    public SkipCommand() {
        setCommandData(Commands.slash("skip", "Skips the currently playing track."));
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.MUST_BE_IN_SAME_VC, CommandFlag.REQUIRES_LAVALINK_NODE);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        GuildAudioPlayer guildAudioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        AudioTrack track = guildAudioPlayer.getPlayer().getPlayingTrack();
        if (track == null) {
            ctx.sendSimpleEphemeralEmbed("There is no music to skip!");
            return;
        }
        if (track.getUserData(TrackInfo.class).getRequesterIdLong() == event.getUser().getIdLong()) {
            guildAudioPlayer.getScheduler().nextTrack();
            AudioTrack nextTrack = guildAudioPlayer.getPlayer().getPlayingTrack();
            guildAudioPlayer.forcePlayerUpdate();
            if (nextTrack == null) {
                ctx.sendSimpleEphemeralEmbed("**Skipped!**");
                return;
            }
            ctx.sendSimpleEphemeralEmbed("**Skipped!** Now playing " + Util.titleMarkdown(nextTrack));
            return;
        }
        if (!ctx.getGuildData().isDJ(event.getMember())) {
            event.replyEmbeds(EmbedUtil.errorEmbed("You need to be a DJ to do this!")).setEphemeral(true).queue();
            return;
        }
        guildAudioPlayer.getScheduler().nextTrack();
        guildAudioPlayer.forcePlayerUpdate();
        AudioTrack nextTrack = guildAudioPlayer.getPlayer().getPlayingTrack();
        if (nextTrack == null) {
            ctx.sendSimpleEphemeralEmbed("**Skipped!**");
            return;
        }
        ctx.sendSimpleEphemeralEmbed("**Skipped!** Now playing " + Util.titleMarkdown(nextTrack));
    }
}
