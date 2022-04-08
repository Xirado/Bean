@file:JvmName("Main")
package at.xirado.bean

import at.xirado.bean.interaction.InteractionCommandHandler
import at.xirado.bean.io.config.BeanConfiguration
import at.xirado.bean.io.config.FileLoader
import at.xirado.bean.listener.InteractionCommandListener
import at.xirado.bean.listener.ModalListener
import at.xirado.bean.listener.ReadyListener
import ch.qos.logback.classic.Level
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

fun main(args: Array<String>) {
    if ("--noclear" !in args)
        print("\u001b[2J\u001b[H")

    if ("--debug" in args)
        (LoggerFactory.getLogger("ROOT") as ch.qos.logback.classic.Logger).level = Level.DEBUG

    Application()

}

class Application {
    companion object {
        const val BEAN_COLOR_ACCENT = 0x0FA5ED

        var VERSION: String = "0.0.0"
        var BUILD_DATE: Long = 0
        lateinit var instance: Application
    }

    val config: BeanConfiguration = BeanConfiguration(FileLoader.loadFileAsYaml("config.yml", true))
    val shardManager: ShardManager
    val coroutineScope = getDefaultScope()
    val interactionCommandHandler: InteractionCommandHandler

    init {
        instance = this
        val properties = Properties()
        properties.load(Application::class.java.getResourceAsStream("/app.properties"))
        VERSION = properties.getProperty("app-version")
        BUILD_DATE = properties.getProperty("build-time").toLong()
        shardManager = DefaultShardManagerBuilder.createDefault(config.discordToken)
            .setEnabledIntents(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES)
            .setShardsTotal(-1)
            .setActivity(Activity.playing("bean.bz"))
            .enableCache(CacheFlag.VOICE_STATE)
            .setBulkDeleteSplittingEnabled(false)
            .setChunkingFilter(ChunkingFilter.NONE)
            .setGatewayEncoding(GatewayEncoding.ETF)
            .addEventListeners(ReadyListener(this), InteractionCommandListener(this), ModalListener(this))
            .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
            .build()

        interactionCommandHandler = InteractionCommandHandler(this)
        Class.forName("at.xirado.bean.i18n.LocalizationManager")
    }

    private fun loadProperties() {

    }
}