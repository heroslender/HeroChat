package com.github.heroslender.herochat

import com.github.heroslender.herochat.chat.Channel
import com.github.heroslender.herochat.chat.PrivateChannel
import com.github.heroslender.herochat.commands.ChannelCommand
import com.github.heroslender.herochat.commands.PrivateChannelCommand
import com.github.heroslender.herochat.config.ChannelConfig
import com.github.heroslender.herochat.config.ChatConfig
import com.github.heroslender.herochat.config.PrivateChannelConfig
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.command.system.CommandRegistration

class ChannelManager(
    private val plugin: HeroChat,
    config: ChatConfig,
    channelConfigs: Map<String, ChannelConfig>,
    privateChannelConfig: PrivateChannelConfig,
) {
    private val _channels: MutableMap<String, Channel> = channelConfigs
        .mapValues { Channel(it.key, it.value) }
        .toMutableMap()
    val channels: Map<String, Channel>
        get() = _channels

    var privateChannel: PrivateChannel = PrivateChannel(privateChannelConfig)
        private set

    private val commands: MutableMap<String, CommandRegistration> = mutableMapOf()

    val defaultChannel: Channel?
        get() = channels[plugin.config.defaultChat]

    val logger: HytaleLogger = plugin.logger.getSubLogger("ChannelManager")

    init {
        if (defaultChannel == null) {
            logger.atSevere().log("Default channel ${config.defaultChat} not found!")
        }

        loadChannel(privateChannel)
        channels.values.forEach(::loadChannel)
    }

    fun reloadChannel(channelId: String) {
        commands.remove(channelId)?.unregister()

        if (channelId == PrivateChannel.ID) {
            privateChannel = PrivateChannel(HeroChat.instance.privateChannelConfig)
            loadChannel(privateChannel)
            return
        }

        val channel = Channel(channelId, HeroChat.instance.channelConfigs[channelId] ?: return)
        _channels[channelId] = channel
        loadChannel(channel)
    }

    fun loadChannel(channel: Channel) {
        val cmd = ChannelCommand(channel)
        commands[channel.id] = plugin.commandRegistry.registerCommand(cmd)
        logger.atInfo().log("Registered channel command ${channel.id}")
    }

    fun loadChannel(channel: PrivateChannel) {
        val cmd = PrivateChannelCommand(channel)
        commands[channel.id] = plugin.commandRegistry.registerCommand(cmd)
        logger.atInfo().log("Registered channel command ${channel.id}")
    }

    fun updateDefaultChannel(channelId: String) {
        plugin.config.defaultChat = channelId
        plugin.saveConfig()
    }
}