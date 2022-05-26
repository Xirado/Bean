package at.xirado.bean.io.db

import at.xirado.bean.coroutineScope
import at.xirado.bean.io.config.BeanConfiguration
import at.xirado.bean.io.config.anyNull
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException

object Database : AutoCloseable {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val isConnected get() = ds != null
    private var ds: HikariDataSource? = null

    @JvmStatic
    val connection: Connection
        get() =
            try {
                val dataSource = ds ?: throw IllegalStateException("HikariDataSource is not available!")
                dataSource.connection
            } catch (e: SQLException) {
                logger.error("Connection is not available!", e)
                throw RuntimeException(e)
            }

    override fun close() {
        ds?.close()
    }

    @JvmStatic
    fun connect(config: BeanConfiguration) {
        coroutineScope.launch {
            if (isConnected) return@launch
            val details = config.dbConfig

            if (details.anyNull("host", "database", "username", "password", "port"))
                throw IllegalStateException("Missing database configuration!")

            val host = details.getString("host")
            val port = details.getInt("port")
            val database = details.getString("database")

            val hikariConfig = HikariConfig().apply {
                driverClassName = "org.mariadb.jdbc.Driver"
                jdbcUrl = "jdbc:mariadb://$host:$port/$database"
                username = details.getString("username")
                password = details.getString("password")
                maximumPoolSize = 10
                addDataSourceProperty("cachePrepStmts", "true")
                addDataSourceProperty("prepStmtCacheSize", "250")
                addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
                addDataSourceProperty("characterEncoding", "utf8")
                addDataSourceProperty("useUnicode", "true")
            }
            ds = HikariDataSource(hikariConfig)
            executeQueries()
        }
    }

    @JvmStatic
    fun awaitReady() {
        while (!isConnected) {
            Thread.onSpinWait()
        }
    }

    private fun executeQueries() {
        val statements = listOf(
            "CREATE TABLE IF NOT EXISTS levels (guild_id BIGINT, user_id BIGINT, xp_total BIGINT, name VARCHAR(256), discriminator VARCHAR(4), avatar VARCHAR(128), PRIMARY KEY(guild_id, user_id)) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS guild_settings (guild_id BIGINT PRIMARY KEY, data JSON CHECK (JSON_VALID(data)))",
            "CREATE TABLE IF NOT EXISTS user_settings (user_id BIGINT PRIMARY KEY, data JSON CHECK (JSON_VALID(data)))",
            "CREATE TABLE IF NOT EXISTS xp_alerts (guild_id BIGINT PRIMARY KEY, mode VARCHAR(128))",
            "CREATE TABLE IF NOT EXISTS wildcard_settings (user_id BIGINT PRIMARY KEY, card VARCHAR(128) NOT NULL, accent INT)",
            "CREATE TABLE IF NOT EXISTS search_queries (user_id BIGINT, searched_at BIGINT, name VARCHAR(256), value VARCHAR(256), playlist BOOL) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS user_balance (guild_id BIGINT, user_id BIGINT, balance BIGINT, PRIMARY KEY(guild_id, user_id))",
            "CREATE TABLE IF NOT EXISTS bookmarks (user_id BIGINT, added_at BIGINT, name VARCHAR(256), value VARCHAR(256), playlist BOOL, PRIMARY KEY(user_id, value)) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS dismissable_contents (user_id BIGINT, identifier VARCHAR(128), state VARCHAR(128), PRIMARY KEY(user_id, identifier))",
            "CREATE TABLE IF NOT EXISTS banned_guilds (guild_id BIGINT PRIMARY KEY, reason VARCHAR(256))"
        )
        connection.use { connection ->
            try {
                for (statement in statements) {
                    connection.prepareStatement(statement).use { it.execute() }
                }
            } catch (e: SQLException) {
                logger.error("Failed to execute the statement", e)
            }
        }
    }
}