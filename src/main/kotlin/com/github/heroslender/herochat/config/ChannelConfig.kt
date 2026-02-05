package com.github.heroslender.herochat.config

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
    var components: MutableMap<String, ComponentConfig> = mutableMapOf()
    var cooldowns: MutableMap<String, Long> = mutableMapOf()

    companion object {
        @JvmField
        val CODEC: BuilderCodec<ChannelConfig> = BuilderCodec.builder(
            ChannelConfig::class.java,
            ::ChannelConfig
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
                KeyedCodec("Format", Codec.STRING),
                { config, value -> config.format = value },
                { config -> config.format }
            ).add()
            .append(
                KeyedCodec("Permission", Codec.STRING, false),
                { config, value -> config.permission = value },
                { config -> config.permission }
            ).add()
            .append(
                KeyedCodec("Distance", Codec.DOUBLE, false),
                { config, value -> config.distance = value },
                { config -> config.distance }
            ).add()
            .append(
                KeyedCodec("CrossWorld", Codec.BOOLEAN, false),
                { config, value -> config.crossWorld = value },
                { config -> config.crossWorld }
            ).add()
            .append(
                KeyedCodec("Components", MapCodec(ComponentConfig.CODEC) { mutableMapOf<String, ComponentConfig>() }),
                { config, value -> config.components = value?.let { HashMap(it) } ?: mutableMapOf() },
                { config -> config.components }
            ).add()
            .append(
                KeyedCodec("Cooldowns", MapCodec(Codec.LONG) { mutableMapOf<String, Long>() }),
                { config, value -> config.cooldowns = value?.let { HashMap(it) } ?: mutableMapOf() },
                { config -> config.cooldowns }
            ).add()
            .build()
    }
}