package com.github.heroslender.herochat.config

import com.github.heroslender.herochat.utils.append
import com.github.heroslender.herochat.utils.appendBoolean
import com.github.heroslender.herochat.utils.appendStringOpt
import com.github.heroslender.herochat.utils.appendString
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.array.ArrayCodec

data class AutoModConfig(
    var enabled: Boolean = true,
    var defaultBlockMessage: String = "&cYour message contains forbidden content.",
    var rules: Array<AutoModRule> = arrayOf(
        AutoModRule(pattern = "badword", replacement = "***"),
        AutoModRule(
            pattern = "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b",
            isRegex = true,
            blockMessage = "&cPlease do not share IP addresses!"
        ),
        AutoModRule(
            pattern = "\\b(https?://|www\\.)\\S+\\b",
            isRegex = true,
            blockMessage = "&cPlease do not advertise!"
        )
    )
) {
    companion object {
        @JvmField
        val CODEC: BuilderCodec<AutoModConfig> = BuilderCodec.builder(
            AutoModConfig::class.java,
            ::AutoModConfig
        )
            .appendBoolean(AutoModConfig::enabled)
            .appendString(AutoModConfig::defaultBlockMessage)
            .append(AutoModConfig::rules, ArrayCodec(AutoModRule.CODEC) { arrayOf<AutoModRule>() })
            .build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AutoModConfig

        if (enabled != other.enabled) return false
        if (defaultBlockMessage != other.defaultBlockMessage) return false
        if (!rules.contentEquals(other.rules)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = enabled.hashCode()
        result = 31 * result + defaultBlockMessage.hashCode()
        result = 31 * result + rules.contentHashCode()
        return result
    }
}

data class AutoModRule(
    var patterns: Array<String> = arrayOf(),
    var isRegex: Boolean = false,
    // If null, the message is BLOCKED. If set (e.g., "***"), the pattern is replaced.
    var replacement: String? = null,
    // Custom message if the rule causes a block. Falls back to defaultBlockMessage if null.
    var blockMessage: String? = null
) {
    constructor(pattern: String, isRegex: Boolean = false, replacement: String? = null, blockMessage: String? = null):
            this(arrayOf(pattern), isRegex, replacement, blockMessage)

    companion object {
        @JvmField
        val CODEC: BuilderCodec<AutoModRule> = BuilderCodec.builder(
            AutoModRule::class.java,
            ::AutoModRule
        )
            .append(AutoModRule::patterns, Codec.STRING_ARRAY)
            .appendBoolean(AutoModRule::isRegex)
            .appendStringOpt(AutoModRule::replacement)
            .appendStringOpt(AutoModRule::blockMessage)
            .build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AutoModRule

        if (isRegex != other.isRegex) return false
        if (!patterns.contentEquals(other.patterns)) return false
        if (replacement != other.replacement) return false
        if (blockMessage != other.blockMessage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isRegex.hashCode()
        result = 31 * result + patterns.contentHashCode()
        result = 31 * result + (replacement?.hashCode() ?: 0)
        result = 31 * result + (blockMessage?.hashCode() ?: 0)
        return result
    }
}