package com.github.heroslender.herochat.chat

import com.github.heroslender.herochat.ComponentParser
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.config.ChannelConfig
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.utils.distanceSquared
import com.github.heroslender.herochat.utils.sendMessage
import com.github.heroslender.herochat.utils.square
import com.hypixel.hytale.server.core.Message
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

        val recipients: Collection<PlayerRef> = if (crossWorld) {
            Universe.get().players
        } else {
            if (sender !is Player) {
                sender.sendMessage(Message.raw("You can't send messages in this channel! Not a global channel."))
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

        var finalMsg = msg
        val settings = HeroChat.instance.userService.getSettings(sender.uuid)
        if (!settings.messageColor.isNullOrEmpty()) {
            finalMsg = settings.messageColor + msg
        }

        val comp = components + ("message" to ComponentConfig(finalMsg))
        val message = ComponentParser.parse(sender.uuid, format, comp)

        val actualRecipients = mutableSetOf<PlayerRef>()
        for (recipient in recipients) {
            val settings = HeroChat.instance.userService.getSettings(recipient.uuid)
            if (!settings.disabledChannels.contains(this.id)) {
                recipient.sendMessage(message)
                actualRecipients.add(recipient)
            }
        }

        val spies = HeroChat.instance.userService.getSpies()
        if (spies.isEmpty())
            return

        val spyMessage = Message.empty()
            .insert(Message.raw("[SPY] ").color("#FF5555").bold(true))
            .insert(message)

        for (spyUuid in spies) {
            if (actualRecipients.none { it.uuid == spyUuid }) {
                Universe.get().sendMessage(spyUuid, spyMessage)
            }
        }
    }
}