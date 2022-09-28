package at.xirado.bean.interaction.command.slash.music

import at.xirado.bean.Application
import at.xirado.bean.audio.*
import at.xirado.bean.interaction.components.IAutocompleteChoice
import at.xirado.bean.interaction.slash.AutoComplete
import at.xirado.bean.interaction.slash.BaseCommand
import at.xirado.bean.interaction.slash.SlashCommand
import at.xirado.bean.util.ResponseType
import at.xirado.bean.util.isUrl
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.minn.jda.ktx.interactions.commands.choice
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.PermissionException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PlayCommand(override val app: Application) : SlashCommand("play") {
    override var description = "Plays a track from YouTube, Soundcloud, Spotify, and more."

    private val errorOccurred = messageReference("general.unknown_error_occurred")
    private val noPermissions = messageReference("commands.play.no_permissions")
    private val noMatches = messageReference("commands.play.no_matches")

    init {
        options {
            option<String>(name = "query", description = "Search term or URL that is supported", required = true, autocomplete = true)
            option<String>(name = "provider", description = "Provider for searching") {
                choice("Youtube (Default)", "ytsearch:")
                choice("Youtube Music", "ytmsearch:")
                choice("Spotify", "spsearch:")
                choice("SoundCloud", "scsearch:")
            }
        }
    }

    @BaseCommand
    suspend fun execute(event: SlashCommandInteractionEvent, query: String, provider: String? = "ytsearch:") {
        val guild = event.guild!!
        val member = event.member!!
        val voiceState = member.voiceState!!

        val channel = voiceState.channel!!

        val audioManager = app.audioManager
        val player = audioManager.getPlayer(guild)
        val playerManager = audioManager.playerManager
        val guildManager = guild.audioManager

        if (!guildManager.isConnected) {
            try {
                guildManager.openAudioConnection(channel)
            } catch (ex: PermissionException) {
                return event.send(ResponseType.ERROR, noPermissions, ephemeral = true)
            }
        }

        event.deferReply(true).queue()

        val lavaPlayerQuery = if (query.isUrl()) query else provider + query
        val addedToQueue = player.player.playingTrack != null

        try {
            when (val result = playerManager.loadItemAsync(player, lavaPlayerQuery)) {
                is AudioTrack -> {
                    result.userData = TrackInfo(member.idLong, null)
                    player.scheduler.queue(result)
                    event.hook.sendMessageEmbeds(getPlayConfirmationEmbed(result, addedToQueue)).queue()
                    player.scheduler.playerSetup(event.channel.asGuildMessageChannel())
                    if (lavaPlayerQuery.length <= 100) {
                        val title = "${result.info.title} - ${result.info.author}"
                        addSearchHistoryEntry(member.idLong, title, lavaPlayerQuery, false)
                    }
                }

                is AudioPlaylist -> {
                    if (result.isSearchResult) {
                        val track = result.tracks[0]
                        track.userData = TrackInfo(member.idLong, null)
                        player.scheduler.queue(track)
                        event.hook.sendMessageEmbeds(getPlayConfirmationEmbed(track, addedToQueue)).queue()
                        player.scheduler.playerSetup(event.channel.asGuildMessageChannel())
                        addSearchHistoryEntry(member.idLong, query, query, false)
                    } else {
                        val playlistInfo = PlaylistInfo(result.name, lavaPlayerQuery)
                        result.tracks.forEach {
                            it.userData = TrackInfo(member.idLong, playlistInfo)
                            player.scheduler.queue(it)
                        }
                        event.hook.sendMessageEmbeds(getPlayConfirmationEmbed(result, addedToQueue)).queue()
                        player.scheduler.playerSetup(event.channel.asGuildMessageChannel())
                        if (lavaPlayerQuery.length <= 100)
                            addSearchHistoryEntry(member.idLong, result.name, lavaPlayerQuery, true)
                    }

                }

                else -> event.send(ResponseType.ERROR, noMatches, ephemeral = true)
            }
        } catch (ex: FriendlyException) {
            event.send(ResponseType.ERROR, errorOccurred, ephemeral = true)
        }
    }

    @AutoComplete(option = "query")
    suspend fun queryAutocomplete(event: CommandAutoCompleteInteractionEvent) {
        if (event.focusedOption.value.isEmpty())
            return event.replyChoices(
                retrieveSearchHistory(event.user.idLong, null).map { it.toChoice() }
            ).queue()

        if (event.focusedOption.value.isUrl())
            return event.replyChoices(emptyList()).queue()

        val userLocale = event.userLocale

        val languageString = if (userLocale.locale.contains('-')) userLocale.locale.split('-')[0] else userLocale.locale
        val countryString = if (userLocale.locale.contains('-')) userLocale.locale.split('-')[1] else localeMap[languageString]?: "US"
        val choices = mutableListOf<IAutocompleteChoice>()

        choices.addAll(retrieveSearchHistory(event.user.idLong, event.focusedOption.value))
        choices.addAll(getYoutubeMusicSearchResults(app, event.focusedOption.value, countryString, languageString).take(25 - choices.size))

        event.replyChoices(
            choices.map { it.toChoice() }
        ).queue()
    }
}

private suspend fun AudioPlayerManager.loadItemAsync(orderingKey: Any, identifier: String) : AudioItem? = suspendCoroutine { continuation ->
    loadItemOrdered(orderingKey, identifier, object : AudioLoadResultHandler {
        override fun trackLoaded(track: AudioTrack) = continuation.resume(track)
        override fun playlistLoaded(playlist: AudioPlaylist) = continuation.resume(playlist)
        override fun noMatches() = continuation.resume(null)
        override fun loadFailed(exception: FriendlyException) = continuation.resumeWithException(exception)
    })
}

/**
 * Translates ISO 639-1 languages to ISO 3166-2 countries. (The ones that discord supports)
 */
private val localeMap = mapOf(
    "da" to "DK",
    "de" to "DE",
    "fr" to "FR",
    "hr" to "HR",
    "it" to "IT",
    "lt" to "LT",
    "hu" to "HU",
    "nl" to "NL",
    "no" to "NO",
    "pl" to "PL",
    "ro" to "RO",
    "fi" to "FI",
    "vi" to "VN",
    "tr" to "TR",
    "cs" to "CZ",
    "el" to "GR",
    "bg" to "BG",
    "ru" to "RU",
    "uk" to "UA",
    "hi" to "IN",
    "th" to "TH",
    "ja" to "JP",
    "ko" to "KR"
)