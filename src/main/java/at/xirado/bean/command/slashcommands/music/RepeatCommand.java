package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.music.GuildAudioPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

public class RepeatCommand extends SlashCommand {
    public RepeatCommand() {
        setCommandData(Commands.slash("repeat", "Repeats the currently playing track."));
        addCommandFlags(CommandFlag.MUST_BE_IN_SAME_VC, CommandFlag.DJ_ONLY, CommandFlag.MUST_BE_IN_VC);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        GuildAudioPlayer player = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        if (player.getPlayer().getPlayingTrack() == null) {
            ctx.replyError("I'm currently not playing any music!").queue();
            return;
        }

        if (!player.getScheduler().isRepeat()) {
            player.getScheduler().setRepeat(true);
            event.replyEmbeds(EmbedUtil.defaultEmbed("\uD83D\uDD01 Repeat mode turned **ON**\nUse this command again to turn it off."))
                    .setEphemeral(true)
                    .queue();
        } else {
            player.getScheduler().setRepeat(false);
            event.replyEmbeds(EmbedUtil.defaultEmbed("\uD83D\uDD01 Repeat mode turned **OFF**"))
                    .setEphemeral(true)
                    .queue();
        }

        player.forcePlayerComponentsUpdate();
    }
}
