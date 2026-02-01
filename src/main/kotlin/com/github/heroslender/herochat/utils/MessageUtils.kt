package com.github.heroslender.herochat.utils

import com.github.heroslender.herochat.ComponentParser
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.config.MessagesConfig
import com.hypixel.hytale.server.core.command.system.CommandSender
import kotlin.reflect.KProperty1

fun CommandSender.sendMessage(
    msgProp: KProperty1<MessagesConfig, String>,
    vararg placeholders: Map.Entry<String, String>,
) {
    val message = msgProp.get(HeroChat.instance.messages)
    val placeholderComponents = if (placeholders.isNotEmpty())
        mapOf(*placeholders.map { (it.key to ComponentConfig(it.value)) }.toTypedArray())
    else emptyMap()

    val parsedMsg = ComponentParser.parse(uuid, message, placeholderComponents)
    sendMessage(parsedMsg)
}