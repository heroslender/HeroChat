package com.github.heroslender.herochat.channel

import com.github.heroslender.herochat.ComponentParser
import com.github.heroslender.herochat.Permissions
import com.github.heroslender.herochat.config.ChannelConfig
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.data.PlayerUser
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.service.UserService
import com.github.heroslender.herochat.utils.distanceSquared
import com.github.heroslender.herochat.utils.sendMessage
import com.github.heroslender.herochat.utils.square
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe

class StandardChannel(id: String, config: ChannelConfig, private val userService: UserService) : Channel {
    override val id: String = id
    override val name: String = config.name
    override val commands: Array<String> = config.commands
    val format: String = config.format
    override val permission: String? = config.permission
    val components: Map<String, ComponentConfig> = config.components

    val distance: Double? = config.distance
    val distanceSquared: Double? = distance?.let { square(it) }
    val crossWorld: Boolean = config.crossWorld ?: true

    override fun sendMessage(sender: User, msg: String) {
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(MessagesConfig::channelNoPermission)
            return
        }

        val settings = sender.settings
        if (settings.disabledChannels.contains(id)) {
            sender.sendMessage(MessagesConfig::channelDisabled)
            return
        }

        val recipients: List<User> = getRecipients(sender) ?: return
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

        val spies = userService.getSpies()
        if (spies.isEmpty()) {
            return
        }

        val spyMessage = Message.empty()
            .insert(Message.raw("[SPY] ").color("#FF5555").bold(true))
            .insert(message)

        for (spy in spies) {
            if (recipients.none { it.uuid == spy.uuid }) {
                spy.sendMessage(spyMessage)
            }
        }
    }

    fun getRecipients(sender: User): List<User>? {
        val recipients = ArrayList<User>()

        if (crossWorld) {
            recipients.addAll(userService.getUsers())
        } else {
            if (sender !is PlayerUser) {
                sender.sendMessage(Message.raw("You can't send messages in this channel! Not a global channel."))
                return null
            }

            if (distanceSquared == null) {
                recipients.addAll(userService.getUsersInWorld(sender.player.worldUuid ?: return null))
            } else {
                recipients.addAll(userService.getUsersNearby(sender, distanceSquared) ?: return null)
            }
        }

        recipients.removeIf { user ->
            return@removeIf user.settings.disabledChannels.contains(this.id)
        }

        return recipients
    }
}