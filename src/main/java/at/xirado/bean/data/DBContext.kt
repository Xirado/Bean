package at.xirado.bean.data

import at.xirado.bean.coroutines.virtualDispatcher
import at.xirado.bean.database.Database
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Factory
import org.koin.core.component.KoinComponent
import java.sql.Connection

private val log = KotlinLogging.logger { }

@Factory
open class DBContext(private val db: Database) : KoinComponent {

    private val connectionContextHolder = ThreadLocal<Connection?>()

    suspend fun <T> doTransaction(block: suspend Transaction.() -> T) : T {
        return newSuspendedTransaction(db = db.exposed, statement = block)
    }

    suspend fun <T> withConnection(block: suspend Connection.() -> T): T {
        val existingConnection = connectionContextHolder.get()

        return if (existingConnection != null) {
            block(existingConnection)
        } else {
            withContext(virtualDispatcher) {
                log.debug { "Getting connection from pool" }
                db.getConnection().use { connection ->
                    connectionContextHolder.set(connection)
                    try {
                        block(connection)
                    } finally {
                        log.debug { "Dropping connection" }
                        connectionContextHolder.remove() // Cleanup after the outermost block
                    }
                }
            }
        }
    }
}