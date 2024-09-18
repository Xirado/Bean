package at.xirado.bean.proxy

import at.xirado.bean.jda.ProxiedGatewayConfig
import net.dv8tion.jda.api.GatewayEncoding
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.Compression
import net.dv8tion.jda.api.utils.SessionController
import net.dv8tion.jda.api.utils.SessionControllerAdapter

class ProxiedGatewayController(
    private val config: ProxiedGatewayConfig,
) : SessionControllerAdapter() {
    override fun getGateway(): String = config.url

    override fun getShardedGateway(api: JDA): SessionController.ShardedGateway {
        return SessionController.ShardedGateway(config.url, config.shardsTotal, config.shardsTotal)
    }
}

fun DefaultShardManagerBuilder.applyProxiedGateway(config: ProxiedGatewayConfig?): DefaultShardManagerBuilder {
    if (config == null || !config.enabled) {
        setGatewayEncoding(GatewayEncoding.ETF)
    } else {
        setCompression(Compression.NONE)
        setSessionController(ProxiedGatewayController(config))
        setShardsTotal(config.shardsTotal)
    }

    return this
}