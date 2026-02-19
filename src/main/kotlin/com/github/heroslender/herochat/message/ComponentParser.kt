package com.github.heroslender.herochat.message

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.Permissions
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.data.User
import com.hypixel.hytale.server.core.Message

class ComponentParser(
    val parseMcColors: Boolean = HeroChat.instance.config.enableMinecraftColors
) {
    companion object {
        const val PLACEHOLDER_START = '{'
        const val PLACEHOLDER_END = '}'
        const val ESCAPE_CHAR = '\\'

        const val RAINBOW = "rainbow"
        const val RESET = "reset"
        const val BOLD = "bold"
        const val ITALIC = "italic"
        const val MONOSPACED = "monospaced"

        fun parse(
            sender: User,
            message: String,
            components: Map<String, ComponentConfig> = emptyMap(),
        ): Message {
            return ComponentParser().parse(sender, message, components)
        }

        fun validateFormat(
            user: User,
            message: String,
            basePermission: String,
            remove: Boolean = false,
        ): String = validateFormat(
            message = message,
            formatColors = user.hasPermission("$basePermission.colors"),
            formatRainbow = user.hasPermission("$basePermission.$RAINBOW"),
            formatGradient = user.hasPermission("$basePermission.gradient"),
            formatBold = user.hasPermission("$basePermission.$BOLD"),
            formatItalic = user.hasPermission("$basePermission.$ITALIC"),
            formatMonospaced = user.hasPermission("$basePermission.$MONOSPACED"),
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
            remove: Boolean = false,
        ): String {
            val parseMcColors: Boolean = HeroChat.instance.config.enableMinecraftColors
            val message = if (parseMcColors) McColorParser.parse(message) else message
            if (!message.contains(PLACEHOLDER_START)) {
                return message
            }

            val buffer = StringBuilder(message.length)
            var start = 0
            while (start < message.length) {
                val prefixIndex: Int = message.indexOf(PLACEHOLDER_START, start)
                if (prefixIndex == -1) break

                if (isEscaped(message, prefixIndex, start)) {
                    buffer.append(message, start, prefixIndex + 1)
                    start = prefixIndex + 1
                    continue
                }

                val suffixIndex: Int = findMatchingEnd(message, prefixIndex)
                if (suffixIndex == -1) break

                fun remove() {
                    if (remove) {
                        buffer.append(message, start, prefixIndex)
                        start = suffixIndex + 1
                    } else {
                        buffer.append(ESCAPE_CHAR)
                        buffer.append(message, start, suffixIndex + 1)
                        start = suffixIndex + 1
                    }
                }

                val placeholder = message.substring(prefixIndex + 1, suffixIndex)
                if (placeholder == BOLD && !formatBold) {
                    remove()
                    continue
                } else if (placeholder == ITALIC && !formatItalic) {
                    remove()
                    continue
                } else if (placeholder == MONOSPACED && !formatMonospaced) {
                    remove()
                    continue
                } else if (placeholder == RAINBOW && !formatRainbow) {
                    remove()
                    continue
                } else if (placeholder.isColor() && !formatColors) {
                    remove()
                    continue
                } else if (placeholder.isGradient() && !formatGradient) {
                    remove()
                    continue
                } else if (!placeholder.isStyle()) {
                    // It's a placeholder, ignore it
                    buffer.append(ESCAPE_CHAR)
                }

                buffer.append(message, start, suffixIndex + 1)
                start = suffixIndex + 1
            }

            if (start != message.length) {
                buffer.append(message, start, message.length)
            }

            return buffer.toString()
        }

        fun stripStyle(
            message: String,
            parseMcColors: Boolean = HeroChat.instance.config.enableMinecraftColors
        ): String {
            val message = if (parseMcColors) McColorParser.parse(message) else message
            if (!message.contains(PLACEHOLDER_START)) {
                return message
            }

            val buffer = StringBuilder(message.length)
            var start = 0
            while (start < message.length) {
                val prefixIndex: Int = message.indexOf(PLACEHOLDER_START, start)
                if (prefixIndex == -1) break

                if (isEscaped(message, prefixIndex, start)) {
                    buffer.append(message, start, prefixIndex + 1)
                    start = prefixIndex + 1
                    continue
                }

                val suffixIndex: Int = findMatchingEnd(message, prefixIndex)
                if (suffixIndex == -1) break

                val placeholder = message.substring(prefixIndex + 1, suffixIndex)
                if (placeholder.isStyle()) {
                    buffer.append(message, start, prefixIndex)
                    start = suffixIndex + 1
                    continue
                }

                buffer.append(message, start, suffixIndex + 1)
                start = suffixIndex + 1
            }

            if (start != message.length) {
                buffer.append(message, start, message.length)
            }

            return buffer.toString()
        }

        private fun isEscaped(text: String, index: Int, startLimit: Int): Boolean {
            var count = 0
            var i = index - 1
            while (i >= startLimit && text[i] == ESCAPE_CHAR) {
                count++
                i--
            }
            return count % 2 != 0
        }

        private fun findMatchingEnd(text: String, start: Int): Int {
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

    fun parsePlaceholders(
        sender: User,
        message: String,
        components: Map<String, ComponentConfig>,
        formatColors: Boolean = true,
        formatStyle: Boolean = true,
        formatPlaceholders: Boolean = true,
        buffer: StringBuilder = StringBuilder(message.length)
    ): StringBuilder {
        val message = if (parseMcColors && (formatColors || formatStyle)) McColorParser.parse(message) else message
        if (!message.contains(PLACEHOLDER_START)) {
            return buffer.append(message)
        }

        var start = 0
        while (start < message.length) {
            val prefixIndex: Int = message.indexOf(PLACEHOLDER_START, start)
            if (prefixIndex == -1) break

            if (isEscaped(message, prefixIndex, start)) {
                buffer.append(message, start, prefixIndex + 1)
                start = prefixIndex + 1
                continue
            }

            val suffixIndex: Int = findMatchingEnd(message, prefixIndex)
            if (suffixIndex == -1) break

            val placeholder = message.substring(prefixIndex + 1, suffixIndex)
            if (placeholder.isStyle()) {
                if ((!formatColors && (placeholder.isColor() || placeholder.isRainbow() || placeholder.isGradient()))
                    || !formatStyle && placeholder.isFormatting()
                ) {
                    buffer.append(ESCAPE_CHAR)
                }
                buffer.append(message, start, suffixIndex + 1)
                start = suffixIndex + 1
                continue
            } else {
                if (!formatPlaceholders) {
                    buffer.append(message, start, suffixIndex + 1)
                    start = suffixIndex + 1
                    continue
                }

                if (start != prefixIndex) {
                    buffer.append(message, start, prefixIndex)
                }

                val resolvedPlaceholder = resolveToString(sender, placeholder, components).trim()
                val c = components[resolvedPlaceholder]
                val text = if (c == null) {
                    parsePlaceholder(sender, resolvedPlaceholder)
                } else if (c.permission == null || sender.hasPermission(c.permission!!)) {
                    c.text
                } else null

                if (text != null) {
                    if (placeholder == "message") {
                        parsePlaceholders(
                            sender = sender,
                            message = text,
                            components = components,
                            formatColors = sender.hasPermission(Permissions.CHAT_COLOR),
                            formatStyle = sender.hasPermission(Permissions.CHAT_FORMATTING),
                            formatPlaceholders = false,
                            buffer = buffer
                        )
                    } else {
                        parsePlaceholders(sender, text, components, buffer = buffer)
                    }
                }
            }

            start = suffixIndex + 1
        }

        if (start != message.length) {
            buffer.append(message, start, message.length)
        }

        return buffer
    }

    fun parse(
        sender: User,
        message: String,
        components: Map<String, ComponentConfig> = emptyMap(),
    ): Message {
        val message = parsePlaceholders(sender, message, components).toString()
        if (!message.contains(PLACEHOLDER_START)) {
            return getCurrentComp().insert(message)
        }

        var start = 0

        var color: String? = null
        var gradient: Gradient? = null
        var boldIndex = -1
        var italicIndex = -1
        var monospacedIndex = -1
        val buffer = StringBuilder()

        while (start < message.length) {
            val prefixIndex: Int = message.indexOf(PLACEHOLDER_START, start)
            if (prefixIndex == -1) break

            if (isEscaped(message, prefixIndex, start)) {
                buffer.append(message, start, prefixIndex - 1).append(PLACEHOLDER_START)
                start = prefixIndex + 1
                continue
            }

            val suffixIndex: Int = findMatchingEnd(message, prefixIndex)
            if (suffixIndex == -1) break

            fun flush() {
                buffer.append(message, start, prefixIndex)
                if (gradient != null) {
                    flush(buffer, gradient!!, boldIndex, italicIndex, monospacedIndex)
                } else {
                    flush(buffer, color, boldIndex, italicIndex, monospacedIndex)
                }

                start = suffixIndex + 1
                color = null
                gradient = null
                boldIndex = -1
                italicIndex = -1
                monospacedIndex = -1
                buffer.clear()
            }

            val placeholder = message.substring(prefixIndex + 1, suffixIndex)
            if (placeholder.isFormatting()) {
                when (placeholder) {
                    BOLD -> boldIndex = prefixIndex - start
                    ITALIC -> italicIndex = prefixIndex - start
                    MONOSPACED -> monospacedIndex = prefixIndex - start
                }
            } else if (placeholder.isRainbow()) {
                flush()

                gradient = Gradient.Rainbow
                continue
            } else if (placeholder.isColor()) {
                flush()

                color = placeholder
                continue
            } else if (placeholder.isGradient()) {
                flush()

                val splitIndex = placeholder.indexOf('-')
                val color1 = placeholder.substring(0, splitIndex)
                val color2 = placeholder.substring(splitIndex + 1)
                gradient = Gradient.Linear(color1, color2)
                continue
            } else {
                buffer.append(message, start, suffixIndex + 1)
                start = suffixIndex + 1
                continue
            }

            buffer.append(message, start, prefixIndex)
            start = suffixIndex + 1
        }

        if (start != message.length) {
            buffer.append(message, start, message.length)
            if (gradient != null) {
                flush(buffer, gradient!!, boldIndex, italicIndex, monospacedIndex)
            } else {
                flush(buffer, color, boldIndex, italicIndex, monospacedIndex)
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

    // Resolve sub-placeholder, eg. {player_prefix_{target}}
    fun resolveToString(sender: User, message: String, components: Map<String, ComponentConfig>): String {
        if (!message.contains(PLACEHOLDER_START)) return message

        val sb = StringBuilder()
        var start = 0

        while (start < message.length) {
            val prefixIndex = message.indexOf(PLACEHOLDER_START, start)
            if (prefixIndex == -1) {
                sb.append(message.substring(start))
                break
            }

            if (isEscaped(message, prefixIndex, start)) {
                sb.append(message.substring(start, prefixIndex - 1))
                sb.append(PLACEHOLDER_START)
                start = prefixIndex + 1
                continue
            }

            val suffixIndex = findMatchingEnd(message, prefixIndex)
            if (suffixIndex == -1) {
                sb.append(message.substring(start))
                break
            }

            sb.append(message.substring(start, prefixIndex))

            val placeholderRaw = message.substring(prefixIndex + 1, suffixIndex)
            val placeholder = resolveToString(sender, placeholderRaw, components).trim()
            if (!placeholder.isStyle()) {
                val c = components[placeholder]
                val text = if (c == null) {
                    parsePlaceholder(sender, placeholder)
                } else if (c.permission == null || sender.hasPermission(c.permission!!)) {
                    c.text
                } else null

                if (text != null) {
                    sb.append(resolveToString(sender, text, components))
                }
            } else {
                sb.append(PLACEHOLDER_START).append(placeholderRaw).append(PLACEHOLDER_END)
            }

            start = suffixIndex + 1
        }
        return sb.toString()
    }

    fun parsePlaceholder(sender: User, placeholder: String): String? =
        PlaceholderManager.parsePlaceholder(sender, placeholder)
}