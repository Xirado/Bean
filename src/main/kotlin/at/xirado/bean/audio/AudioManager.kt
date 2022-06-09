package at.xirado.bean.audio

import at.xirado.bean.Application
import at.xirado.bean.util.getLog
import at.xirado.bean.util.noneNull
import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifyConfig
import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifySourceManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeHttpContextFilter
import net.dv8tion.jda.api.entities.Guild
import java.util.concurrent.ConcurrentHashMap

private val log = getLog<AudioManager>()

class AudioManager(val application: Application) {
    val playerManager = DefaultAudioPlayerManager()
    val audioPlayers = ConcurrentHashMap<Long, GuildPlayer>()

    init {
        val spotifyConfigObject = application.config.spotifyConfig
        if (spotifyConfigObject.noneNull("client_id", "client_secret")) {
            val id = spotifyConfigObject.getString("client_id")
            val secret = spotifyConfigObject.getString("client_secret")
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
        val youtubeConfig = application.config.ytConfig
        if (youtubeConfig.noneNull("papisid", "psid")) {
            YoutubeHttpContextFilter.setPAPISID(youtubeConfig.getString("papisid"))
            YoutubeHttpContextFilter.setPSID(youtubeConfig.getString("psid"))
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