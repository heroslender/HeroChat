package com.github.heroslender.herochat

import com.github.heroslender.herochat.dependencies.LuckPermsDependency.prefix
import com.github.heroslender.herochat.dependencies.LuckPermsDependency.suffix
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.dependencies.LuckPermsDependency
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.permissions.PermissionsModule
import com.hypixel.hytale.server.core.universe.PlayerRef

object ComponentParser {
    const val PLACEHOLDER_START = '{'
    const val PLACEHOLDER_END = '}'

    fun parse(
        player: PlayerRef,
        message: String,
        components: Map<String, ComponentConfig>,
        component: Message = Message.empty()
    ): Message {
        if (!message.contains(PLACEHOLDER_START)) {
            return component.insert(message)
        }

        var start = 0

        var formattingComponent: Message? = null
        fun getFormattingComponentOrInit(): Message {
            if (formattingComponent == null) {
                formattingComponent = Message.empty()
            }
            return formattingComponent!!
        }

        var formattingSuffixIndex = -1
        do {
            val prefixIndex: Int = message.indexOf(PLACEHOLDER_START, start)
            if (prefixIndex == -1) {
                break
            }

            val suffixIndex: Int = message.indexOf(PLACEHOLDER_END, prefixIndex)
            if (suffixIndex == -1) {
                break
            }

            val isFormattingChain = formattingComponent != null && prefixIndex == formattingSuffixIndex + 1
            val placeholder = message.substring(prefixIndex + 1, suffixIndex).trim { it <= ' ' }
            if (formattingComponent != null && !isFormattingChain && placeholder.isFormatting()) {
                if (start != prefixIndex) {
                    formattingComponent.insert(message.substring(start, prefixIndex))
                }

                component.insert(formattingComponent)
                formattingComponent = null
            } else if (start != prefixIndex) {
                (formattingComponent ?: component).insert(message.substring(start, prefixIndex))
            }

            if (placeholder.startsWith('#')) {
                getFormattingComponentOrInit().color(placeholder)
            } else {
                when (placeholder) {
                    "bold" -> getFormattingComponentOrInit().bold(true)
                    "italic" -> getFormattingComponentOrInit().italic(true)
                    "monospaced" -> getFormattingComponentOrInit().monospace(true)
                    else -> {
                        val c = components[placeholder]
                        val text = if (c == null) {
                            parsePlaceholder(player, placeholder)
                        } else if (c.permission == null || PermissionsModule.get()
                                .hasPermission(player.uuid, c.permission!!)
                        ) {
                            c.text
                        } else null

                        if (text != null) {
                            parse(player, text, components, formattingComponent ?: component)
                        }
                    }
                }
            }


            if (placeholder.isFormatting()) {
                formattingSuffixIndex = suffixIndex
            }

            start = suffixIndex + 1
        } while (start < message.length)

        if (formattingComponent != null) {
            if (start != message.length) formattingComponent.insert(message.substring(start, message.length))

            component.insert(formattingComponent)
        } else if (start != message.length) {
            component.insert(message.substring(start, message.length))
        }

        return component
    }

    fun parsePlaceholder(player: PlayerRef, placeholder: String): String? {
        val user = LuckPermsDependency.getUser(player)

        return when (placeholder) {
            "luckperms_prefix" -> user?.prefix
            "luckperms_suffix" -> user?.suffix
            "player_username" -> player.username
            else -> null
        }
    }

    fun String.isFormatting(): Boolean {
        return startsWith('#') || this == "bold" || this == "italic" || this == "monospaced"
    }
}