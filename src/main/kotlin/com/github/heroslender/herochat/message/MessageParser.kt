package com.github.heroslender.herochat.message

import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.data.User
import com.hypixel.hytale.server.core.Message
import java.text.DecimalFormat

object MessageParser {
    const val PLACEHOLDER_START = '{'
    const val PLACEHOLDER_END = '}'
    const val ESCAPE_CHAR = '\\'

    fun parse(
        sender: User,
        message: String,
        components: Map<String, ComponentConfig> = emptyMap(),
    ): Message {
        val start = System.nanoTime()
        return ColorParser.parse(sender, message, components).also {
            println("Parsed new message (${DecimalFormat("#0.000").format((System.nanoTime() - start) / 1_000_000.0)}ms)")
        }
    }
}