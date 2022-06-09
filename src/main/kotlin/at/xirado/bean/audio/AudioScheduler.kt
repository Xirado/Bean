package at.xirado.bean.audio

import at.xirado.bean.Application
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
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

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext)
            nextTrack()
    }

}