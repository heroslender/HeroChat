package com.github.heroslender.herochat.message

import com.github.heroslender.herochat.Permissions
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.message.MessageParser.ESCAPE_CHAR
import com.github.heroslender.herochat.message.MessageParser.PLACEHOLDER_END
import com.github.heroslender.herochat.message.MessageParser.PLACEHOLDER_START
import com.hypixel.hytale.server.core.Message

class ColorParser {
    companion object {
        const val HEX_COLOR_CHAR = '#'

        const val RAINBOW = "rainbow"
        const val RESET = "reset"
        const val BOLD = "bold"
        const val ITALIC = "italic"
        const val MONOSPACED = "monospaced"

        const val MC_COLOR_CHAR = '&'
        const val MC_ALT_COLOR_CHAR = '§'
        const val MC_HEX_COLOR_CHAR = HEX_COLOR_CHAR
        const val MC_EXPANDED_HEX_COLOR_CHAR = 'x'
        const val MC_BOLD = 'l'
        const val MC_ITALIC = 'o'
        const val MC_RESET = 'r'
        val COLOR_REPLACEMENTS = arrayOfNulls<String>(128)

        init {
            register('0', "#000000") // Black
            register('1', "#0000AA") // Dark Blue
            register('2', "#00AA00") // Dark Green
            register('3', "#00AAAA") // Dark Aqua
            register('4', "#AA0000") // Dark Red
            register('5', "#AA00AA") // Dark Purple
            register('6', "#FFAA00") // Gold
            register('7', "#AAAAAA") // Gray
            register('8', "#555555") // Dark Gray
            register('9', "#5555FF") // Blue
            register('a', "#55FF55") // Green
            register('b', "#55FFFF") // Aqua
            register('c', "#FF5555") // Red
            register('d', "#FF55FF") // Light Purple
            register('e', "#FFFF55") // Yellow
            register('f', "#FFFFFF") // White
        }

        private fun register(char: Char, replacement: String) {
            if (char.code < 128) COLOR_REPLACEMENTS[char.code] = replacement
            val upper = char.uppercaseChar()
            if (upper.code < 128) COLOR_REPLACEMENTS[upper.code] = replacement
        }

        fun parse(
            sender: User,
            message: String,
            components: Map<String, ComponentConfig> = emptyMap(),
        ): Message {
            val message = PlaceholderManager.parsePlaceholders(sender, message, components)
            return ColorParser().parseColors(message)
        }

        fun stripStyle(
            message: String,
        ): String = validateFormat(
            message = message,
            formatColors = false,
            formatRainbow = false,
            formatGradient = false,
            formatBold = false,
            formatItalic = false,
            formatMonospaced = false,
            formatPlaceholders = true,
            remove = true,
        )

        fun validateFormat(
            user: User,
            message: String,
            basePermission: String,
            formatPlaceholders: Boolean = false,
            remove: Boolean = false,
        ): String = validateFormat(
            message = message,
            formatColors = user.hasPermission("$basePermission.colors") || user.hasPermission(Permissions.CHAT_COLOR),
            formatRainbow = user.hasPermission("$basePermission.${RAINBOW}"),
            formatGradient = user.hasPermission("$basePermission.gradient"),
            formatBold = user.hasPermission("$basePermission.${BOLD}") || user.hasPermission(Permissions.CHAT_FORMATTING),
            formatItalic = user.hasPermission("$basePermission.${ITALIC}") || user.hasPermission(Permissions.CHAT_FORMATTING),
            formatMonospaced = user.hasPermission("$basePermission.${MONOSPACED}") || user.hasPermission(Permissions.CHAT_FORMATTING),
            formatPlaceholders = formatPlaceholders,
            remove = remove,
        )

        fun validateFormat(
            message: String,
            formatColors: Boolean = true,
            formatRainbow: Boolean = true,
            formatGradient: Boolean = true,
            formatBold: Boolean = true,
            formatItalic: Boolean = true,
            formatMonospaced: Boolean = true,
            formatPlaceholders: Boolean = false,
            remove: Boolean = false,
        ): String {
            if (!message.contains(PLACEHOLDER_START)
                && !message.contains(MC_COLOR_CHAR)
                && !message.contains(MC_ALT_COLOR_CHAR)
            ) {
                return message
            }

            val length = message.length
            val builder = StringBuilder(message.length)

            var i = 0
            while (i < length) {
                val current = message[i]

                if (current == ESCAPE_CHAR) {
                    builder.append(current)
                    builder.append(message[++i])
                    i++
                    continue
                }

                if ((current == MC_COLOR_CHAR || current == MC_ALT_COLOR_CHAR) && i + 1 < length) {
                    // Handle the minecraft color format
                    val nextChar = message[i + 1]
                    val nextCode = nextChar.code

                    if (!formatColors && nextChar == MC_EXPANDED_HEX_COLOR_CHAR && i + 13 < length) {
                        val hex = parseHexExtended(message, i)
                        if (hex != null) {
                            if (remove) {
                                i += 14
                                continue
                            } else {
                                builder.append(ESCAPE_CHAR)
                            }
                        }
                    } else if (!formatColors && nextChar == MC_HEX_COLOR_CHAR && i + 7 < length) {
                        if (isValidHex(message, i)) {
                            if (remove) {
                                i += 8
                                continue
                            } else {
                                builder.append(ESCAPE_CHAR)
                            }
                        }
                    } else if (nextChar == MC_RESET) {
                        if (remove) {
                            i += 2
                            continue
                        } else {
                            builder.append(ESCAPE_CHAR)
                        }
                    } else if (!formatBold && nextChar == MC_BOLD) {
                        if (remove) {
                            i += 2
                            continue
                        } else {
                            builder.append(ESCAPE_CHAR)
                        }
                    } else if (!formatItalic && nextChar == MC_ITALIC) {
                        if (remove) {
                            i += 2
                            continue
                        } else {
                            builder.append(ESCAPE_CHAR)
                        }
                    } else if (!formatColors && nextCode < 128) {
                        val replacement = COLOR_REPLACEMENTS[nextCode]
                        if (replacement != null) {
                            if (remove) {
                                i += 2
                                continue
                            } else {
                                builder.append(ESCAPE_CHAR)
                            }
                        }
                    }
                } else if (current == PLACEHOLDER_START && i + 2 < length) {
                    // Handle my placeholder style format
                    val end = findMatchingEnd(message, i)
                    if (end == -1) break

                    val placeholder = message.substring(i + 1, end)
                    if (placeholder == RESET
                        || (!formatBold && placeholder == BOLD)
                        || (!formatItalic && placeholder == ITALIC)
                        || (!formatMonospaced && placeholder == MONOSPACED)
                        || (!formatRainbow && placeholder.isRainbow())
                        || (!formatColors && placeholder.isColor())
                        || (!formatGradient && placeholder.isGradient())
                        || !formatPlaceholders && !placeholder.isStyle()
                    ) {
                        if (remove) {
                            i = end + 1
                            continue
                        } else {
                            builder.append(ESCAPE_CHAR)
                        }
                    }
                }

                builder.append(current)
                i++
            }

            return builder.toString()
        }

        fun findMatchingEnd(text: String, start: Int): Int {
            var depth = 1
            var i = start + 1
            while (i < text.length) {
                val char = text[i]
                if (char == ESCAPE_CHAR && i + 1 < text.length) {
                    i += 2 // skip escape and the escaped char
                    continue
                }
                if (char == PLACEHOLDER_START) {
                    depth++
                } else if (char == PLACEHOLDER_END) {
                    depth--
                    if (depth == 0) return i
                }
                i++
            }
            return -1
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

        fun parseHexExtended(message: String, index: Int): String? {
            val hexBuilder = StringBuilder(7).append(HEX_COLOR_CHAR)

            for ((hi, h) in (index + 2..index + 13).withIndex()) {
                val hexChar = message[h]
                if (hi % 2 == 0) {
                    if (hexChar != MC_COLOR_CHAR && hexChar != MC_ALT_COLOR_CHAR) {
                        return null
                    }
                } else {
                    hexBuilder.append(hexChar)
                }
            }

            return hexBuilder.toString()
        }

        fun String.isStyle(): Boolean = isFormatting() || isColor() || isRainbow() || isGradient()

        fun String.isFormatting(): Boolean {
            return this == BOLD || this == ITALIC || this == MONOSPACED || this == RESET
        }

        fun String.isColor(): Boolean = (length == 7 || length == 4) && startsWith('#')

        fun String.isRainbow(): Boolean = this == RAINBOW

        fun String.isGradient(): Boolean = length == 15 && startsWith("#") && indexOf('-') != -1
    }

    val root = Message.empty()
    private var topComp: Message? = null
    private var child: Message? = null

    fun newTopComp(): Message {
        val message = Message.empty()
        root.insert(message)
        topComp = message
        child = null
        return message
    }

    fun getCurrentComp(): Message = child ?: topComp ?: root

    fun newChild(): Message {
        val message = Message.empty()
        getCurrentComp().insert(message)
        child = message
        return message
    }

    fun parseColors(message: String): Message {
        val length = message.length

        var color: String? = null
        var gradient: Gradient? = null
        var boldIndex = -1
        var italicIndex = -1
        var monospacedIndex = -1
        val builder = StringBuilder()

        fun flush() {
            if (gradient != null) {
                flush(builder, gradient!!, boldIndex, italicIndex, monospacedIndex)
            } else {
                flush(builder, color, boldIndex, italicIndex, monospacedIndex)
            }

            color = null
            gradient = null
            boldIndex = -1
            italicIndex = -1
            monospacedIndex = -1
            builder.clear()
        }

        var i = 0
        while (i < length) {
            val current = message[i]

            if (current == ESCAPE_CHAR) {
                builder.append(message[++i])
                i++
                continue
            }

            if ((current == MC_COLOR_CHAR || current == MC_ALT_COLOR_CHAR) && i + 1 < length) {
                // Handle the minecraft color format
                val nextChar = message[i + 1]
                val nextCode = nextChar.code

                if (nextChar == MC_EXPANDED_HEX_COLOR_CHAR && i + 13 < length) {
                    val hex = parseHexExtended(message, i)
                    if (hex != null) {
                        flush()

                        color = hex
                        i += 14
                        continue
                    }
                } else if (nextChar == MC_HEX_COLOR_CHAR && i + 7 < length) {
                    if (isValidHex(message, i)) {
                        flush()

                        color = message.substring(i + 1, i + 8)
                        i += 8
                        continue
                    }
                } else if (nextChar == MC_RESET) {
                    flush()
                    i += 2
                    continue
                } else if (nextChar == MC_BOLD) {
                    boldIndex = builder.length
                    i += 2
                    continue
                } else if (nextChar == MC_ITALIC) {
                    italicIndex = builder.length
                    i += 2
                    continue
                } else if (nextCode < 128) {
                    val replacement = COLOR_REPLACEMENTS[nextCode]
                    if (replacement != null) {
                        flush()

                        color = replacement
                        i += 2
                        continue
                    }
                }
            } else if (current == PLACEHOLDER_START && i + 2 < length) {
                // Handle my placeholder style format
                val end = findMatchingEnd(message, i)
                if (end == -1) break

                val placeholder = message.substring(i + 1, end)
                if (!placeholder.isStyle()) {
                    builder.append(PLACEHOLDER_START).append(placeholder).append(PLACEHOLDER_END)
                    i = end + 1
                    continue
                }

                if (placeholder == RESET) {
                    flush()
                    i = end + 1
                    continue
                } else if (placeholder.isFormatting()) {
                    when (placeholder) {
                        BOLD -> boldIndex = builder.length
                        ITALIC -> italicIndex = builder.length
                        MONOSPACED -> monospacedIndex = builder.length
                    }

                    i = end + 1
                    continue
                } else if (placeholder.isRainbow()) {
                    flush()
                    i = end + 1

                    gradient = Gradient.Rainbow
                    continue
                } else if (placeholder.isColor()) {
                    flush()
                    i = end + 1

                    color = placeholder
                    continue
                } else if (placeholder.isGradient()) {
                    flush()
                    i = end + 1

                    val splitIndex = placeholder.indexOf('-')
                    val color1 = placeholder.substring(0, splitIndex)
                    val color2 = placeholder.substring(splitIndex + 1)
                    gradient = Gradient.Linear(color1, color2)
                    continue
                }
            }

            builder.append(current)
            i++
        }

        if (builder.isNotEmpty()) {
            if (gradient != null) {
                flush(builder, gradient!!, boldIndex, italicIndex, monospacedIndex)
            } else {
                flush(builder, color, boldIndex, italicIndex, monospacedIndex)
            }
        }

        return root
    }

    fun flush(buffer: StringBuilder, color: String?, boldIndex: Int, italicIndex: Int, monospacedIndex: Int) {
        if (buffer.isEmpty()) {
            return
        }

        var comp = newTopComp()
        if (color != null) {
            comp.color(color)
        }

        if (boldIndex == -1 && italicIndex == -1 && monospacedIndex == -1) {
            comp.formattedMessage.rawText = buffer.toString()
            return
        }

        if (boldIndex == 0) {
            comp.bold(true)
        }

        if (italicIndex == 0) {
            comp.italic(true)
        }

        if (monospacedIndex == 0) {
            comp.monospace(true)
        }

        var start = 0
        for (i in 1 until buffer.length) {
            if (i == boldIndex || i == italicIndex || i == monospacedIndex) {
                comp.formattedMessage.rawText = buffer.substring(start, i)
                start = i
                comp = newChild()
            }

            if (i == boldIndex) {
                comp.bold(true)
            }
            if (i == italicIndex) {
                comp.italic(true)
            }
            if (i == monospacedIndex) {
                comp.monospace(true)
            }
        }

        comp.formattedMessage.rawText = buffer.substring(start)
    }

    fun flush(
        buffer: StringBuilder,
        gradient: Gradient,
        boldIndex: Int,
        italicIndex: Int,
        monospacedIndex: Int
    ) {
        if (buffer.isEmpty()) {
            return
        }

        val bold = boldIndex == 0
        val italic = italicIndex == 0
        val monospaced = monospacedIndex == 0

        var comp: Message
        val length = buffer.length
        for (i in 0 until length) {
            comp = newTopComp()
            comp.color(gradient.getColorAt(i, length))
            if (bold) comp.bold(true)
            if (italic) comp.italic(true)
            if (monospaced) comp.monospace(true)
            comp.formattedMessage.rawText = buffer[i].toString()
        }
    }
}