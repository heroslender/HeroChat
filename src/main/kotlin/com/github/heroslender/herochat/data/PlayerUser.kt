package com.github.heroslender.herochat.data

import com.github.heroslender.herochat.utils.distanceSquared
import com.github.heroslender.herochat.utils.hasPermission
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.universe.PlayerRef
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import java.util.*

class PlayerUser(
    val player: PlayerRef,
    override var settings: UserSettings,
) : User {
    override val uuid: UUID
        get() = player.uuid
    override val username: String
        get() = player.username

    override var lastMessage: String = ""
    override val cooldowns: Object2LongMap<String> = Object2LongOpenHashMap()

    override fun sendMessage(message: Message) {
        player.sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun distanceSquared(other: User): Double {
        if (other !is PlayerUser) return Double.MAX_VALUE

        return player.transform.position.distanceSquared(other.player.transform.position)
    }
}