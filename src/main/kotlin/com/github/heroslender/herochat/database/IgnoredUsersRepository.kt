package com.github.heroslender.herochat.database

import java.sql.SQLException
import java.util.UUID

class IgnoredUsersRepository(private val database: Database) {

    fun addIgnoredUser(ignoringUuid: UUID, ignoredUuid: UUID) {
        val query = "INSERT INTO ignored_users (ignoring_uuid, ignored_uuid) VALUES (?, ?)"
        try {
            database.connection.use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, ignoringUuid.toString())
                    stmt.setString(2, ignoredUuid.toString())
                    stmt.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun removeIgnoredUser(ignoringUuid: UUID, ignoredUuid: UUID) {
        val query = "DELETE FROM ignored_users WHERE ignoring_uuid = ? AND ignored_uuid = ?"
        try {
            database.connection.use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, ignoringUuid.toString())
                    stmt.setString(2, ignoredUuid.toString())
                    stmt.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun getIgnoredUsers(ignoringUuid: UUID): MutableSet<UUID> {
        val ignoredUsers = mutableSetOf<UUID>()
        val query = "SELECT ignored_uuid FROM ignored_users WHERE ignoring_uuid = ?"
        try {
            database.connection.use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, ignoringUuid.toString())
                    val rs = stmt.executeQuery()
                    while (rs.next()) {
                        ignoredUsers.add(UUID.fromString(rs.getString("ignored_uuid")))
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return ignoredUsers
    }
}