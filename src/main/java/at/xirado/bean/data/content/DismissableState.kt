package at.xirado.bean.data.content

import at.xirado.bean.data.database.SQLBuilder
import java.sql.SQLException

class DismissableState(val userId: Long, val content: IDismissable<*>, status: Status) {
    var status: Status = status
        private set

    fun update(): DismissableState {
        try {
            SQLBuilder(UPDATE_QUERY, userId, content.identifier, status.name).execute()
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
        return this
    }

    fun updateStatus(status: Status): DismissableState {
        this.status = status
        return this.update()
    }

    companion object {
        private const val UPDATE_QUERY =
            "INSERT INTO dismissable_contents (user_id, identifier, state) values (?,?,?) " +
                    "ON DUPLICATE KEY UPDATE identifier = VALUES(identifier), state = VALUES(state) "
    }
}