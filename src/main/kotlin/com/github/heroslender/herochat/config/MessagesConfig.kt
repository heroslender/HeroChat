package com.github.heroslender.herochat.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec

class MessagesConfig {
    var channelNoPermission: String = "{#FF5555}You do not have permission to send messages in this channel."
    var privateChatSelf: String = "{#FF5555}You cannot start a private conversation with yourself."

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
                KeyedCodec("PrivateChatSelf", Codec.STRING, false),
                { config, value -> config.privateChatSelf = value },
                { config -> config.privateChatSelf }
            ).add()
            .build()
    }
}