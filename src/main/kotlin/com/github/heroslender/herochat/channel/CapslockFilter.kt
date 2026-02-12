package com.github.heroslender.herochat.channel

import com.github.heroslender.herochat.config.CapslockFilterConfig

data class CapslockFilter(
    val enabled: Boolean = true,
    val percentage: Int = 50,
    val minLength: Int = 5,
) {
    constructor(config: CapslockFilterConfig) : this(config.enabled, config.percentage, config.minLength)
}
