package com.github.heroslender.herochat.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec

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