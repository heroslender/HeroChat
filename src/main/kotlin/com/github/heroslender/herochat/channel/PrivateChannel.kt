package com.github.heroslender.herochat.channel

import com.github.heroslender.herochat.ComponentParser
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.config.PrivateChannelConfig
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.event.PreChatEvent
import com.github.heroslender.herochat.event.PrivateChannelChatEvent
import com.github.heroslender.herochat.service.UserService
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.server.core.HytaleServer

class PrivateChannel(
    config: PrivateChannelConfig,
    private val userService: UserService
) : Channel {
    override val id: String = ID
    override val name: String = config.name
    override val commands: Array<String> = config.commands
    val format: String = config.senderFormat
    val receiverFormat: String = config.receiverFormat
    override val permission: String? = config.permission
    override val capslockFilter: CapslockFilter = CapslockFilter(config.capslockFilter)
    val cooldowns: Map<String, Long> = config.cooldowns
    val components: Map<String, ComponentConfig> = config.components

    override fun sendMessage(sender: User, msg: String) {
        val settings = sender.settings
        val targetUuid = settings.focusedPrivateTarget
        if (targetUuid == null) {
            sender.sendMessage(MessagesConfig::privateChatNotActive)
            return
        }

        val target = userService.getUser(targetUuid)
        if (target == null) {
            sender.sendMessage(MessagesConfig::privateChatPlayerNotFound)
            return
        }

        sendMessage(sender, target, msg)
    }

    fun sendMessage(
        sender: User,
        target: User,
        msg: String
    ) {
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(MessagesConfig::channelNoPermission)
            return
        }

        if (target.uuid == sender.uuid) {
            sender.sendMessage(MessagesConfig::privateChatSelf)
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
            ).whenComplete { event, throwable -> onPreChatEvent(event, target, throwable) }
    }

    fun onPreChatEvent(event: PreChatEvent, target: User, throwable: Throwable?) {
        if (throwable != null) {
            throwable.printStackTrace()
            return
        }

        HytaleServer.get()
            .eventBus
            .dispatchForAsync(PrivateChannelChatEvent::class.java)
            .dispatch(
                PrivateChannelChatEvent(
                    sender = event.sender,
                    channel = this,
                    target = target,
                    message = event.message
                )
            ).whenComplete(::onChatEvent)
    }

    fun onChatEvent(event: PrivateChannelChatEvent, throwable: Throwable?) {
        if (throwable != null) {
            throwable.printStackTrace()
            return
        }

        val sender = event.sender
        val target = event.target

        val comp = HeroChat.instance.config.components +
                components +
                ("message" to ComponentConfig(event.message)) +
                ("target_username" to ComponentConfig(target.username))
        val message = ComponentParser.parse(sender.uuid, format, comp)
        val receivedMessage = ComponentParser.parse(sender.uuid, receiverFormat, comp)

        sender.sendMessage(message)
        target.sendMessage(receivedMessage)
    }

    companion object {
        const val ID = "tell"
    }
}
