package com.github.heroslender.herochat.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec

class MessagesConfig {
    var channelNoPermission: String = "{#FF5555}You do not have permission to send messages in this channel."
    var channelNotFound: String = "{#FF5555}Channel not found."
    var channelJoined: String = "{#55FF55}You are now talking in {#FFFFFF}{channel}{#55FF55}."
    var privateChatStarted: String = "{#55FF55}You are now in a private conversation with {#FFFFFF}{target}{#55FF55}."
    var privateChatPlayerNotFound: String = "{#FF5555}The player is not online."
    var privateChatSelf: String = "{#FF5555}You cannot start a private conversation with yourself."
    var privateChatNotActive: String = "{#FF5555}You are not in a private conversation."

    companion object {
        @JvmField
        val CODEC: BuilderCodec<MessagesConfig> = BuilderCodec.builder(
            MessagesConfig::class.java,
            ::MessagesConfig
        )
            .append(
                KeyedCodec("ChannelNoPermission", Codec.STRING, false),
                { config, value -> config.channelNoPermission = value },
                { config -> config.channelNoPermission }
            ).add()
            .append(
                KeyedCodec("ChannelNotFound", Codec.STRING, false),
                { config, value -> config.channelNotFound = value },
                { config -> config.channelNotFound }
            ).add()
            .append(
                KeyedCodec("ChannelJoined", Codec.STRING, false),
                { config, value -> config.channelJoined = value },
                { config -> config.channelJoined }
            ).add()
            .append(
                KeyedCodec("PrivateChatStarted", Codec.STRING, false),
                { config, value -> config.privateChatStarted = value },
                { config -> config.privateChatStarted }
            ).add()
            .append(
                KeyedCodec("PrivateChatPlayerNotFound", Codec.STRING, false),
                { config, value -> config.privateChatPlayerNotFound = value },
                { config -> config.privateChatPlayerNotFound }
            ).add()
            .append(
                KeyedCodec("PrivateChatSelf", Codec.STRING, false),
                { config, value -> config.privateChatSelf = value },
                { config -> config.privateChatSelf }
            ).add()
            .append(
                KeyedCodec("PrivateChatNotActive", Codec.STRING, false),
                { config, value -> config.privateChatNotActive = value },
                { config -> config.privateChatNotActive }
            ).add()
            .build()
    }
}