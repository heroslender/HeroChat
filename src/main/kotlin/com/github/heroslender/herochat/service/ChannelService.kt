package com.github.heroslender.herochat.service

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.channel.Channel
import com.github.heroslender.herochat.channel.PrivateChannel
import com.github.heroslender.herochat.channel.StandardChannel
import com.github.heroslender.herochat.config.ChatConfig
import com.hypixel.hytale.logger.HytaleLogger

class ChannelService(
    private val plugin: HeroChat,
    config: ChatConfig,
) {
    private val _channels: MutableMap<String, Channel> = mutableMapOf()
    val channels: Map<String, Channel>
        get() = _channels

    val defaultChannel: Channel?
        get() = channels[plugin.config.defaultChat]

    private val logger: HytaleLogger = plugin.logger.getSubLogger("ChannelService")

    init {
        plugin.channelConfigs.keys.forEach(::loadChannel)
        loadChannel(PrivateChannel.ID)

        if (defaultChannel == null) {
            logger.atSevere().log("Default channel ${config.defaultChat} not found!")
        }
    }

    fun reloadChannel(channelId: String) {
        unloadChannel(channelId)

        loadChannel(channelId)
    }

    fun loadChannel(channelId: String) {
        val channel = if (channelId == PrivateChannel.ID)
            PrivateChannel(plugin.privateChannelConfig, plugin.userService, logger)
        else
            StandardChannel(channelId, plugin.channelConfigs[channelId] ?: return, plugin.userService, logger)

        _channels[channelId] = channel

        channel.load()
        logger.atInfo().log("Loaded channel ${channel.name}.")
    }

    fun unloadChannel(channelId: String) {
        val channel = _channels.remove(channelId) ?: return
        channel.unload()
        logger.atInfo().log("Unloaded channel ${channel.name}.")
    }

    fun updateDefaultChannel(channelId: String) {
        plugin.config.defaultChat = channelId
        plugin.saveConfig()
    }
}