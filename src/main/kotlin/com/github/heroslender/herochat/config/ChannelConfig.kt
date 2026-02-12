package com.github.heroslender.herochat.config

import com.github.heroslender.herochat.utils.append
import com.github.heroslender.herochat.utils.appendString
import com.github.heroslender.herochat.utils.appendStringOpt
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec

class ChannelConfig {
    var name: String = "Global"
    var commands: Array<String> = arrayOf("g", "global")
    var format: String = "{player_username}{#555555}{bold}> {#AAAAAA}{message}"
    var permission: String? = null
    var distance: Double? = null
    var crossWorld: Boolean? = null
    var capslockFilter: CapslockFilterConfig = CapslockFilterConfig()
    var cooldowns: MutableMap<String, Long> = mutableMapOf()
    var components: MutableMap<String, ComponentConfig> = mutableMapOf()

    companion object {
        @JvmField
        val CODEC: BuilderCodec<ChannelConfig> = BuilderCodec.builder(
            ChannelConfig::class.java,
            ::ChannelConfig
        )
            .appendString(ChannelConfig::name)
            .append(ChannelConfig::commands, Codec.STRING_ARRAY) { emptyArray<String>() }
            .appendString(ChannelConfig::format)
            .appendStringOpt(ChannelConfig::permission)
            .append(ChannelConfig::distance, Codec.DOUBLE)
            .append(ChannelConfig::crossWorld, Codec.BOOLEAN)
            .append(ChannelConfig::capslockFilter, CapslockFilterConfig.CODEC) { CapslockFilterConfig() }
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