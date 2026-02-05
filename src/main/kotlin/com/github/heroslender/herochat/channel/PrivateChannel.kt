package com.github.heroslender.herochat.channel

import com.github.heroslender.herochat.ComponentParser
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.Permissions
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.config.PrivateChannelConfig
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.service.UserService
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.server.core.universe.Universe

class PrivateChannel(config: PrivateChannelConfig, private val userService: UserService) : Channel {
    override val id: String = ID
    override val name: String = config.name
    override val commands: Array<String> = config.commands
    val format: String = config.senderFormat
    val receiverFormat: String = config.receiverFormat
    override val permission: String? = config.permission
    val components: Map<String, ComponentConfig> = config.components
    val cooldowns: Map<String, Long> = config.cooldowns

    override fun sendMessage(sender: User, msg: String) {
        val settings = sender.settings
        if (settings.disabledChannels.contains(id)) {
            sender.sendMessage(MessagesConfig::channelDisabled)
            return
        }

        val targetUuid = settings.focusedPrivateTarget
        if (targetUuid == null) {
            sender.sendMessage(MessagesConfig::privateChatNotActive)
            return
        }

        if (sender.isCooldown()) {
            sender.sendMessage(MessagesConfig::chatCooldown)
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

        val settings = sender.settings
        if (settings.disabledChannels.contains(id)) {
            sender.sendMessage(MessagesConfig::channelDisabled)
            return
        }

        val comp = HeroChat.instance.config.components +
                components +
                ("message" to ComponentConfig(msg)) +
                ("target_username" to ComponentConfig(target.username))
        val message = ComponentParser.parse(sender.uuid, format, comp)
        val receivedMessage = ComponentParser.parse(sender.uuid, receiverFormat, comp)

        sender.sendMessage(message)
        target.sendMessage(receivedMessage)

        val spies = HeroChat.instance.userService.getSpies()
        if (spies.isEmpty()) {
            return
        }

        // Spy format: [SPY] Sender -> Target: Message
        val spyText =
            "{#FF5555}[SPY] {#AAAAAA}${sender.username} {#555555}-> {#AAAAAA}${target.username}{#555555}: {#FFFFFF}$msg"
        val spyMsg = ComponentParser.parse(sender.uuid, spyText)

        for (spy in spies) {
            if (spy.uuid != sender.uuid && spy.uuid != target.uuid) {
                spy.sendMessage(spyMsg)
            }
        }
    }

    fun User.isCooldown(): Boolean {
        val now = System.currentTimeMillis()
        val cooldown = getCooldown(this)

        if (now - lastMessageTime >= cooldown) {
            lastMessageTime = now
            return false
        }

        return true
    }

    fun getCooldown(user: User): Long {
        if (user.hasPermission(Permissions.BYPASS_COOLDOWN)) {
            return 0
        }

        var cooldown: Long? = null
        for ((perm, dur) in cooldowns) {
            if (user.hasPermission(perm)) {
                if (cooldown == null || cooldown < dur) {
                    cooldown = dur
                }
            }
        }

        return cooldown ?: 0
    }

    companion object {
        const val ID = "tell"
    }
}
