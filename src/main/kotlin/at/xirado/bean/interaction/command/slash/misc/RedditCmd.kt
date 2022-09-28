package at.xirado.bean.interaction.command.slash.misc

import at.xirado.bean.Application
import at.xirado.bean.interaction.slash.AutoComplete
import at.xirado.bean.interaction.slash.BaseCommand
import at.xirado.bean.interaction.slash.SlashCommand
import at.xirado.bean.io.exception.CommandException
import at.xirado.bean.util.ResponseType
import at.xirado.bean.util.getGuildI18n
import at.xirado.bean.util.getNullable
import at.xirado.simplejson.JSONObject
import at.xirado.simplejson.get
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.util.await
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import okhttp3.Request

class RedditCmd(override val app: Application) : SlashCommand("reddit", "Gets a trending post from a subreddit") {
    private val notAvailable = messageReference("commands.reddit.not_available")
    private val nsfw = messageReference("commands.reddit.nsfw")
    private val source = messageReference("commands.reddit.source")

    private val recommendedSubs = listOf(
        "r/memes", "r/me_irl", "r/ProgrammerHumor", "r/dankmemes", "r/AdviceAnimals", "r/interestingasfuck"
    )

    init {
        options {
            option<String>(name = "subreddit", description = "Which subreddit to search in", autocomplete = true)
        }
    }

    @BaseCommand
    suspend fun execute(event: SlashCommandInteractionEvent, subreddit: String = "memes") {
        event.deferReply().queue()
        val trimmed = subreddit.removePrefix("r/")
        val url = "https://meme-api.herokuapp.com/gimme/$trimmed"

        app.httpClient.newCall(Request.Builder().url(url).get().build()).await().use { response ->
            val json = response.body?.let { JSONObject.fromJson(it.byteStream()) }
                ?: throw CommandException(ResponseType.ERROR, "commands.reddit.not_available")

            if (!response.isSuccessful) {
                val responseMessage = json.getNullable<String>("message") ?: notAvailable
                return event.send(ResponseType.ERROR, responseMessage)
            }

            val isChannelNsfw = (event.channel as? IAgeRestrictedChannel)?.isNSFW ?: false

            if (json.get<Boolean>("nsfw") && !isChannelNsfw)
                return event.send(ResponseType.WARNING, nsfw)

            val locale = with(app) { event.getGuildI18n() }

            val embed = Embed {
                title = json.get<String>("title")
                image = json.get<String>("url")
                description = "[${source.get(locale)}](${json.get<String>("postLink")})"
                footer { name = "r/${json.get<String>("subreddit")}" }
                color = 0x152238
            }
            event.hook.sendMessageEmbeds(embed).queue()
        }
    }

    @AutoComplete("subreddit")
    fun autoComplete(event: CommandAutoCompleteInteractionEvent) {
        event.replyChoiceStrings(
            recommendedSubs.filter { it.startsWith(event.focusedOption.value, ignoreCase = true) }
        ).queue()
    }
}