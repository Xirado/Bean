package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

public class PauseCommand extends SlashCommand {
    public PauseCommand() {
        setCommandData(Commands.slash("pause", "Pauses the currently playing track."));
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.DJ_ONLY, CommandFlag.MUST_BE_IN_SAME_VC);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        GuildAudioPlayer guildPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        AudioPlayer player = guildPlayer.getPlayer();

        if (player.getPlayingTrack() == null) {
            ctx.replyError("I'm currently not playing any music!").setEphemeral(true).queue();
            return;
        }
        if (player.isPaused()) {
            ctx.replyError("The player is already paused! Use `/resume`").setEphemeral(true).queue();
            return;
        }
        player.setPaused(true);
        guildPlayer.forcePlayerComponentsUpdate();
        ctx.sendSimpleEphemeralEmbed("The player is now paused!");
    }
}
