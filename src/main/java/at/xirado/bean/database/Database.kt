package at.xirado.bean.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import java.sql.Connection

@Single
class Database(
    private val config: DatabaseConfig
) : KoinComponent {
    private val dataSource: HikariDataSource
    val exposed: Database

    init {
        dataSource = connect()
        migrateFlyway()
        exposed = Database.connect(dataSource)
    }

    fun getConnection(): Connection = dataSource.connection

    private fun connect(): HikariDataSource {
        val host = config.host
        val port = config.port
        val database = config.database

        val hikariConfig = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = "jdbc:postgresql://$host:$port/$database"
            username = config.username
            password = config.password
            maximumPoolSize = 10
            addDataSourceProperty("rewriteBatchedInserts", "true")
        }

        return HikariDataSource(hikariConfig)
    }

    private fun migrateFlyway() {
        Flyway
            .configure()
            .dataSource(dataSource)
            .load()
            .migrate()
    }
}