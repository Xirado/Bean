package at.xirado.bean.data.database

import at.xirado.bean.data.database.table.DiscordOAuthSessions
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.reflect.KMutableProperty0

fun connectExposed(source: HikariDataSource): Database =
    Database.connect(source)

fun createMissingTables() {
    transaction {
        SchemaUtils.createMissingTablesAndColumns(DiscordOAuthSessions)
    }
}

class TransactionContext<E : Entity<*>>(private val entity: E) {
    inline operator fun E.invoke(block: E.() -> Unit) {
        block()
    }

    suspend fun <T> set(property: KMutableProperty0<T>, new: T) {
        val old = property.get()
        property.set(new)
    }

    suspend fun <T> update(property: KMutableProperty0<T>, block: T.() -> Unit) {
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