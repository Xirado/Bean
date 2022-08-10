package at.xirado.bean.interaction.command.slash.music

import at.xirado.bean.Application
import at.xirado.bean.audio.*
import at.xirado.bean.interaction.AutoComplete
import at.xirado.bean.interaction.CommandFlag
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.interaction.components.IAutocompleteChoice
import at.xirado.bean.util.isUrl
import at.xirado.bean.util.replyError
import at.xirado.bean.util.sendErrorMessage
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.minn.jda.ktx.interactions.choice
import dev.minn.jda.ktx.interactions.getOption
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.PermissionException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PlayCommand(override val application: Application) : SlashCommand("play", "Plays a track from YouTube, Soundcloud, Spotify, and more.") {
    init {
        option<String>(name = "query", description = "Search term or URL that is supported", required = true, autocomplete = true)
        option<String>(name = "provider", description = "Provider for searching") {
            choice("Youtube (Default)", "ytsearch:")
            choice("Youtube Music", "ytmsearch:")
            choice("Spotify", "spsearch:")
            choice("SoundCloud", "scsearch:")
        }
        commandFlags.apply {
            add(CommandFlag.USER_MUST_JOIN_VC)
            add(CommandFlag.MUST_JOIN_BOT_VC)
        }
    }

    override suspend fun baseCommand(event: SlashCommandInteractionEvent) {
        val guild = event.guild!!
        val member = event.member!!
        val voiceState = member.voiceState!!

        val channel = voiceState.channel!!

        val audioManager = application.audioManager
        val player = audioManager.getPlayer(guild)
        val playerManager = audioManager.playerManager
        val guildManager = guild.audioManager

        if (!guildManager.isConnected) {
            try {
                guildManager.openAudioConnection(channel)
            } catch (ex: PermissionException) {
                return event.replyError("I do not have permission to join this channel!").queue()
            }
        }

        event.deferReply(true).queue()

        val query = event.getOption<String>("query")!!
        val prefix = event.getOption<String>("provider")?: "ytsearch:"
        val lavaPlayerQuery = if (query.isUrl()) query else prefix + query
        val addedToQueue = player.player.playingTrack != null

        try {
            when (val result = playerManager.loadItemAsync(player, lavaPlayerQuery)) {
                is AudioTrack -> {
                    result.userData = TrackInfo(member.idLong, null)
                    player.scheduler.queue(result)
                    event.hook.sendMessageEmbeds(getPlayConfirmationEmbed(result, addedToQueue)).queue()
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
                        addSearchHistoryEntry(member.idLong, query, query, false)
                    } else {
                        val playlistInfo = PlaylistInfo(result.name, lavaPlayerQuery)
                        result.tracks.forEach {
                            it.userData = TrackInfo(member.idLong, playlistInfo)
                            player.scheduler.queue(it)
                        }
                        event.hook.sendMessageEmbeds(getPlayConfirmationEmbed(result, addedToQueue)).queue()
                        if (lavaPlayerQuery.length <= 100)
                            addSearchHistoryEntry(member.idLong, result.name, lavaPlayerQuery, true)
                    }

                }

                else -> event.sendErrorMessage("Sorry, i haven't found anything!").queue()
            }
        } catch (ex: FriendlyException) {
            event.sendErrorMessage(ex.message?: "An error occurred").queue()
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
        choices.addAll(getYoutubeMusicSearchResults(application, event.focusedOption.value, countryString, languageString).take(25 - choices.size))

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