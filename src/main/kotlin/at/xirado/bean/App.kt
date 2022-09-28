@file:JvmName("Main")
package at.xirado.bean

import at.xirado.bean.audio.AudioManager
import at.xirado.bean.command.LegacyCommandHandler
import at.xirado.bean.data.guild.GuildManager
import at.xirado.bean.data.user.UserManager
import at.xirado.bean.i18n.LocalizationManager
import at.xirado.bean.interaction.InteractionCommandHandler
import at.xirado.bean.interaction.ListenerManager
import at.xirado.bean.io.config.BeanConfiguration
import at.xirado.bean.io.config.FileLoader
import at.xirado.bean.io.db.Database
import at.xirado.bean.listener.*
import ch.qos.logback.classic.Level
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.getDefaultScope
import net.dv8tion.jda.api.GatewayEncoding
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.cache.CacheFlag
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors

val coroutineScope = getDefaultScope()
val executor = Executors.newFixedThreadPool(10)

fun main(args: Array<String>) {
    if ("--noclear" !in args)
        print("\u001b[2J\u001b[H")

    if ("--debug" !in args)
        (LoggerFactory.getLogger("ROOT") as ch.qos.logback.classic.Logger).level = Level.INFO

    Application()
}

class Application {
    val config = BeanConfiguration(FileLoader.loadFileAsYaml("config.yml", true))
    val shardManager: ShardManager
    val httpClient = OkHttpClient()
    val interactionCommandHandler: InteractionCommandHandler
    val legacyCommandHandler: LegacyCommandHandler
    val localizationManager = LocalizationManager()
    val guildManager = GuildManager(this)
    val userManager = UserManager(this)
    val audioManager = AudioManager(this)
    val listenerManager = ListenerManager(this)

    private val listeners: List<CoroutineEventListener> = listOf(
        InteractionListener(this), ReadyListener(this), LevelingListener(this),
        listenerManager, LegacyCommandListener(this), MusicPlayerListener(this)
    )

    init {
        Message.suppressContentIntentWarning()
        val properties = Properties()
        properties.load(Application::class.java.getResourceAsStream("/app.properties"))
        VERSION = if (config.devMode) "DEV" else properties.getProperty("app-version")
        BUILD_DATE = if (config.devMode) 0L else properties.getProperty("build-time").toLong()
        shardManager = DefaultShardManagerBuilder.createDefault(config.discordToken)
            .setEnabledIntents(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES)
            .setShardsTotal(-1)
            .setEventManagerProvider { CoroutineEventManager(coroutineScope) }
            .setActivity(Activity.playing("bean.bz"))
            .enableCache(CacheFlag.VOICE_STATE)
            .setBulkDeleteSplittingEnabled(false)
            .addEventListeners(listeners)
            .setChunkingFilter(ChunkingFilter.NONE)
            .setGatewayEncoding(GatewayEncoding.ETF)
            .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOJI, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.STICKER)
            .build()

        interactionCommandHandler = InteractionCommandHandler(this)
        legacyCommandHandler = LegacyCommandHandler(this)

        Database.connect(config)
    }

    companion object {
        var VERSION: String = "0.0.0"
        var BUILD_DATE: Long = 0
    }
}