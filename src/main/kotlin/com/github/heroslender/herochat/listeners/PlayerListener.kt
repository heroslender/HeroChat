package com.github.heroslender.herochat.listeners

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.channel.PrivateChannel
import com.github.heroslender.herochat.channel.StandardChannel
import com.github.heroslender.herochat.commands.IChannelCommand
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.event.TestPlayerChatEvent
import com.github.heroslender.herochat.service.ChannelService
import com.github.heroslender.herochat.service.UserService
import com.github.heroslender.herochat.utils.registerEvent
import com.github.heroslender.herochat.utils.registerGlobalEvent
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.event.EventPriority
import com.hypixel.hytale.protocol.Packet
import com.hypixel.hytale.protocol.packets.interface_.ChatMessage
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent
import com.hypixel.hytale.server.core.io.PacketHandler
import com.hypixel.hytale.server.core.io.adapter.PacketFilter
import com.hypixel.hytale.server.core.io.handlers.game.GamePacketHandler
import com.hypixel.hytale.server.core.universe.PlayerRef

class PlayerListener(
    private val userService: UserService,
    private val channelService: ChannelService,
) : PacketFilter {
    init {
        registerGlobalEvent<String, PlayerChatEvent>(EventPriority.FIRST) { e ->
            if (e is TestPlayerChatEvent) {
                return@registerGlobalEvent
            }

            if (e.isCancelled || e.targets.isEmpty()) {
                return@registerGlobalEvent
            }

            e.isCancelled = true
            onChat(e.sender, e.content)
        }

        registerEvent<PlayerConnectEvent> { e ->
            userService.onJoin(e.playerRef)
        }

        registerEvent<PlayerDisconnectEvent> { e ->
            userService.onQuit(e.playerRef)
        }

        HeroChat.instance.logger.atInfo().log("Registered player listeners")
    }

    // Intercept the chat message packet to fix players not being
    // able to use `"` char in chat messages as they get removed
    // by the hytale command manager.
    override fun test(
        handler: PacketHandler,
        packet: Packet
    ): Boolean {
        if (packet is ChatMessage) {
            val message = packet.message
            if (message.isNullOrEmpty()) {
                return false
            }

            val firstChar = message[0]
            if (firstChar == '.') {
                return false
            }

            if (firstChar == '/') {
                val i = message.indexOf(' ')
                val cmd = if (i == -1) message.substring(1) else message.substring(1, i).lowercase()
                val msg = if (i == -1) "" else message.substring(i + 1)
                for (channel in channelService.channels.values) {
                    if (channel is StandardChannel) {
                        val command = channel.command ?: continue
                        if (handleCommand(handler, command, cmd, msg)) {
                            return true
                        }
                    } else if (channel is PrivateChannel) {
                        val command = channel.command
                        if (command != null && handleCommand(handler, command, cmd, msg)) {
                            return true
                        }

                        val replyCommand = channel.replyCommand
                        if (replyCommand != null && handleCommand(handler, replyCommand, cmd, msg)) {
                            return true
                        }
                    }
                }

                return false
            }

            onChat(handler.playerRef, message)
            return true
        }

        return false
    }

    fun onChat(playerRef: PlayerRef, message: String) {
        val sender = userService.getUser(playerRef) ?: return

        for (channel in channelService.channels.values) {
            if (channel is StandardChannel && !channel.shoutCommands.isNullOrEmpty()) {
                for (cmd in channel.shoutCommands) {
                    if (message.startsWith(cmd)) {
                        channel.sendMessage(sender, message.substring(cmd.length).trim())
                        return
                    }
                }
            }
        }

        val channel = channelService.channels[sender.settings.focusedChannelId] ?: channelService.defaultChannel
        if (channel == null) {
            sender.sendMessage(MessagesConfig::channelNotFound)
            return
        }

        channel.sendMessage(sender, message)
    }

    fun handleCommand(handler: PacketHandler, command: IChannelCommand, cmd: String, message: String): Boolean {
        if (command.aliases.contains(cmd)) {
            val sender = userService.getUser(handler.playerRef) ?: return true
            command.execute(sender, message)
            return true
        }

        return false
    }

    val PacketHandler.playerRef: PlayerRef
        get() {
            if (this is GamePacketHandler) {
                return playerRef
            }

            throw IllegalStateException("PacketHandler is not a GamePacketHandler")
        }
}
