package com.github.heroslender.herochat.data

import com.hypixel.hytale.server.core.Message
import java.util.UUID

interface User {
    val uuid: UUID
    val username: String
    var settings: UserSettings
    var lastMessageTime: Long

    fun sendMessage(message: Message)

    fun hasPermission(permission: String): Boolean

    fun distanceSquared(other: User):  Double
}