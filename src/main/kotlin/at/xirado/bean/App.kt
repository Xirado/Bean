@file:JvmName("Main")
package at.xirado.bean

import at.xirado.bean.data.GuildManager
import at.xirado.bean.data.UserManager
import at.xirado.bean.i18n.LocalizationManager
import at.xirado.bean.interaction.InteractionCommandHandler
import at.xirado.bean.io.config.BeanConfiguration
import at.xirado.bean.io.config.FileLoader
import at.xirado.bean.io.db.Database
import at.xirado.bean.listener.InteractionListener
import at.xirado.bean.listener.LevelingListener
import at.xirado.bean.listener.ReadyListener
import ch.qos.logback.classic.Level
import dev.minn.jda.ktx.CoroutineEventManager
import dev.minn.jda.ktx.getDefaultScope
import net.dv8tion.jda.api.GatewayEncoding
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.cache.CacheFlag
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

lateinit var APPLICATION: Application

class Application {
    val config = BeanConfiguration(FileLoader.loadFileAsYaml("config.yml", true))
    val shardManager: ShardManager

    val interactionCommandHandler: InteractionCommandHandler
    val localizationManager = LocalizationManager()
    val guildManager = GuildManager(this)
    val userData = UserManager(this)

    init {
        APPLICATION = this
        val properties = Properties()
        properties.load(Application::class.java.getResourceAsStream("/app.properties"))
        VERSION = properties.getProperty("app-version")
        BUILD_DATE = properties.getProperty("build-time").toLong()
        shardManager = DefaultShardManagerBuilder.createDefault(config.discordToken)
            .setEnabledIntents(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES)
            .setShardsTotal(-1)
            .setEventManagerProvider { CoroutineEventManager(coroutineScope) }
            .setActivity(Activity.playing("bean.bz"))
            .enableCache(CacheFlag.VOICE_STATE)
            .setBulkDeleteSplittingEnabled(false)
            .addEventListeners(InteractionListener(this), ReadyListener(this), LevelingListener(this))
            .setChunkingFilter(ChunkingFilter.NONE)
            .setGatewayEncoding(GatewayEncoding.ETF)
            .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.STICKER)
            .build()

        interactionCommandHandler = InteractionCommandHandler(this)

        Class.forName("at.xirado.bean.i18n.LocalizationManager")

        Database.connect(config)
    }

    companion object {
        var VERSION: String = "0.0.0"
        var BUILD_DATE: Long = 0
    }
}