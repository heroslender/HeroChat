package com.github.heroslender.herochat.channel

import com.github.heroslender.herochat.Permissions
import com.github.heroslender.herochat.config.ChannelConfig
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.data.PlayerUser
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.event.ChannelChatEvent
import com.github.heroslender.herochat.event.PreChatEvent
import com.github.heroslender.herochat.message.ColorParser
import com.github.heroslender.herochat.message.MessageParser
import com.github.heroslender.herochat.service.UserService
import com.github.heroslender.herochat.utils.runInWorld
import com.github.heroslender.herochat.utils.sendMessage
import com.github.heroslender.herochat.utils.square
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.Message

class StandardChannel(
    id: String,
    config: ChannelConfig,
    private val userService: UserService
) : Channel {
    override val id: String = id
    override val name: String = config.name
    override val commands: Array<String> = config.commands
    val format: String = config.format
    override val permission: String? = config.permission
    override val capslockFilter: CapslockFilter = CapslockFilter(config.capslockFilter)
    val cooldowns: Map<String, Long> = config.cooldowns
    val components: Map<String, ComponentConfig> = config.components

    val distance: Double? = config.distance
    val distanceSquared: Double? = distance?.let { square(it) }
    val crossWorld: Boolean = config.crossWorld ?: true

    override fun sendMessage(sender: User, msg: String) {
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(MessagesConfig::channelNoPermission)
            return
        }

        HytaleServer.get()
            .eventBus
            .dispatchForAsync(PreChatEvent::class.java)
            .dispatch(
                PreChatEvent(
                    sender = sender,
                    channel = this,
                    message = msg
                )
            ).whenComplete(::onPreChatEvent)
    }

    fun onPreChatEvent(event: PreChatEvent, throwable: Throwable?) {
        if (throwable != null) {
            throwable.printStackTrace()
            return
        }

        if (event.isCancelled) {
            return
        }

        val recipients: MutableList<User> = getRecipients(event.sender) ?: return

        HytaleServer.get()
            .eventBus
            .dispatchForAsync(ChannelChatEvent::class.java)
            .dispatch(
                ChannelChatEvent(
                    sender = event.sender,
                    channel = this,
                    message = event.message,
                    recipients = recipients
                )
            ).whenComplete(::onChatEvent)
    }

    fun onChatEvent(event: ChannelChatEvent, throwable: Throwable?) {
        if (throwable != null) {
            throwable.printStackTrace()
            return
        }

        if (event.isCancelled) {
            return
        }

        val message = ColorParser.validateFormat(
            user = event.sender,
            message = event.message,
            basePermission = Permissions.CHAT_MESSAGE_STYLE,
        )
        val comp = components + ("message" to ComponentConfig(message))

        // Some placeholders require the world thread to work
        event.sender.runInWorld {
            val message = MessageParser.parse(event.sender, format, comp)

            for (recipient in event.recipients) {
                recipient.sendMessage(message)
            }

            if (event.recipients.isEmpty() || event.recipients.singleOrNull()?.uuid == event.sender.uuid) {
                event.sender.sendMessage(MessagesConfig::chatNoRecipients)
            }
        }
    }

    fun getRecipients(sender: User): MutableList<User>? {
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
            return@removeIf permission?.let { !user.hasPermission(it) } ?: false
                    && user.settings.disabledChannels.contains(this.id)
        }

        return recipients
    }
}