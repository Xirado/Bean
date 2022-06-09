package at.xirado.bean.audio

import at.xirado.bean.util.ColorPalette
import com.github.topislavalinkplugins.topissourcemanagers.ISRCAudioTrack
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.minn.jda.ktx.Embed
import net.dv8tion.jda.api.entities.MessageEmbed
import kotlin.math.roundToLong

fun titleMarkdown(track: AudioTrack, short: Boolean = false) =
    if (short)
        "[${track.info.title} - ${track.info.author}](${track.info.uri})"
    else
        "[${track.info.title}](${track.info.uri}) by **${track.info.author}**"

fun getPlayConfirmationEmbed(audioItem: AudioItem, addedToQueue: Boolean): MessageEmbed {
    val description = when (audioItem) {
        is AudioTrack -> {
            val title = audioItem.info.title
            val uri = audioItem.info.uri
            val author = audioItem.info.author
            val duration = formatDuration(audioItem.duration)
            "[$title]($uri) by **$author** ($duration)"
        }
        is AudioPlaylist -> {
            val duration = calculateDuration(audioItem)
            val data = audioItem.tracks[0].getUserData(TrackInfo::class.java)
            val playlistInfo = data.playlistInfo
            if (playlistInfo == null)
                "No description found"
            else
                "[${playlistInfo.name}](${playlistInfo.url}) ($duration)"
        }
        else -> "No description found"
    }

    val thumbnail = when (audioItem) {
        is AudioTrack -> getArtworkUrl(audioItem)
        is AudioPlaylist -> getArtworkUrl(audioItem.tracks[0])
        else -> null
    }

    return Embed {
        this.color = ColorPalette.PRIMARY.rgb
        this.description = description
        this.thumbnail = thumbnail
        this.author {
            name = if (addedToQueue) "Added to queue" else "Now playing"
        }

        if (audioItem is AudioPlaylist)
            this.footer(name = audioItem.name)
    }
}

private fun getArtworkUrl(track: AudioTrack) = when (track) {
    is YoutubeAudioTrack -> "https://img.youtube.com/vi/${track.info.identifier}/maxresdefault.jpg"
    is ISRCAudioTrack -> track.artworkURL
    else -> null
}

private fun calculateDuration(playlist: AudioPlaylist) = formatDuration(playlist.tracks.sumOf { it.duration })

private fun formatDuration(duration: Long): String {
    if (duration == Long.MAX_VALUE)
        return "LIVE"

    var seconds = (duration / 1000.0).roundToLong()
    val hours = seconds / 3600
    seconds %= 3600L
    val minutes = seconds / 60L
    seconds %= 60L
    return (if (hours > 0L) "$hours:" else "") + (if (minutes < 10L) "0$minutes" else minutes) + ":" + if (seconds < 10L) "0$seconds" else seconds
}