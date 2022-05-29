package at.xirado.bean.io.db

import at.xirado.bean.coroutineScope
import at.xirado.bean.io.config.BeanConfiguration
import at.xirado.bean.io.config.anyNull
import at.xirado.bean.util.getLog
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.launch
import java.sql.Connection
import java.sql.SQLException

object Database : AutoCloseable {
    private val logger = getLog<Database>()
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
                driverClassName = "org.postgresql.Driver"
                jdbcUrl = "jdbc:postgresql://$host:$port/$database"
                username = details.getString("username")
                password = details.getString("password")
                maximumPoolSize = 10
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
            "CREATE TABLE IF NOT EXISTS experience (guild_id BIGINT NOT NULL, user_id BIGINT NOT NULL, experience BIGINT NOT NULL, name VARCHAR(256), discriminator CHAR(4), avatar VARCHAR(128), PRIMARY KEY(guild_id, user_id))",
            "CREATE TABLE IF NOT EXISTS guild_data (guild_id BIGINT NOT NULL PRIMARY KEY, data JSONB NOT NULL)",
            "CREATE TABLE IF NOT EXISTS user_data (user_id BIGINT NOT NULL PRIMARY KEY, data JSONB NOT NULL)",
            "CREATE TABLE IF NOT EXISTS search_queries (user_id BIGINT, searched_at BIGINT, name VARCHAR(256), value VARCHAR(256), playlist BOOL)",
            "CREATE TABLE IF NOT EXISTS bookmarks (user_id BIGINT, added_at BIGINT, name VARCHAR(256), value VARCHAR(256), playlist BOOL, PRIMARY KEY(user_id, value))",
            "CREATE TABLE IF NOT EXISTS dismissable_contents (user_id BIGINT, identifier VARCHAR(128), state VARCHAR(128), PRIMARY KEY(user_id, identifier))",
        )
        connection.use { connection ->
            try {
                for (statement in statements) {
                    kotlin.runCatching { connection.prepareStatement(statement).use { it.execute() } }.onFailure { logger.error("Failed to run statement \"$statement\"", it) }
                }
            } catch (e: SQLException) {
                logger.error("Failed to execute the statement", e)
            }
        }
    }
}