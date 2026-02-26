package com.github.heroslender.herochat.message

import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.data.User
import com.hypixel.hytale.server.core.Message

object MessageParser {
    const val PLACEHOLDER_START = '{'
    const val PLACEHOLDER_END = '}'
    const val ESCAPE_CHAR = '\\'

    fun parse(
        sender: User,
        message: String,
        components: Map<String, ComponentConfig> = emptyMap(),
    ): Message {
        return ColorParser.parse(sender, message, components)
    }
}