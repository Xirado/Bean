package at.xirado.bean.misc;

import at.xirado.bean.Bean;
import at.xirado.bean.lavaplayer.SpotifyTrack;
import at.xirado.bean.misc.objects.TrackInfo;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class MusicUtil
{

    private static final Button REWIND = Button.secondary("player_previous", Emoji.fromEmote("previous", 940204537371840522L, false));
    private static final Button PAUSE = Button.secondary("player_play", Emoji.fromEmote("pause", 940204537329877032L, false));
    private static final Button PLAY = Button.secondary("player_play", Emoji.fromEmote("play", 940204537023725579L, false));
    private static final Button SKIP = Button.secondary("player_next", Emoji.fromEmote("next", 940204536713314324L, false));
    private static final Button REPEAT = Button.secondary("player_repeat", Emoji.fromEmote("repeat", 940204537355063346L, false));
    private static final Button SHUFFLE = Button.secondary("player_shuffle", Emoji.fromEmote("shuffle", 940748348137304124L, false));

    public static String getStageTopicString(AudioTrack track)
    {
        if (track == null)
            return "Queue is empty!";
        return "Playing " + track.getInfo().title + (track.getInfo().author == null ? "" : " by " + track.getInfo().author);
    }

    public static MessageEmbed getAddedToQueueMessage(GuildAudioPlayer player, AudioTrack track)
    {
        LavalinkPlayer audioPlayer = player.getPlayer();
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(0x452350);
        if (audioPlayer.getPlayingTrack() == null)
        {
            builder.setDescription("**Now playing** " + Util.titleMarkdown(track) + " (**" + FormatUtil.formatTime(track.getDuration()) + "**)");
        }
        else
        {
            builder.setDescription("**Added** " + Util.titleMarkdown(track) + " **to the queue!** (**" + FormatUtil.formatTime(track.getDuration()) + "**)");
        }
        if (track instanceof YoutubeAudioTrack)
            builder.setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/mqdefault.jpg");
        else if (track instanceof SpotifyTrack spotifyTrack)
            builder.setThumbnail(spotifyTrack.getArtworkURL());
        return builder.build();
    }

    public static MessageEmbed getPlayerEmbed(AudioTrack track)
    {
        if (track == null)
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("No music playing", "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                    .setColor(EmbedUtil.DEFAULT_COLOR)
                    .setDescription(getProgressBar(0)+"\n--:-- / --:--")
                    .setThumbnail("https://bean.bz/img/icons/android-chrome-512x512.png");
            return builder.build();
        }
        TrackInfo info = track.getUserData(TrackInfo.class);
        long position = Bean.getInstance().getLavalink().getExistingLink(info.getGuild()).getPlayer().getTrackPosition();
        if (position < 0)
            position = 0;
        int percentage = (int) ((double)position / (double) track.getDuration() * 100);

        GuildAudioPlayer guildAudioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(info.getGuildId());

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(track.getInfo().title + " - " + track.getInfo().author, track.getInfo().uri)
                .setColor(EmbedUtil.DEFAULT_COLOR);

        if (info.getVoteSkips().size() > 0)
            builder.setFooter(info.getVoteSkips().size() + " vote"+(info.getVoteSkips().size() == 1 ? "" : "s")+" to skip");

        String description = getProgressBar(percentage)+ "\n" + FormatUtil.formatTime(position) + " / " + FormatUtil.formatTime(track.getDuration());
        if (track.getDuration() == Long.MAX_VALUE)
            description = "\uD83D\uDD34 Live";
        Queue<AudioTrack> queue = guildAudioPlayer.getScheduler().getQueue();

        AtomicInteger index = new AtomicInteger();
        String[] tracks = queue.stream()
                .limit(10)
                .map(queuedTrack -> "`" + (index.get() < 9 ? " " : "") + index.incrementAndGet() + "` [" + queuedTrack.getInfo().title + " - " + queuedTrack.getInfo().author + "](" + queuedTrack.getInfo().uri + ")" ).toArray(String[]::new);

        if (tracks.length > 0)
        {
            int length = 0;
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : tracks)
            {
                if (length + s.length()+1 <= MessageEmbed.VALUE_MAX_LENGTH)
                    stringBuilder.append(s).append("\n");

                length += s.length()+1;
            }
            builder.addField("Coming next", stringBuilder.toString(), false);
        }

        builder.setDescription(description);

        if (track instanceof SpotifyTrack spotifyTrack)
            builder.setThumbnail(spotifyTrack.getArtworkURL());
        else if (track instanceof YoutubeAudioTrack)
            builder.setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/mqdefault.jpg");

        return builder.build();
    }

    private static final String BAR = "<:bar_full:940243281604911174>";
    private static final String KNOB_BEGINNING = "<:bar_first:940243281630101574>";
    private static final String KNOB_BEGINNING_SPLIT = "<:bar_first_split:940244099439689828>";
    private static final String KNOB_MIDDLE = "<:bar_middle:940243281592348682>";
    private static final String KNOB_END = "<:bar_last:940243281592324126>";
    private static final String KNOB_END_SPLIT = "<:bar_last_split:940244099112505375>";

    // Ok look I know this looks ugly, but I was told I should do it like that so pls don't judge q_q
    private static final String[] PROGRESS_BARS = {
            KNOB_BEGINNING       + BAR.repeat(11),
            KNOB_MIDDLE          + BAR.repeat(11),
            KNOB_END             + BAR.repeat(11),
            KNOB_END_SPLIT       + KNOB_BEGINNING_SPLIT + BAR.repeat(10),
            BAR                  + KNOB_BEGINNING       + BAR.repeat(10),
            BAR                  + KNOB_MIDDLE          + BAR.repeat(10),
            BAR                  + KNOB_END             + BAR.repeat(10),
            BAR                  + KNOB_END_SPLIT       + KNOB_BEGINNING_SPLIT + BAR.repeat(9),
            BAR.repeat(2)        + KNOB_BEGINNING       + BAR.repeat(9),
            BAR.repeat(2)        + KNOB_MIDDLE          + BAR.repeat(9),
            BAR.repeat(2)        + KNOB_END             + BAR.repeat(9),
            BAR.repeat(2)        + KNOB_END_SPLIT       + KNOB_BEGINNING_SPLIT + BAR.repeat(8),
            BAR.repeat(3)        + KNOB_BEGINNING       + BAR.repeat(8),
            BAR.repeat(3)        + KNOB_MIDDLE          + BAR.repeat(8),
            BAR.repeat(3)        + KNOB_END             + BAR.repeat(8),
            BAR.repeat(3)        + KNOB_END_SPLIT       + KNOB_BEGINNING_SPLIT + BAR.repeat(7),
            BAR.repeat(4)        + KNOB_BEGINNING       + BAR.repeat(7),
            BAR.repeat(4)        + KNOB_MIDDLE          + BAR.repeat(7),
            BAR.repeat(4)        + KNOB_END             + BAR.repeat(7),
            BAR.repeat(4)        + KNOB_END_SPLIT       + KNOB_BEGINNING_SPLIT + BAR.repeat(6),
            BAR.repeat(5)        + KNOB_BEGINNING       + BAR.repeat(6),
            BAR.repeat(5)        + KNOB_MIDDLE          + BAR.repeat(6),
            BAR.repeat(5)        + KNOB_END             + BAR.repeat(6),
            BAR.repeat(5)        + KNOB_END_SPLIT       + KNOB_BEGINNING_SPLIT + BAR.repeat(5),
            BAR.repeat(6)        + KNOB_BEGINNING       + BAR.repeat(5),
            BAR.repeat(6)        + KNOB_MIDDLE          + BAR.repeat(5),
            BAR.repeat(6)        + KNOB_END             + BAR.repeat(5),
            BAR.repeat(6)        + KNOB_END_SPLIT       + KNOB_BEGINNING_SPLIT + BAR.repeat(4),
            BAR.repeat(7)        + KNOB_BEGINNING       + BAR.repeat(4),
            BAR.repeat(7)        + KNOB_MIDDLE          + BAR.repeat(4),
            BAR.repeat(7)        + KNOB_END             + BAR.repeat(4),
            BAR.repeat(7)        + KNOB_END_SPLIT       + KNOB_BEGINNING_SPLIT + BAR.repeat(3),
            BAR.repeat(8)        + KNOB_BEGINNING       + BAR.repeat(3),
            BAR.repeat(8)        + KNOB_MIDDLE          + BAR.repeat(3),
            BAR.repeat(8)        + KNOB_END             + BAR.repeat(3),
            BAR.repeat(8)        + KNOB_END_SPLIT       + KNOB_BEGINNING_SPLIT + BAR.repeat(2),
            BAR.repeat(9)        + KNOB_BEGINNING       + BAR.repeat(2),
            BAR.repeat(9)        + KNOB_MIDDLE          + BAR.repeat(2),
            BAR.repeat(9)        + KNOB_END             + BAR.repeat(2),
            BAR.repeat(9)        + KNOB_END_SPLIT       + KNOB_BEGINNING_SPLIT + BAR,
            BAR.repeat(10)       + KNOB_BEGINNING       + BAR,
            BAR.repeat(10)       + KNOB_MIDDLE          + BAR,
            BAR.repeat(10)       + KNOB_END             + BAR,
            BAR.repeat(10)       + KNOB_END_SPLIT       + KNOB_BEGINNING_SPLIT,
            BAR.repeat(11)       + KNOB_BEGINNING,
            BAR.repeat(11)       + KNOB_MIDDLE,
            BAR.repeat(11)       + KNOB_END
    };

    public static String getProgressBar(int percentage) // from 0 to 100
    {
        return PROGRESS_BARS[(int) Math.min(PROGRESS_BARS.length-1, ((percentage/100d) * PROGRESS_BARS.length))];
    }

    public static ActionRow getPlayerButtons(boolean isPaused, boolean isRepeat, boolean isShuffle)
    {
        return ActionRow.of(REWIND, isPaused ? PLAY : PAUSE, SKIP, isRepeat ? Util.getEnabledButton(REPEAT) : REPEAT, isShuffle ? Util.getEnabledButton(SHUFFLE) : SHUFFLE);
    }

    public static ActionRow getPlayerButtons(GuildAudioPlayer player)
    {
        ActionRow row = ActionRow.of(REWIND, player.getPlayer().isPaused() ? PLAY : PAUSE, SKIP, player.getScheduler().isRepeat() ? Util.getEnabledButton(REPEAT) : REPEAT, player.getScheduler().isShuffle() ? Util.getEnabledButton(SHUFFLE) : SHUFFLE);;
        String channelId = player.getLink().getChannel();
        if (channelId == null)
            return row;
        AudioChannel channel = Bean.getInstance().getShardManager().getVoiceChannelById(channelId);
        if (channel == null)
            return row;
        return Util.getListeningUsers(channel) == 0 ? row.asDisabled() : row;
    }
}
