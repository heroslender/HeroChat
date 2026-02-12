package com.github.heroslender.herochat.config

import com.github.heroslender.herochat.utils.appendString
import com.github.heroslender.herochat.utils.appendStringOpt
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
            .appendString(ComponentConfig::text)
            .appendStringOpt(ComponentConfig::permission)
            .build()
    }
}