package at.xirado.bean.audio

import at.xirado.bean.Application
import at.xirado.bean.util.getLog
import at.xirado.bean.util.noneNull
import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifyConfig
import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifySourceManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import net.dv8tion.jda.api.entities.Guild
import java.util.concurrent.ConcurrentHashMap

private val log = getLog<AudioManager>()

class AudioManager(val application: Application) {
    val playerManager = DefaultAudioPlayerManager()
    val audioPlayers = ConcurrentHashMap<Long, GuildPlayer>()

    init {
        val config = application.config.spotifyConfig
        if (config.noneNull("client_id", "client_secret")) {
            val id = config.getString("client_id")
            val secret = config.getString("client_secret")
            val spotifyConfig = SpotifyConfig().apply {
                clientId = id
                clientSecret = secret
                setCountryCode("US")
            }
            playerManager.registerSourceManager(SpotifySourceManager(null, spotifyConfig, playerManager))
            log.info("Registered SpotifySourceManager")
        } else {
            log.warn("Could not register SpotifySourceManager because of missing credentials!")
        }
        AudioSourceManagers.registerRemoteSources(playerManager)
    }

    @Synchronized
    fun isLoaded(guildId: Long) = audioPlayers.containsKey(guildId)

    @Synchronized
    fun getPlayer(guild: Guild) = audioPlayers.computeIfAbsent(guild.idLong) { id ->
        val player = GuildPlayer(application, id, playerManager)
        guild.audioManager.sendingHandler = player.sendHandler
        return@computeIfAbsent player
    }

}