package at.xirado.bean.data.database

import at.xirado.bean.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import java.sql.Connection

class Database(private val config: DatabaseConfig) {
    private val dataSource: HikariDataSource
    val exposed: Database

    init {
        val (source, db) = connect()
        dataSource = source
        exposed = db
        createMissingTables(this)
    }

    private fun connect(): Pair<HikariDataSource, Database> {
        val host = config.host
        val port = config.port
        val dbName = config.database

        val hikariConfig = HikariConfig().apply {
            driverClassName = "org.mariadb.jdbc.Driver"
            jdbcUrl = "jdbc:mariadb://$host:$port/$dbName"
            username = config.username
            password = config.password
            maximumPoolSize = 8
        }

        val dataSource = HikariDataSource(hikariConfig)
        val database = Database.connect(dataSource)

        return dataSource to database
    }

    fun getConnectionFromPool(): Connection = dataSource.connection
}