package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.misc.MusicUtil;
import at.xirado.bean.misc.objects.CachedMessage;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

public class PlayerCommand extends SlashCommand
{

    public PlayerCommand()
    {
        setCommandData(Commands.slash("player", "Music Player Navigation"));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx)
    {
        GuildAudioPlayer player = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());

        if (player.getPlayer().getPlayingTrack() == null)
        {
            event.replyEmbeds(EmbedUtil.defaultEmbed("Nothing is playing at the moment!")).setEphemeral(true).queue();
            return;
        }

        AudioTrack track = player.getPlayer().getPlayingTrack();
        boolean isRepeat = player.getScheduler().isRepeat();
        boolean isPaused = player.getPlayer().isPaused();

        event.reply("<a:Loading:846383295120801792> Loading...")
                .flatMap(InteractionHook::deleteOriginal)
                .flatMap(v -> event.getChannel().sendMessageEmbeds(MusicUtil.getPlayerEmbed(track))
                        .setActionRows(MusicUtil.getPlayerButtons(isPaused, isRepeat))
                )
                .queue(msg -> player.setOpenPlayer(new CachedMessage(msg)));
    }
}
