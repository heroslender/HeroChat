package com.github.heroslender.herochat.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.File
import java.sql.Connection
import java.sql.SQLException

class Database(dataFolder: File) {
    private val dataSource: HikariDataSource

    init {
        val dbFile = File(dataFolder, "database")
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:h2:file:${dbFile.absolutePath};MODE=MySQL"
        config.driverClassName = "org.h2.Driver"
        config.username = "sa"
        config.password = ""
        config.maximumPoolSize = 10
        config.connectionTimeout = 30000

        dataSource = HikariDataSource(config)
        initTables()
    }

    private fun initTables() {
        try {
            connection.use { conn ->
                val stmt = conn.createStatement()
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS user_settings (
                        uuid VARCHAR(36) PRIMARY KEY,
                        focused_channel VARCHAR(64) NOT NULL DEFAULT 'global',
                        focused_target VARCHAR(36),
                        message_color VARCHAR(16),
                        disabled_channels TEXT
                    );
                """.trimIndent())
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    val connection: Connection
        @Throws(SQLException::class)
        get() = dataSource.connection

    fun close() {
        if (!dataSource.isClosed) {
            dataSource.close()
        }
    }
}