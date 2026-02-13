package com.github.heroslender.herochat

import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.dependencies.PlaceholderAPIDependency
import com.github.heroslender.herochat.utils.hasPermission
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.console.ConsoleSender
import com.hypixel.hytale.server.core.universe.Universe
import java.util.*

class ComponentParser {
    companion object {
        const val PLACEHOLDER_START = '{'
        const val PLACEHOLDER_END = '}'
        private const val ESCAPE_CHAR = '\\'

        const val BOLD = "bold"
        const val ITALIC = "italic"
        const val MONOSPACED = "monospaced"

        fun parse(
            sender: UUID,
            message: String,
            components: Map<String, ComponentConfig> = emptyMap(),
        ): Message {
            return ComponentParser().parse(sender, message, components)
        }
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

    fun parse(
        sender: UUID,
        message: String,
        components: Map<String, ComponentConfig> = emptyMap(),
        formatColors: Boolean = true,
        formatStyle: Boolean = true,
        formatPlaceholders: Boolean = true,
    ): Message {
        if (!message.contains(PLACEHOLDER_START)) {
            return getCurrentComp().insert(message)
        }

        var start = 0
        var formattingSuffixIndex = -2
        while (start < message.length) {
            val prefixIndex: Int = message.indexOf(PLACEHOLDER_START, start)
            if (prefixIndex == -1) break

            if (isEscaped(message, prefixIndex, start)) {
                val target = getCurrentComp()
                if (prefixIndex > start) {
                    target.insert(message.substring(start, prefixIndex - 1))
                }
                target.insert(PLACEHOLDER_START.toString())
                start = prefixIndex + 1
                continue
            }

            val suffixIndex: Int = findMatchingEnd(message, prefixIndex)
            if (suffixIndex == -1) break

            fun appendPending() {
                if (start != prefixIndex) {
                    getCurrentComp().insert(message.substring(start, prefixIndex))
                }
            }

            val placeholder = message.substring(prefixIndex + 1, suffixIndex)
            if (placeholder.isColor()) {
                if (!formatColors) {
                    getCurrentComp().insert(message.substring(start, suffixIndex + 1))
                    start = suffixIndex + 1
                    continue
                }

                appendPending()
                val comp = newTopComp()
                comp.color(placeholder)
            } else if (placeholder.isFormatting()) {
                if (!formatStyle) {
                    getCurrentComp().insert(message.substring(start, suffixIndex + 1))
                    start = suffixIndex + 1
                    continue
                }

                val isFormattingChain = prefixIndex == formattingSuffixIndex + 1
                val child = if (isFormattingChain) getCurrentComp() else {
                    appendPending()
                    newChild()
                }

                when (placeholder) {
                    BOLD -> child.bold(true)
                    ITALIC -> child.italic(true)
                    MONOSPACED -> child.monospace(true)
                }
            } else {
                if (!formatPlaceholders) {
                    getCurrentComp().insert(message.substring(start, suffixIndex + 1))
                    start = suffixIndex + 1
                    continue
                }
                appendPending()

                val resolvedPlaceholder = resolveToString(sender, placeholder, components).trim()
                val c = components[resolvedPlaceholder]
                val text = if (c == null) {
                    parsePlaceholder(sender, resolvedPlaceholder)
                } else if (c.permission == null || sender.hasPermission(c.permission!!)) {
                    c.text
                } else null

                if (text != null) {
                    if (placeholder == "message") {
                        parse(
                            sender = sender,
                            message = text,
                            components = components,
                            formatColors = sender.hasPermission(Permissions.CHAT_COLOR),
                            formatStyle = sender.hasPermission(Permissions.CHAT_FORMATTING),
                            formatPlaceholders = false
                        )
                    } else {
                        parse(sender, text, components)
                    }
                }
            }

            if (placeholder.isFormatting()) {
                formattingSuffixIndex = suffixIndex
            }

            start = suffixIndex + 1
        }

        if (start != message.length) {
            getCurrentComp().insert(message.substring(start, message.length))
        }

        return root
    }

    // Resolve sub-placeholder, eg. {player_prefix_{target}}
    fun resolveToString(sender: UUID, message: String, components: Map<String, ComponentConfig>): String {
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
            if (!placeholder.isFormatting()) {
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

    private fun isEscaped(text: String, index: Int, startLimit: Int): Boolean {
        var count = 0
        var i = index - 1
        while (i >= startLimit && text[i] == ESCAPE_CHAR) {
            count++
            i--
        }
        return count % 2 != 0
    }

    fun parsePlaceholder(sender: UUID, placeholder: String): String? {
        if (sender == ConsoleSender.INSTANCE.uuid) {
            if (placeholder.equals("player_username", ignoreCase = true)) {
                return ConsoleSender.INSTANCE.displayName
            }

            return null
        }

        return PlaceholderAPIDependency.parsePlaceholder(Universe.get().getPlayer(sender), placeholder)
    }

    fun String.isFormatting(): Boolean {
        return isColor() || this == BOLD || this == ITALIC || this == MONOSPACED
    }

    fun String.isColor(): Boolean = startsWith('#')
}