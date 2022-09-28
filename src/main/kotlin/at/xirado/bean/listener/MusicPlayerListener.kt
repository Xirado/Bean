package at.xirado.bean.listener

import at.xirado.bean.Application
import at.xirado.bean.util.await
import dev.minn.jda.ktx.events.CoroutineEventListener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.AudioChannel
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import kotlin.time.Duration.Companion.minutes

class MusicPlayerListener(val app: Application) : CoroutineEventListener {
    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is GuildVoiceLeaveEvent -> onVoiceLeave(event)
            is GuildVoiceMoveEvent -> onVoiceMove(event)
        }
    }

    private fun onBotKick(guild: Guild) {
        destroy(guild)
    }

    private suspend fun onBotMoved(newChannel: AudioChannel) {
        if (newChannel.members.size > 1)
            return

        wait(newChannel, false)
    }

    private suspend fun onVoiceLeave(event: GuildVoiceLeaveEvent) {
        if (event.member == event.guild.selfMember)
            return onBotKick(event.guild)
        if (event.guild.selfMember !in event.channelLeft.members)
            return

        if (event.channelLeft.members.size == 1)
            wait(event.channelLeft, true)
    }

    private suspend fun onVoiceMove(event: GuildVoiceMoveEvent) {
        if (event.member == event.guild.selfMember)
            return onBotMoved(event.channelJoined)
        if (event.guild.selfMember !in event.channelLeft.members)
            return

        if (event.channelLeft.members.size == 1)
            wait(event.channelLeft, true)
    }

    private suspend fun wait(channel: AudioChannel, stopIfNothingPlaying: Boolean) {
        val guild = channel.guild
        val player = app.audioManager.getPlayer(guild)
        if (stopIfNothingPlaying && !isPlaying(guild))
            return guild.audioManager.closeAudioConnection()

        player.player.isPaused = true
        checkForJoin(channel.jda, channel)
            ?: return guild.audioManager.closeAudioConnection()
        resume(guild)
    }

    private fun isPlaying(guild: Guild): Boolean {
        val player = app.audioManager.getPlayer(guild)

        return player.player.playingTrack != null
    }

    private fun resume(guild: Guild) {
        val player = app.audioManager.getPlayer(guild)
        player.player.isPaused = false
    }

    private fun destroy(guild: Guild) {
        val player = app.audioManager.getPlayer(guild)

        player.destroy()
        guild.audioManager.closeAudioConnection()
    }

    private suspend fun checkForJoin(jda: JDA, channel: AudioChannel): GenericGuildVoiceEvent? {
        return jda.await(1.minutes) {
            when (it) {
                is GuildVoiceJoinEvent -> it.channelJoined == channel
                is GuildVoiceMoveEvent -> it.channelJoined == channel
                else -> false
            }
        }
    }
}