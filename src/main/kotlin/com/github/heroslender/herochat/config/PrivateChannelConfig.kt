package com.github.heroslender.herochat.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec

class PrivateChannelConfig {
    var name: String = "Whisper"
    var commands: Array<String> = arrayOf("tell", "w", "whisper", "pm")
    var senderFormat: String = "Message to {target_username}{#555555}{bold}> {#AAAAAA}{message}"
    var receiverFormat: String = "Message from {player_username}{#555555}{bold}> {#AAAAAA}{message}"
    var permission: String? = null
    var components: MutableMap<String, ComponentConfig> = mutableMapOf()

    companion object {
        @JvmField
        val CODEC: BuilderCodec<PrivateChannelConfig> = BuilderCodec.builder(
            PrivateChannelConfig::class.java,
            ::PrivateChannelConfig
        )
            .append(
                KeyedCodec("Name", Codec.STRING),
                { config, value -> config.name = value },
                { config -> config.name }
            ).add()
            .append(
                KeyedCodec("Commands", Codec.STRING_ARRAY, false),
                { config, value -> config.commands = value ?: emptyArray() },
                { config -> config.commands }
            ).add()
            .append(
                KeyedCodec("SenderFormat", Codec.STRING),
                { config, value -> config.senderFormat = value },
                { config -> config.senderFormat }
            ).add()
            .append(
                KeyedCodec("ReceiverFormat", Codec.STRING),
                { config, value -> config.receiverFormat = value },
                { config -> config.receiverFormat }
            ).add()
            .append(
                KeyedCodec("Permission", Codec.STRING, false),
                { config, value -> config.permission = value },
                { config -> config.permission }
            ).add()
            .append(
                KeyedCodec("Components", MapCodec(ComponentConfig.CODEC) { mutableMapOf<String, ComponentConfig>() }),
                { config, value -> config.components = value?.let { HashMap(it) } ?: mutableMapOf() },
                { config -> config.components }
            ).add()
            .build()
    }
}