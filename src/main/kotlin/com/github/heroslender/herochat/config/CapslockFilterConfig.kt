package com.github.heroslender.herochat.config

import com.github.heroslender.herochat.utils.append
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.builder.BuilderCodec

data class CapslockFilterConfig(
    var enabled: Boolean = false,
    var percentage: Int = 50,
    var minLength: Int = 5,
){
    companion object {
        @JvmField
        val CODEC: BuilderCodec<CapslockFilterConfig> = BuilderCodec.builder(
            CapslockFilterConfig::class.java,
            ::CapslockFilterConfig
        )
            .append(CapslockFilterConfig::enabled, Codec.BOOLEAN)
            .append(CapslockFilterConfig::percentage, Codec.INTEGER)
            .append(CapslockFilterConfig::minLength, Codec.INTEGER)
            .build()
    }
}