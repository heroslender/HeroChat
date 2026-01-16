package com.github.heroslender.herochat.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec

class ChatConfig {
    var chatFormat: String = "{prefix}{admin_prefix} {#ff5555}{player_username}{#555555}{bold}> {#AAAAAA}{message}"
    var components: MutableMap<String, ComponentConfig> = mutableMapOf()

    companion object {

        @JvmField
        val CODEC: BuilderCodec<ChatConfig> = BuilderCodec.builder(
            ChatConfig::class.java,
            ::ChatConfig
        )
            .append(
                KeyedCodec("ChatFormat", Codec.STRING),
                { config, value -> config.chatFormat = value },
                { config -> config.chatFormat }
            ).add()
            .append(
                KeyedCodec("Components", MapCodec(ComponentConfig.CODEC) { mutableMapOf<String, ComponentConfig>() }),
                { config, value -> config.components = value },
                { config -> config.components }
            ).add()
            .build()
    }
}

class ComponentConfig(
    var text: String = "",
    var permission: String? = null,
) {
    companion object {
        @JvmField
        val CODEC: BuilderCodec<ComponentConfig> = BuilderCodec.builder(
            ComponentConfig::class.java,
            ::ComponentConfig
        )
            .append(
                KeyedCodec("Text", Codec.STRING),
                { config, value -> config.text = value },
                { config -> config.text }
            ).add()
            .append(
                KeyedCodec("Permission", Codec.STRING, false),
                { config, value -> config.permission = value },
                { config -> config.permission }
            ).add()
            .build()
    }
}
