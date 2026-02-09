package com.github.heroslender.herochat.listeners

import com.github.heroslender.herochat.ComponentParser
import com.github.heroslender.herochat.Permissions
import com.github.heroslender.herochat.channel.Channel
import com.github.heroslender.herochat.channel.PrivateChannel
import com.github.heroslender.herochat.channel.StandardChannel
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.event.ChannelChatEvent
import com.github.heroslender.herochat.event.PreChatEvent
import com.github.heroslender.herochat.event.PrivateChannelChatEvent
import com.github.heroslender.herochat.service.UserService
import com.github.heroslender.herochat.utils.registerEvent
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.event.EventPriority
import com.hypixel.hytale.server.core.Message

class ChatListener(
    private val userService: UserService,
) {
    init {
        // Check for disabled channels, chat cooldown and spam
        registerEvent<PreChatEvent>(EventPriority.EARLY) { e ->
            val settings = e.sender.settings
            if (settings.disabledChannels.contains(e.channel.id)) {
                e.sender.sendMessage(MessagesConfig::channelDisabled)
                e.isCancelled = true
                return@registerEvent
            }

            if (isCooldown(e.sender, e.channel)) {
                e.sender.sendMessage(MessagesConfig::chatCooldown)
                e.isCancelled = true
                return@registerEvent
            }

            val lastMsg = e.sender.lastMessage
            if (!e.sender.hasPermission(Permissions.BYPASS_SPAM) && lastMsg.equals(e.message, ignoreCase = true)) {
                e.sender.sendMessage(MessagesConfig::chatSpamWarning)
                e.isCancelled = true
                return@registerEvent
            }
            e.sender.lastMessage = e.message
        }

        // Append player default color to message
        registerEvent<ChannelChatEvent> { e ->
            val settings = e.sender.settings
            if (e.sender.hasPermission(Permissions.SETTINGS_MESSAGE_COLOR) && settings.messageColor != null) {
                e.message = "{${settings.messageColor}}${e.message}"
            }
        }

        // Spy chat
        registerEvent<ChannelChatEvent>(EventPriority.LAST) { e ->
            val spies = userService.getSpies()
            if (spies.isEmpty()) {
                return@registerEvent
            }

            val spyMessage = Message.empty()
                .insert(Message.raw("[SPY][${e.channel.name}]").color("#FF5555").bold(true))
                .insert(e.sender.username)
                .insert(" -> ")
                .insert(e.message)

            for (spy in spies) {
                if (e.recipients.none { it.uuid == spy.uuid }) {
                    spy.sendMessage(spyMessage)
                }
            }
        }

        // Spy private chat
        registerEvent<PrivateChannelChatEvent>(EventPriority.LAST) { e ->
            val spies = userService.getSpies()
            if (spies.isEmpty()) {
                return@registerEvent
            }

            // Spy format: [SPY] Sender -> Target: Message
            val spyText =
                "{#FF5555}[SPY] {#AAAAAA}${e.sender.username} {#555555}-> {#AAAAAA}${e.target.username}{#555555}: {#FFFFFF}${e.message}"
            val spyMsg = ComponentParser.parse(e.sender.uuid, spyText)

            for (spy in spies) {
                if (spy.uuid != e.sender.uuid && spy.uuid != e.target.uuid) {
                    spy.sendMessage(spyMsg)
                }
            }
        }
    }

    fun isCooldown(user: User, channel: Channel): Boolean {
        val now = System.currentTimeMillis()
        val cooldown = channel.getCooldown(user)

        if (now - user.lastMessageTime >= cooldown) {
            user.lastMessageTime = now
            return false
        }

        return true
    }

    fun Channel.getCooldown(user: User): Long {
        if (user.hasPermission(Permissions.BYPASS_COOLDOWN)) {
            return 0
        }

        val cooldowns = when (this) {
            is StandardChannel -> this.cooldowns
            is PrivateChannel -> this.cooldowns
            else -> return 0
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
}