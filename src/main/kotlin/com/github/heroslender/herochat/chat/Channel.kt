package com.github.heroslender.herochat.chat

import com.github.heroslender.herochat.ComponentParser
import com.github.heroslender.herochat.config.ChannelConfig
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.utils.distanceSquared
import com.github.heroslender.herochat.utils.sendMessage
import com.github.heroslender.herochat.utils.square
import com.hypixel.hytale.server.core.command.system.CommandSender
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe

class Channel(id: String, config: ChannelConfig) {
    val id: String = id
    val name: String = config.name
    val commands: Array<String> = config.commands
    val format: String = config.format
    val permission: String? = config.permission
    val components: Map<String, ComponentConfig> = config.components

    val distance: Double? = config.distance
    val distanceSquared: Double? = distance?.let { square(it) }
    val crossWorld: Boolean = config.crossWorld ?: true

    fun sendMessage(sender: CommandSender, msg: String) {
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(MessagesConfig::channelNoPermission)
            return
        }

        val comp = components + ("message" to ComponentConfig(msg))
        println(format)
        val message = ComponentParser.parse(sender.uuid, format, comp)

        val players: Collection<PlayerRef> = if (crossWorld) {
            Universe.get().players
        } else {
            if (sender !is Player) {
                return
            }

            if (distanceSquared == null) {
                sender.world?.playerRefs ?: emptyList()
            } else {
                val playerRef =
                    sender.reference?.store?.getComponent(sender.reference ?: return, PlayerRef.getComponentType())
                        ?: return
                val position = playerRef.transform.position

                Universe.get()
                    .getWorld(playerRef.worldUuid!!)
                    ?.playerRefs
                    ?.filter {
                        it.transform.position.distanceSquared(
                            position
                        ) <= distanceSquared
                    }
                    ?: emptyList()
            }
        }

        for (player in players) {
            player.sendMessage(message)
        }
    }
}