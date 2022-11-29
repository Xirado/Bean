package at.xirado.bean.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.Buffer
import java.nio.ByteBuffer

class SendHandler(private val player: AudioPlayer) : AudioSendHandler {
    private val buffer = ByteBuffer.allocate(1024)
    private val frame = MutableAudioFrame()

    init { frame.setBuffer(buffer) }

    override fun canProvide() = player.provide(frame)

    override fun isOpus() = true

    override fun provide20MsAudio(): ByteBuffer {
        (buffer as Buffer).flip()
        return buffer
    }
}