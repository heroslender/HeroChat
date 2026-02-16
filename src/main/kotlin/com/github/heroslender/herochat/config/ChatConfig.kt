package com.github.heroslender.herochat.config

import com.github.heroslender.herochat.utils.append
import com.github.heroslender.herochat.utils.appendString
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec

class ChatConfig {
    var defaultChat: String = "global"
    var enableMinecraftColors: Boolean = true
    var nicknameMaxLength: Int = 16
    var components: MutableMap<String, ComponentConfig> = mutableMapOf()

    companion object {

        @JvmField
        val CODEC: BuilderCodec<ChatConfig> = BuilderCodec.builder(
            ChatConfig::class.java,
            ::ChatConfig
        )
            .appendString(ChatConfig::defaultChat)
            .append(ChatConfig::enableMinecraftColors, Codec.BOOLEAN)
            .append(ChatConfig::nicknameMaxLength, Codec.INTEGER)
            .append(
                KeyedCodec("Components", MapCodec(ComponentConfig.CODEC) { mutableMapOf<String, ComponentConfig>() }),
                { config, value -> config.components = value?.let { HashMap(it) } ?: mutableMapOf() },
                { config -> config.components }
            ).add()
            .build()
    }
}
