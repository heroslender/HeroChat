package com.github.heroslender.herochat.data

import com.hypixel.hytale.server.core.Message
import it.unimi.dsi.fastutil.objects.Object2LongMap
import java.util.UUID

interface User {
    val uuid: UUID
    val username: String
    var settings: UserSettings

    var lastMessage: String
    val cooldowns: Object2LongMap<String>

    fun sendMessage(message: Message)

    fun hasPermission(permission: String): Boolean

    fun distanceSquared(other: User):  Double
}