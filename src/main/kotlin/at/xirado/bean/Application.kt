@file:JvmName("Main")
package at.xirado.bean

import at.xirado.bean.io.config.BeanConfiguration
import at.xirado.bean.io.config.ConfigLoader
import dev.minn.jda.ktx.getDefaultScope
import net.dv8tion.jda.api.GatewayEncoding
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.cache.CacheFlag

fun main() {
    Application()
}

class Application {
    companion object {
        lateinit var instance: Application
    }

    val config: BeanConfiguration = BeanConfiguration(ConfigLoader.loadFileAsYaml("config.yml", true))
    val shardManager: ShardManager
    val coroutineScope = getDefaultScope()

    init {
        instance = this

        shardManager = DefaultShardManagerBuilder.createDefault(config.discordToken)
            .setEnabledIntents(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES)
            .setShardsTotal(-1)
            .setActivity(Activity.playing("bean.bz"))
            .enableCache(CacheFlag.VOICE_STATE)
            .setBulkDeleteSplittingEnabled(false)
            .setChunkingFilter(ChunkingFilter.NONE)
            .setGatewayEncoding(GatewayEncoding.ETF)
            .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
            .build()
    }
}