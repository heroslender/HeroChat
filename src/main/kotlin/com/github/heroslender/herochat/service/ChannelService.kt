package com.github.heroslender.herochat.service

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.channel.Channel
import com.github.heroslender.herochat.channel.PrivateChannel
import com.github.heroslender.herochat.channel.StandardChannel
import com.github.heroslender.herochat.commands.ChannelCommand
import com.github.heroslender.herochat.commands.PrivateChannelCommand
import com.github.heroslender.herochat.config.ChannelConfig
import com.github.heroslender.herochat.config.ChatConfig
import com.github.heroslender.herochat.config.PrivateChannelConfig
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.command.system.CommandRegistration

class ChannelService(
    private val plugin: HeroChat,
    config: ChatConfig,
    channelConfigs: Map<String, ChannelConfig>,
    privateChannelConfig: PrivateChannelConfig,
) {
    private val _channels: MutableMap<String, Channel> = mutableMapOf()
    val channels: Map<String, Channel>
        get() = _channels

    val defaultChannel: Channel?
        get() = channels[plugin.config.defaultChat]

    private val logger: HytaleLogger = plugin.logger.getSubLogger("ChannelService")
    private val commands: MutableMap<String, CommandRegistration> = mutableMapOf()

    init {
        _channels.putAll(channelConfigs.mapValues { StandardChannel(it.key, it.value, plugin.userService) })
        _channels[PrivateChannel.ID] = PrivateChannel(privateChannelConfig, plugin.userService)

        if (defaultChannel == null) {
            logger.atSevere().log("Default channel ${config.defaultChat} not found!")
        }

        channels.values.forEach(::loadChannel)
    }

    fun reloadChannel(channelId: String) {
        commands.remove(channelId)?.unregister()

        val channel = if (channelId == PrivateChannel.ID)
            PrivateChannel(plugin.privateChannelConfig, plugin.userService)
        else
            StandardChannel(channelId, plugin.channelConfigs[channelId] ?: return, plugin.userService)

        _channels[channelId] = channel
        loadChannel(channel)
    }

    fun loadChannel(channel: Channel) {
        if (channel.commands.isNotEmpty()) {
            val cmd = when (channel) {
                is StandardChannel -> ChannelCommand(channel, plugin.userService)
                is PrivateChannel -> PrivateChannelCommand(channel, plugin.userService)
                else -> return
            }
            commands[channel.id] = plugin.commandRegistry.registerCommand(cmd)
            logger.atInfo()
                .log("Registered channel command ${cmd.name}${cmd.aliases.joinToString(", ", " with aliases: ")}.")
        }
    }

    fun updateDefaultChannel(channelId: String) {
        plugin.config.defaultChat = channelId
        plugin.saveConfig()
    }
}