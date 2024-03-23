package at.xirado.bean.data.database

import at.xirado.bean.Bean
import at.xirado.bean.data.database.entity.DiscordOAuthSession.Companion.transform
import at.xirado.bean.data.database.table.DiscordGuilds
import at.xirado.bean.data.database.table.DiscordOAuthSessions
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.sql.SQLException
import kotlin.reflect.KMutableProperty0

private val log = LoggerFactory.getLogger(at.xirado.bean.data.database.Database::class.java)

fun createMissingTables(db: at.xirado.bean.data.database.Database) {
    transaction(db.exposed) {
        SchemaUtils.createMissingTablesAndColumns(DiscordOAuthSessions, DiscordGuilds)
    }

    val sqlStatementsFile = Bean::class.java.getResourceAsStream("/init.sql")?.readAllBytes()?.toString(StandardCharsets.UTF_8)
        ?: throw IllegalStateException("init.sql not found")

    val sqlStatements = sqlStatementsFile.split(';').map(String::trim).filterNot(String::isBlank)

    db.getConnectionFromPool().use {
        sqlStatements.forEach { statement ->
            try {
                it.prepareStatement(statement).use { it.execute() }
            } catch (e: SQLException) {
                log.error("Failed to run SQL statement", e)
            }
        }
    }
}

class TransactionContext<E : Entity<*>>(private val entity: E) {
    inline operator fun E.invoke(block: E.() -> Unit) {
        block()
    }

    fun <T> set(property: KMutableProperty0<T>, new: T) {
        property.set(new)
    }

    fun <T> update(property: KMutableProperty0<T>, block: T.() -> Unit) {
        val obj = property.get()
        block(obj)
        property.set(obj)
    }
}

suspend inline fun <E : Entity<*>> E.withTransaction(crossinline block: context(E) TransactionContext<E>.() -> Unit) {
    val entity = this
    newSuspendedTransaction {
        val context = TransactionContext(entity)
        entity.block(context)
    }
}

fun <T> Column<List<T>>.asSet() = transform({ it.toList() }, { it.toSet() })