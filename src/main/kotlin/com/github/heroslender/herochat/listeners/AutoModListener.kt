package com.github.heroslender.herochat.listeners

import com.github.heroslender.herochat.Permissions
import com.github.heroslender.herochat.config.AutoModConfig
import com.github.heroslender.herochat.config.ChatConfig
import com.github.heroslender.herochat.event.PreChatEvent
import com.github.heroslender.herochat.message.ComponentParser
import com.github.heroslender.herochat.utils.registerEvent
import java.util.regex.Pattern

class AutoModListener(val config: ChatConfig, val autoModConfig: AutoModConfig) {
    private val regexPatterns: Map<String, Pattern> = autoModConfig.rules
        .filter { it.isRegex }
        .flatMap { it.patterns.toList() }
        .map { it to Pattern.compile(it) }
        .let { mapOf(*it.toTypedArray()) }

    init {
        registerEvent<PreChatEvent> { e ->
            if (!autoModConfig.enabled || e.sender.hasPermission(Permissions.BYPASS_AUTOMOD)) {
                return@registerEvent
            }

            var message = e.message
            for (rule in autoModConfig.rules) {
                if (rule.isRegex) {
                    for (patternStr in rule.patterns) {
                        val pattern = regexPatterns[patternStr] ?: continue
                        val matcher = pattern.matcher(message)
                        if (!matcher.find()) {
                            continue
                        }

                        if (rule.replacement != null) {
                            message = matcher.replaceAll(rule.replacement)
                            continue
                        }

                        e.sender.sendMessage(
                            ComponentParser.parse(
                                sender = e.sender,
                                message = rule.blockMessage ?: autoModConfig.defaultBlockMessage,
                                components = config.components
                            )
                        )
                        e.isCancelled = true
                    }
                } else {
                    for (pattern in rule.patterns) {
                        if (!message.contains(pattern, ignoreCase = true)) {
                            continue
                        }

                        if (rule.replacement != null) {
                            message = message.replace(pattern, rule.replacement!!, ignoreCase = true)
                            continue
                        }

                        e.sender.sendMessage(
                            ComponentParser.parse(
                                sender = e.sender,
                                message = rule.blockMessage ?: autoModConfig.defaultBlockMessage,
                                components = config.components
                            )
                        )
                        e.isCancelled = true
                    }
                }
            }

            e.message = message
        }
    }
}