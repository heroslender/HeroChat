package com.github.heroslender.herochat.chat

import com.github.heroslender.herochat.ComponentParser
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.config.ChannelConfig
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.utils.distanceSquared
import com.github.heroslender.herochat.utils.square
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe

class Channel(id: String, config: ChannelConfig) {
    val id: String = id
    val name: String = config.name
    val format: String = config.format
    val components: Map<String, ComponentConfig> = HeroChat.instance.config.components

    val distance: Double? = config.distance
    val distanceSquared: Double? = distance?.let { square(it) }
    val crossWorld: Boolean = config.crossWorld ?: true

    fun sendMessage(playerRef: PlayerRef, msg: String) {
        val comp = components + ("message" to ComponentConfig(msg))
        println(format)
        val message = ComponentParser.parse(playerRef, format, comp)

        val players: Collection<PlayerRef> = if (crossWorld) {
            Universe.get().players
        } else if (distanceSquared == null) {
            Universe.get().getWorld(playerRef.worldUuid!!)?.playerRefs ?: emptyList()
        } else {
            val position = playerRef.transform.position
            Universe.get()
                .getWorld(playerRef.worldUuid!!)?.playerRefs?.filter { it.transform.position.distanceSquared(position) <= distanceSquared }
                ?: emptyList()
        }

        for (player in players) {
            player.sendMessage(message)
        }
    }
}