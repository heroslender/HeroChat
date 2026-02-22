package com.github.heroslender.herochat.message

import com.github.heroslender.herochat.message.ComponentParser.Companion.BOLD
import com.github.heroslender.herochat.message.ComponentParser.Companion.ESCAPE_CHAR
import com.github.heroslender.herochat.message.ComponentParser.Companion.ITALIC
import com.github.heroslender.herochat.message.ComponentParser.Companion.PLACEHOLDER_END
import com.github.heroslender.herochat.message.ComponentParser.Companion.PLACEHOLDER_START
import com.github.heroslender.herochat.message.ComponentParser.Companion.RESET

object McColorParser {
    private const val COLOR_CHAR = '&'
    private const val ALT_COLOR_CHAR = '§'
    private const val HEX_COLOR_CHAR = '#'
    private const val EXPANDED_HEX_COLOR_CHAR = 'x'
    private val REPLACEMENTS = arrayOfNulls<String>(128)

    init {
        register('0', "{#000000}") // Black
        register('1', "{#0000AA}") // Dark Blue
        register('2', "{#00AA00}") // Dark Green
        register('3', "{#00AAAA}") // Dark Aqua
        register('4', "{#AA0000}") // Dark Red
        register('5', "{#AA00AA}") // Dark Purple
        register('6', "{#FFAA00}") // Gold
        register('7', "{#AAAAAA}") // Gray
        register('8', "{#555555}") // Dark Gray
        register('9', "{#5555FF}") // Blue
        register('a', "{#55FF55}") // Green
        register('b', "{#55FFFF}") // Aqua
        register('c', "{#FF5555}") // Red
        register('d', "{#FF55FF}") // Light Purple
        register('e', "{#FFFF55}") // Yellow
        register('f', "{#FFFFFF}") // White

        register('l', "{$BOLD}")
        register('o', "{$ITALIC}")
//        register('n', "{underlined}")
//        register('m', "{strikethrough}")
//        register('k', "{obfuscated}")
        register('r', "{$RESET}")
    }

    private fun register(char: Char, replacement: String) {
        if (char.code < 128) REPLACEMENTS[char.code] = replacement
        val upper = char.uppercaseChar()
        if (upper.code < 128) REPLACEMENTS[upper.code] = replacement
    }

    fun parse(message: String): String {
        if (message.indexOf(COLOR_CHAR) == -1 && message.indexOf(ALT_COLOR_CHAR) == -1) {
            return message
        }

        val length = message.length
        val builder = StringBuilder(length + (length shr 1))

        var i = 0
        while (i < length) {
            val current = message[i]

            if (current == ESCAPE_CHAR) {
                builder.append(message[++i])
                i++
                continue
            } else if ((current == COLOR_CHAR || current == ALT_COLOR_CHAR) && i + 1 < length) {
                val nextChar = message[i + 1]
                val nextCode = nextChar.code

                if (nextChar == EXPANDED_HEX_COLOR_CHAR && i + 13 < length) {
                    val hex = parseHexExtended(message, i)
                    if (hex != null) {
                        builder.append(PLACEHOLDER_START).append('#').append(hex).append(PLACEHOLDER_END)
                        i += 14
                        continue
                    }
                } else if (nextChar == HEX_COLOR_CHAR && i + 7 < length) {
                    if (isValidHex(message, i)) {
                        builder.append(PLACEHOLDER_START).append(message, i + 1, i + 8).append(PLACEHOLDER_END)
                        i += 8
                        continue
                    }
                } else if (nextCode < 128) {
                    val replacement = REPLACEMENTS[nextCode]
                    if (replacement != null) {
                        builder.append(replacement)
                        i += 2 // Avança 2 caracteres (& e o código)
                        continue
                    }
                }
            }

            builder.append(current)
            i++
        }

        return builder.toString()
    }

    fun isValidHex(message: String, index: Int): Boolean {
        for (i in index + 2..index + 7) {
            val hexChar = message[i]
            if (hexChar !in '0'..'9' && hexChar !in 'a'..'f' && hexChar !in 'A'..'F') {
                return false
            }
        }

        return true
    }

    fun parseHexExtended(message: String, index: Int): StringBuilder? {
        val hexBuilder = StringBuilder(6)

        for ((hi, h) in (index + 2..index + 13).withIndex()) {
            val hexChar = message[h]
            if (hi % 2 == 0) {
                if (hexChar != COLOR_CHAR && hexChar != ALT_COLOR_CHAR) {
                    return null
                }
            } else {
                hexBuilder.append(hexChar)
            }
        }

        return hexBuilder
    }
}