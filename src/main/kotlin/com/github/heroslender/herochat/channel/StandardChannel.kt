package com.github.heroslender.herochat.channel

import com.github.heroslender.herochat.ComponentParser
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.Permissions
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

class StandardChannel(id: String, config: ChannelConfig) : Channel {
    override val id: String = id
    override val name: String = config.name
    override val commands: Array<String> = config.commands
    val format: String = config.format
    override val permission: String? = config.permission
    val components: Map<String, ComponentConfig> = config.components

    val distance: Double? = config.distance
    val distanceSquared: Double? = distance?.let { square(it) }
    val crossWorld: Boolean = config.crossWorld ?: true

    override fun sendMessage(sender: CommandSender, msg: String) {
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(MessagesConfig::channelNoPermission)
            return
        }

        val settings = HeroChat.instance.userService.getSettings(sender.uuid)
        if (settings.disabledChannels.contains(id)) {
            sender.sendMessage(MessagesConfig::channelDisabled)
            return
        }

        val recipients: List<PlayerRef> = getRecipients(sender) ?: return
        if (recipients.isEmpty()) {
            sender.sendMessage(MessagesConfig::chatNoRecipients)
            return
        }

        var finalMsg = msg
        if (sender.hasPermission(Permissions.SETTINGS_MESSAGE_COLOR)) {
            finalMsg = "${settings.messageColor?.let { "{$it}" }.orEmpty()}$msg"
        }

        val comp = components + ("message" to ComponentConfig(finalMsg))
        val message = ComponentParser.parse(sender.uuid, format, comp)

        for (recipient in recipients) {
            recipient.sendMessage(message)
        }

        val spies = HeroChat.instance.userService.getSpies()
        if (spies.isEmpty()) {
            return
        }

        val spyMessage = Message.empty()
            .insert(Message.raw("[SPY] ").color("#FF5555").bold(true))
            .insert(message)

        for (spyUuid in spies) {
            if (recipients.none { it.uuid == spyUuid }) {
                Universe.get().sendMessage(spyUuid, spyMessage)
            }
        }
    }

    fun getRecipients(sender: CommandSender): List<PlayerRef>? {
        val recipients = ArrayList<PlayerRef>()

        if (crossWorld) {
            recipients.addAll(Universe.get().players)
        } else {
            if (sender !is Player) {
                sender.sendMessage(Message.raw("You can't send messages in this channel! Not a global channel."))
                return null
            }

            val playersInWorld = sender.world?.playerRefs
            if (playersInWorld != null) {
                recipients.addAll(playersInWorld)
            }

            if (distanceSquared != null && recipients.isNotEmpty()) {
                val playerRef = sender.reference?.store?.getComponent(
                    sender.reference ?: return null,
                    PlayerRef.getComponentType()
                ) ?: return null
                val position = playerRef.transform.position

                recipients.removeIf {
                    it.transform.position.distanceSquared(position) > distanceSquared
                }
            }
        }

        val userService = HeroChat.instance.userService
        recipients.removeIf { playerRef ->
            val settings = userService.getSettings(playerRef.uuid)
            return@removeIf settings.disabledChannels.contains(this.id)
        }

        return recipients
    }
}