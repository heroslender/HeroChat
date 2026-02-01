package com.github.heroslender.herochat.chat

import com.github.heroslender.herochat.ComponentParser
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.config.PrivateChannelConfig
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.server.core.command.system.CommandSender
import com.hypixel.hytale.server.core.universe.PlayerRef

class PrivateChannel(config: PrivateChannelConfig) {
    val id: String = ID
    val name: String = config.name
    val format: String = config.senderFormat
    val receiverFormat: String = config.receiverFormat
    val permission: String? = config.permission
    val components: Map<String, ComponentConfig> = config.components

    fun sendMessage(
        sender: CommandSender,
        msg: String,
        extra: PlayerRef
    ) {
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(MessagesConfig::channelNoPermission)
            return
        }

        if (extra.uuid == sender.uuid) {
            sender.sendMessage(MessagesConfig::privateChatSelf)
            return
        }

        val comp = HeroChat.instance.config.components +
                components +
                ("message" to ComponentConfig(msg)) +
                ("target_username" to ComponentConfig(extra.username))
        val message = ComponentParser.parse(sender.uuid, format, comp)
        val receivedMessage = ComponentParser.parse(sender.uuid, receiverFormat, comp)

        sender.sendMessage(message)
        extra.sendMessage(receivedMessage)
    }

    companion object {
        const val ID = "tell"
    }
}
