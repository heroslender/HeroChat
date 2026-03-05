package com.github.heroslender.herochat.listeners

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.service.ChannelService
import com.github.heroslender.herochat.service.UserService
import com.github.heroslender.herochat.utils.registerEvent
import com.github.heroslender.herochat.utils.registerGlobalEvent
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent

class PlayerListener(
    private val userService: UserService,
    private val channelService: ChannelService,
) {
    init {
        // Use global registration so moderation plugins that also register globally can cancel first.
        registerGlobalEvent<String, PlayerChatEvent>(Short.MAX_VALUE) { e ->
            if (e.isCancelled || e.targets.isEmpty()) {
                return@registerGlobalEvent
            }

            e.isCancelled = true

            val sender = userService.getUser(e.sender) ?: return@registerGlobalEvent
            val channel = channelService.channels[sender.settings.focusedChannelId] ?: channelService.defaultChannel
            if (channel == null) {
                sender.sendMessage(MessagesConfig::channelNotFound)
                return@registerGlobalEvent
            }

            channel.sendMessage(sender, e.content)
        }

        registerEvent<PlayerConnectEvent> { e ->
            userService.onJoin(e.playerRef)
        }

        registerEvent<PlayerDisconnectEvent> { e ->
            userService.onQuit(e.playerRef)
        }

        HeroChat.instance.logger.atInfo().log("Registered player listeners")
    }
}
