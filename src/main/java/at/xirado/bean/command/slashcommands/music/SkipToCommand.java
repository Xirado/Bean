package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.BlockingQueue;

public class SkipToCommand extends SlashCommand {
    public SkipToCommand() {
        setCommandData(Commands.slash("skipto", "Skips to a specified track in the queue.")
                .addOption(OptionType.INTEGER, "index", "Index to skip to.", true)
        );
        addCommandFlags(CommandFlag.DJ_ONLY, CommandFlag.MUST_BE_IN_VC, CommandFlag.MUST_BE_IN_SAME_VC, CommandFlag.REQUIRES_LAVALINK_NODE);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        long index = event.getOption("index").getAsLong();
        GuildAudioPlayer guildAudioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        if (guildAudioPlayer.getPlayer().getPlayingTrack() == null) {
            ctx.sendSimpleEphemeralEmbed("There is no music playing!");
            return;
        }
        BlockingQueue<AudioTrack> queue = guildAudioPlayer.getScheduler().getQueue();
        if (queue.size() == 0) {
            ctx.sendSimpleEphemeralEmbed("There is nothing in the queue!");
            return;
        }
        if (index < 1 || index > queue.size()) {
            ctx.sendSimpleEphemeralEmbed("Index must be a valid integer between 1 and " + queue.size() + "!");
            return;
        }
        for (int i = 0; i < index - 1; i++) {
            queue.remove();
        }
        guildAudioPlayer.getScheduler().nextTrack();
        guildAudioPlayer.forcePlayerUpdate();
        ctx.sendSimpleEmbed("**Skipped!** Now playing: `" + guildAudioPlayer.getPlayer().getPlayingTrack().getInfo().title + "`");
    }

}
