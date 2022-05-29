package at.xirado.bean.io.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet

private val log = LoggerFactory.getLogger(SQLBuilder::class.java) as Logger

class SQLBuilder(@Language("SQL") val sqlString: String, vararg parameters: Any) {

    private val parameterList = mutableListOf<Any>()

    init {
        parameterList.addAll(parameters)
    }

    fun addParameter(vararg parameters: Any): SQLBuilder {
        parameterList.addAll(parameters)
        return this
    }

    suspend fun <T> executeQuery(result: (ResultSet) -> T?): T? {
        Database.connection.use { connection ->
            connection.prepareStatement(sqlString).use {
                parameterList.forEachIndexed { index, param -> it.setObject(index+1, param) }
                return withContext(Dispatchers.IO) { it.executeQuery().use { result.invoke(it) } }
            }
        }
    }

    suspend fun execute(): Boolean {
        Database.connection.use { connection ->
            connection.prepareStatement(sqlString).use {
                parameterList.forEachIndexed { index, param -> it.setObject(index+1, param) }
                return withContext(Dispatchers.IO) { it.execute() }
            }
        }
    }




}