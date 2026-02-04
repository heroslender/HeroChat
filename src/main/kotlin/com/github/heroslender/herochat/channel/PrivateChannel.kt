package com.github.heroslender.herochat.channel

import com.github.heroslender.herochat.ComponentParser
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.config.PrivateChannelConfig
import com.github.heroslender.herochat.service.UserService
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.server.core.command.system.CommandSender
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe

class PrivateChannel(config: PrivateChannelConfig, private val userService: UserService) : Channel {
    override val id: String = ID
    override val name: String = config.name
    override val commands: Array<String> = config.commands
    val format: String = config.senderFormat
    val receiverFormat: String = config.receiverFormat
    override val permission: String? = config.permission
    val components: Map<String, ComponentConfig> = config.components

    override fun sendMessage(sender: CommandSender, msg: String) {
        val settings = userService.getSettings(sender.uuid)
        if (settings.disabledChannels.contains(id)) {
            sender.sendMessage(MessagesConfig::channelDisabled)
            return
        }

        val targetUuid = settings.focusedPrivateTarget
        if (targetUuid == null) {
            sender.sendMessage(MessagesConfig::privateChatNotActive)
            return
        }

        val target = Universe.get().getPlayer(targetUuid)
        if (target == null) {
            sender.sendMessage(MessagesConfig::privateChatPlayerNotFound)
            return
        }

        sendMessage(sender, msg, target)
    }

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

        val settings = userService.getSettings(sender.uuid)
        if (settings.disabledChannels.contains(id)) {
            sender.sendMessage(MessagesConfig::channelDisabled)
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

        val spies = HeroChat.instance.userService.getSpies()
        if (spies.isEmpty()) {
            return
        }

        // Spy format: [SPY] Sender -> Target: Message
        val spyText =
            "{#FF5555}[SPY] {#AAAAAA}${sender.displayName} {#555555}-> {#AAAAAA}${extra.username}{#555555}: {#FFFFFF}$msg"
        val spyMsg = ComponentParser.parse(sender.uuid, spyText)

        for (spyUuid in spies) {
            if (spyUuid != sender.uuid && spyUuid != extra.uuid) {
                Universe.get().sendMessage(spyUuid, spyMsg)
            }
        }
    }

    companion object {
        const val ID = "tell"
    }
}
