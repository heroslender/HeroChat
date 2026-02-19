package com.github.heroslender.herochat.utils

import com.github.heroslender.herochat.message.ComponentParser
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.data.User
import com.hypixel.hytale.server.core.Message
import kotlin.reflect.KProperty1

fun User.sendMessage(
    msgProp: KProperty1<MessagesConfig, String>,
    vararg placeholders: Pair<String, String>,
): Unit = sendMessage(messageFromConfig(msgProp, this, *placeholders))

fun messageFromConfig(
    msgProp: KProperty1<MessagesConfig, String>,
    target: User,
    vararg placeholders: Pair<String, String>,
): Message {
    val message = messageStrFromConfig(msgProp)

    val placeholderComponents = mutableMapOf<String, ComponentConfig>()
    placeholderComponents.putAll(HeroChat.instance.config.components)
    if (placeholders.isNotEmpty()) {
        placeholderComponents.putAll(placeholders.map { (key, value) -> (key to ComponentConfig(value)) })
    }

    return ComponentParser.parse(target, message, placeholderComponents)
}

fun messageStrFromConfig(
    msgProp: KProperty1<MessagesConfig, String>,
) = msgProp.get(HeroChat.instance.messages)
