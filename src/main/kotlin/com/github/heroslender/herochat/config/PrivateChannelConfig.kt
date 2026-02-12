package com.github.heroslender.herochat.config

import com.github.heroslender.herochat.utils.append
import com.github.heroslender.herochat.utils.appendString
import com.github.heroslender.herochat.utils.appendStringOpt
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
    var capslockFilter: CapslockFilterConfig = CapslockFilterConfig()
    var cooldowns: MutableMap<String, Long> = mutableMapOf()
    var components: MutableMap<String, ComponentConfig> = mutableMapOf()

    companion object {
        @JvmField
        val CODEC: BuilderCodec<PrivateChannelConfig> = BuilderCodec.builder(
            PrivateChannelConfig::class.java,
            ::PrivateChannelConfig
        )
            .appendString(PrivateChannelConfig::name)
            .append(PrivateChannelConfig::commands, Codec.STRING_ARRAY) { emptyArray<String>() }
            .appendString(PrivateChannelConfig::senderFormat)
            .appendString(PrivateChannelConfig::receiverFormat)
            .appendStringOpt(PrivateChannelConfig::permission)
            .append(PrivateChannelConfig::capslockFilter, CapslockFilterConfig.CODEC) { CapslockFilterConfig() }
            .append(
                KeyedCodec("Cooldowns", MapCodec(Codec.LONG) { mutableMapOf<String, Long>() }),
                { config, value -> config.cooldowns = value?.let { HashMap(it) } ?: mutableMapOf() },
                { config -> config.cooldowns }
            ).add()
            .append(
                KeyedCodec("Components", MapCodec(ComponentConfig.CODEC) { mutableMapOf<String, ComponentConfig>() }),
                { config, value -> config.components = value?.let { HashMap(it) } ?: mutableMapOf() },
                { config -> config.components }
            ).add()
            .build()
    }
}