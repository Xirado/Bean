package at.xirado.bean.audio

import at.xirado.bean.Application
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager

class GuildPlayer(val application: Application, val guildId: Long, manager: AudioPlayerManager) {
    val player = manager.createPlayer()
    val sendHandler = AudioPlayerSendHandler(player)
    val scheduler = AudioScheduler(application, player, guildId, this).also { player.addListener(it) }
}