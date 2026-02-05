package com.github.heroslender.herochat.listeners

import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.service.ChannelService
import com.github.heroslender.herochat.service.UserService
import com.github.heroslender.herochat.utils.registerEvent
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.event.EventPriority
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent

class PlayerListener(
    private val userService: UserService,
    private val channelService: ChannelService,
) {
    init {
        registerEvent<PlayerChatEvent>(EventPriority.EARLY) { e ->
            e.isCancelled = true

            val sender = userService.getUser(e.sender) ?: return@registerEvent
            val channel = channelService.channels[sender.settings.focusedChannelId] ?: channelService.defaultChannel
            if (channel == null) {
                sender.sendMessage(MessagesConfig::channelNotFound)
                return@registerEvent
            }

            channel.sendMessage(sender, e.content)
        }

        registerEvent<PlayerConnectEvent> { e ->
            userService.onJoin(e.playerRef)
        }

        registerEvent<PlayerDisconnectEvent> { e ->
            userService.onQuit(e.playerRef)
        }
    }
}