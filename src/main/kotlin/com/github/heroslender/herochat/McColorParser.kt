package com.github.heroslender.herochat

import com.github.heroslender.herochat.ComponentParser.Companion.BOLD
import com.github.heroslender.herochat.ComponentParser.Companion.ITALIC

object McColorParser {
    private const val COLOR_CHAR = '&'
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
//        register('r', "{reset}")
    }

    private fun register(char: Char, replacement: String) {
        if (char.code < 128) REPLACEMENTS[char.code] = replacement
        val upper = char.uppercaseChar()
        if (upper.code < 128) REPLACEMENTS[upper.code] = replacement
    }

    fun parse(message: String): String {
        if (message.indexOf(COLOR_CHAR) == -1) {
            return message
        }

        val length = message.length
        val builder = StringBuilder(length + (length shr 1))
        
        var i = 0
        while (i < length) {
            val current = message[i]

            if (current == COLOR_CHAR && i + 1 < length) {
                val nextChar = message[i + 1]
                val nextCode = nextChar.code

                if (nextCode < 128) {
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
}