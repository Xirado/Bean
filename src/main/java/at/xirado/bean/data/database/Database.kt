/*
 * Copyright 2024 Marcel Korzonek and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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