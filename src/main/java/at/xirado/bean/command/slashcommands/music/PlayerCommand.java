package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.lavaplayer.SpotifyTrack;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

public class PlayerCommand extends SlashCommand
{

    public PlayerCommand()
    {
        setCommandData(Commands.slash("player", "Music Player Navigation"));
    }

    private static final Button REWIND = Button.secondary("rewind", "⏪");
    private static final Button PAUSE = Button.secondary("pause",   "⏸");
    private static final Button PLAY = Button.secondary("pause",    "▶");
    private static final Button SKIP = Button.secondary("skip",     "⏭");
    private static final Button REPEAT = Button.secondary("repeat", "\uD83D\uDD02");

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx)
    {
        GuildAudioPlayer player = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());

        if (player.getPlayer().getPlayingTrack() == null)
        {
            event.replyEmbeds(EmbedUtil.defaultEmbed("The queue is empty!")).setEphemeral(true).queue();
            return;
        }

        AudioTrack track = player.getPlayer().getPlayingTrack();
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(track.getInfo().title);
        if (track instanceof SpotifyTrack spotifyTrack)
            builder.setThumbnail(spotifyTrack.getArtworkURL());
        else if (track instanceof YoutubeAudioTrack)
            builder.setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/mqdefault.jpg");
        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
        player.getOpenPlayers().add(event.getHook());
    }
}
