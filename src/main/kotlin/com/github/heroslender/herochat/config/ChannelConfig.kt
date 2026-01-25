package com.github.heroslender.herochat.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec

class ChannelConfig {
    var name: String = "Global"
    var format: String = "{player_username}{#555555}{bold}> {#AAAAAA}{message}"
    var permission: String? = null
    var distance: Double? = null
    var crossWorld: Boolean? = null

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
            .build()
    }
}