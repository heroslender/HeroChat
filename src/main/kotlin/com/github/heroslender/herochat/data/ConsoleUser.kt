package com.github.heroslender.herochat.data

import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.console.ConsoleSender
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import java.util.*

class ConsoleUser(
    val console: ConsoleSender = ConsoleSender.INSTANCE,
    override var settings: UserSettings = UserSettings(console.uuid),
) : User {
    override val uuid: UUID
        get() = console.uuid
    override val username: String
        get() = console.displayName

    override var lastMessage: String = ""
    override val cooldowns: Object2LongMap<String> = Object2LongOpenHashMap()

    override fun sendMessage(message: Message) {
        console.sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return console.hasPermission(permission)
    }

    override fun distanceSquared(other: User): Double {
        return Double.MAX_VALUE
    }
}