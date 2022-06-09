package at.xirado.bean.audio

import at.xirado.bean.interaction.components.SearchHistorySuggestion
import at.xirado.bean.io.db.SQLBuilder
import org.intellij.lang.annotations.Language

// CREATE TABLE IF NOT EXISTS search_queries (user_id BIGINT, searched_at BIGINT, name VARCHAR(256), value VARCHAR(256), playlist BOOL)
suspend fun retrieveSearchHistory(userId: Long, prefix: String? = null): List<SearchHistorySuggestion> {
    return SQLBuilder("SELECT * FROM search_queries WHERE user_id = ?${if (prefix == null) "" else " AND name ILIKE ?"} ORDER BY searched_at DESC LIMIT 25")
        .addParameter(userId).also { if (prefix != null) it.addParameter("$prefix%") }.executeQuery {
            val list = mutableListOf<SearchHistorySuggestion>()
            while (it.next()) {
                val name = it.getString("name")
                val value = it.getString("value")
                val playlist = it.getBoolean("playlist")
                list.add(SearchHistorySuggestion(name, value, playlist))
            }
            return@executeQuery list
        }?: emptyList()
}

@Language("sql")
private const val ADD_SEARCH_HISTORY_ENTRY_SQL = """
    INSERT INTO search_queries (user_id, searched_at, name, value, playlist) values (?,?,?,?,?)
    ON CONFLICT(user_id, value) DO UPDATE SET searched_at = excluded.searched_at
"""

suspend fun addSearchHistoryEntry(userId: Long, name: String, value: String, playlist: Boolean) {
    SQLBuilder(ADD_SEARCH_HISTORY_ENTRY_SQL, userId, System.currentTimeMillis(), name, value, playlist).execute()
}