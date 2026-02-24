package com.github.heroslender.herochat.message

import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.data.PlayerUser
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.dependencies.PlaceholderAPIDependency
import com.github.heroslender.herochat.message.ColorParser.Companion.isStyle
import com.github.heroslender.herochat.message.MessageParser.ESCAPE_CHAR
import com.github.heroslender.herochat.message.MessageParser.PLACEHOLDER_END
import com.github.heroslender.herochat.message.MessageParser.PLACEHOLDER_START

object PlaceholderManager {

    @JvmStatic
    fun parsePlaceholder(user: User, placeholder: String): String? {
        var result = parseSelfPlaceholders(user, placeholder)
        if (result != null) {
            return result
        }

        if (user is PlayerUser) {
            result = PlaceholderAPIDependency.parsePlaceholder(user.player, placeholder)
            if (result != null) {
                return result
            }
        }

        return null
    }

    fun parseSelfPlaceholders(user: User, placeholder: String): String? = when (placeholder) {
        "player_username" -> user.username
        "player_nickname" -> user.settings.nickname ?: user.username
        else -> null
    }

    fun parsePlaceholders(
        sender: User,
        message: String,
        components: Map<String, ComponentConfig>,
    ): String = parsePlaceholders(sender, message, components, StringBuilder(message.length)).toString()

    fun parsePlaceholders(
        sender: User,
        message: String,
        components: Map<String, ComponentConfig>,
        buffer: StringBuilder,
    ): StringBuilder {
        if (!message.contains(PLACEHOLDER_START)) {
            return buffer.append(message)
        }

        var i = 0
        while (i < message.length) {
            val current = message[i]
            if (current == ESCAPE_CHAR) {
                buffer.append(message[++i])
                i++
                continue
            }

            if (current == PLACEHOLDER_START) {
                val end = findMatchingEnd(message, i)
                if (end == -1) break

                val placeholder = message.substring(i + 1, end)
                if (placeholder.isStyle()) {
                    buffer.append(PLACEHOLDER_START).append(placeholder).append(PLACEHOLDER_END)
                    i = end + 1
                    continue
                }

                val resolvedPlaceholder = parsePlaceholders(sender, placeholder, components).trim()
                val component = components[resolvedPlaceholder]
                val text = if (component == null) {
                    parsePlaceholder(sender, resolvedPlaceholder.toString())
                } else if (component.permission == null || sender.hasPermission(component.permission!!)) {
                    component.text
                } else null

                if (text != null) {
                    parsePlaceholders(sender, text, components, buffer = buffer)
                }

                i = end + 1
                continue
            }

            buffer.append(current)
            i++
        }

        return buffer
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
}