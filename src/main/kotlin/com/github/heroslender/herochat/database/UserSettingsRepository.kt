package com.github.heroslender.herochat.database

import com.github.heroslender.herochat.data.UserSettings
import java.sql.SQLException
import java.util.*

class UserSettingsRepository(private val database: Database) {

    fun load(uuid: UUID): UserSettings {
        val query = "SELECT * FROM user_settings WHERE uuid = ?"
        try {
            database.connection.use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, uuid.toString())
                    val rs = stmt.executeQuery()
                    if (rs.next()) {
                        val focusedChannel = rs.getString("focused_channel")
                        val focusedTargetStr = rs.getString("focused_target")
                        val messageColor = rs.getString("message_color")
                        val disabledChannelsStr = rs.getString("disabled_channels")
                        val spyMode = rs.getBoolean("spy_mode")
                        val nickname = rs.getString("nickname")

                        val focusedTarget = if (focusedTargetStr != null) UUID.fromString(focusedTargetStr) else null
                        val disabledChannels = if (disabledChannelsStr != null && disabledChannelsStr.isNotEmpty()) {
                            disabledChannelsStr.split(",").toMutableSet()
                        } else {
                            mutableSetOf()
                        }

                        return UserSettings(
                            uuid,
                            focusedChannel,
                            focusedTarget,
                            messageColor,
                            disabledChannels,
                            spyMode,
                            nickname
                        )
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return UserSettings(uuid)
    }

    fun save(settings: UserSettings) {
        val query = """
            MERGE INTO user_settings (uuid, focused_channel, focused_target, message_color, disabled_channels, spy_mode, nickname) 
            KEY (uuid) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        try {
            database.connection.use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, settings.uuid.toString())
                    stmt.setString(2, settings.focusedChannelId)
                    stmt.setString(3, settings.focusedPrivateTarget?.toString())
                    stmt.setString(4, settings.messageColor)
                    stmt.setString(5, settings.disabledChannels.joinToString(","))
                    stmt.setBoolean(6, settings.spyMode)
                    stmt.setString(7, settings.nickname)
                    stmt.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}