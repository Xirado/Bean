package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

public class PlayerCommand extends SlashCommand {

    public PlayerCommand() {
        setCommandData(Commands.slash("player", "Music Player Navigation"));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        GuildAudioPlayer player = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());

        AudioTrack track = player.getPlayer().getPlayingTrack();
        boolean isRepeat = player.getScheduler().isRepeat();
        boolean isPaused = player.getPlayer().isPaused();

        event.reply("One moment...").queue(
                x -> player.playerSetup(
                        (GuildMessageChannel) event.getChannel(),
                        (success) -> event.getHook().deleteOriginal().queue(),
                        (error) -> event.reply("An error occurred!").setEphemeral(true).queue()
                )
        );
    }
}
