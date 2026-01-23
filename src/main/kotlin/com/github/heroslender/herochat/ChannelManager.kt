package com.github.heroslender.herochat

import com.github.heroslender.herochat.chat.Channel
import com.github.heroslender.herochat.commands.ChannelCommand
import com.github.heroslender.herochat.config.ChannelConfig
import com.github.heroslender.herochat.config.ChatConfig
import com.hypixel.hytale.logger.HytaleLogger

class ChannelManager(
    plugin: HeroChat,
    config: ChatConfig,
    channelConfigs: Map<String, ChannelConfig>,
) {
    val channels: Map<String, Channel> = channelConfigs.mapValues { Channel(it.key, it.value) }
    val defaultChannel: Channel? = channels[config.defaultChat]

    val logger: HytaleLogger = plugin.logger.getSubLogger("ChannelManager")

    init {
        if (defaultChannel == null) {
            logger.atSevere().log("Default channel ${config.defaultChat} not found!")
        }

        for (channel in channels) {
            val cmd = ChannelCommand(channel.value)
            plugin.commandRegistry.registerCommand(cmd)
            logger.atInfo().log("Registered channel command ${channel.key}")
        }
    }
}