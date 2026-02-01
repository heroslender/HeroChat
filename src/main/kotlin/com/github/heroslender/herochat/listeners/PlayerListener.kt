package com.github.heroslender.herochat.listeners

import com.github.heroslender.herochat.ChannelManager
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.chat.PrivateChannel
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.service.UserService
import com.github.heroslender.herochat.utils.registerEvent
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.event.EventPriority
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent
import com.hypixel.hytale.server.core.universe.Universe
import java.util.concurrent.CompletableFuture

class PlayerListener(
    private val userService: UserService,
    private val channelManager: ChannelManager,
) {
    init {
        registerEvent<PlayerChatEvent>(EventPriority.EARLY) { e ->
            e.isCancelled = true
            val executor = Universe.get().getWorld(e.sender.worldUuid ?: return@registerEvent)
            val player = CompletableFuture.supplyAsync({
                e.sender.reference?.store?.getComponent(
                    e.sender.reference ?: return@supplyAsync null,
                    Player.getComponentType()
                )
            }, executor).join() ?: return@registerEvent

            val settings = userService.getSettings(e.sender.uuid)
            if (settings.focusedChannelId == PrivateChannel.ID) {
                val targetUuid = settings.focusedPrivateTarget
                if (targetUuid == null) {
                    player.sendMessage(MessagesConfig::privateChatNotActive)
                    return@registerEvent
                }

                val target = Universe.get().getPlayer(targetUuid)
                if (target == null) {
                    player.sendMessage(MessagesConfig::privateChatPlayerNotFound)
                    return@registerEvent
                }

                channelManager.privateChannel.sendMessage(player, e.content, target)
                return@registerEvent
            }

            val channel = channelManager.channels[settings.focusedChannelId] ?: channelManager.defaultChannel

            if (channel != null) {
                channel.sendMessage(player, e.content)
            } else {
                player.sendMessage(MessagesConfig::channelNotFound)
            }
        }

        registerEvent<PlayerConnectEvent> { e ->
            userService.loadUserAsync(e.playerRef.uuid)
        }

        registerEvent<PlayerDisconnectEvent> { e ->
            userService.unloadUser(e.playerRef.uuid)
        }
    }
}