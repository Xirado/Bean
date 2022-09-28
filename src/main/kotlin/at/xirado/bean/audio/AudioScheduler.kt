package at.xirado.bean.audio

import at.xirado.bean.Application
import at.xirado.bean.util.ColorPalette
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.GuildMessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import java.util.concurrent.LinkedBlockingQueue

class AudioScheduler(val application: Application, val player: AudioPlayer, val guildId: Long, val guildPlayer: GuildPlayer) : AudioEventAdapter() {
    val queue = LinkedBlockingQueue<AudioTrack>()
    var lastTrack: AudioTrack? = null
    var repeat = false
    var shuffle = false

    fun queue(track: AudioTrack) {
        if (player.playingTrack != null)
            queue.offer(track)
        else
            player.playTrack(track)
    }

    fun nextTrack() {
        if (repeat && lastTrack != null)
            return player.playTrack(lastTrack!!.makeClone())

        val track = queue.poll()
        if (track != null)
            player.playTrack(track)
        else
            player.stopTrack()
    }

    suspend fun playerSetup(channel: GuildMessageChannel) {
        channel.sendMessageEmbeds(generateEmbed(player.playingTrack)).await()
    }

    private fun generateEmbed(playing: AudioTrack?): MessageEmbed {
        return Embed {
            title = if (playing == null) "No music playing" else "${playing.info.title} - ${playing.info.author}"
            if (playing != null)
                url = playing.info.uri

            thumbnail = if (playing == null) "https://bean.bz/assets/vinyl.png" else getArtworkUrl(playing)
            color = ColorPalette.PRIMARY.rgb
        }
    }

    @Synchronized
    fun destroy() {
        queue.clear()
        lastTrack = null
        repeat = false
        shuffle = false
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext)
            nextTrack()
    }
}