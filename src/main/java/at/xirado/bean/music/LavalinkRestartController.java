package at.xirado.bean.music;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.objects.TrackInfo;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.LavalinkUtil;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class LavalinkRestartController {

    static {
        Path path = Path.of("players");
        try {
            if (!Files.exists(path))
                Files.createDirectory(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void storeData(GuildAudioPlayer player) {
        DataObject toStore = player.toJson();

        if (player.getPlayer().getPlayingTrack() == null)
            return;

        Path path = Path.of("players", player.getGuildId() + ".json");

        try {
            Files.writeString(path, toStore.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resumeSession(Guild guild) {
        Path path = Path.of("players", guild.getIdLong() + ".json");

        if (!path.toFile().exists())
            return;

        try {
            byte[] content = Files.readAllBytes(path);
            DataObject object = DataObject.fromJson(content);

            Files.delete(path);

            GuildAudioPlayer player = Bean.getInstance().getAudioManager().getAudioPlayer(guild.getIdLong());

            AudioChannel channel = guild.getChannelById(AudioChannel.class, object.getLong("channel_id"));

            if (channel == null)
                return;

            player.getLink().connect(channel);

            AudioTrack currentTrack = toAudioTrack(object.getObject("playing_track"));
            currentTrack.setPosition(object.getLong("position"));

            player.getPlayer().playTrack(currentTrack);
            object.getArray("tracks").stream(DataArray::getObject).forEach(track -> player.getScheduler().queue(toAudioTrack(track)));

            player.getScheduler().setShuffle(object.getBoolean("shuffle"));
            player.getScheduler().setRepeat(object.getBoolean("repeat"));

            object.getArray("history").stream(DataArray::getObject)
                    .map(LavalinkRestartController::toAudioTrack)
                    .forEach(track -> player.getScheduler().addLastTrack(track));

            if (!object.isNull("player_channel_id")) {
                GuildMessageChannel playerChannel = guild.getChannelById(GuildMessageChannel.class, object.getLong("player_channel_id"));
                if (playerChannel != null)
                    player.playerSetup(playerChannel, null, null);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static DataObject toJson(AudioTrack track) {
        try {
            return DataObject.empty()
                    .put("track", LavalinkUtil.toMessage(track))
                    .put("user_data", track.getUserData(TrackInfo.class).toData());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static AudioTrack toAudioTrack(DataObject json) {
        try {
            AudioTrack track = LavalinkUtil.toAudioTrack(json.getString("track"));
            track.setUserData(TrackInfo.fromData(json.getObject("user_data")));
            return track;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
