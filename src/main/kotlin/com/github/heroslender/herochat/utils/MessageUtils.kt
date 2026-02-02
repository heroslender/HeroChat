package com.github.heroslender.herochat.utils

import com.github.heroslender.herochat.ComponentParser
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.config.MessagesConfig
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandSender
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.*
import kotlin.reflect.KProperty1

fun CommandSender.sendMessage(
    msgProp: KProperty1<MessagesConfig, String>,
    vararg placeholders: Pair<String, String>,
): Unit = sendMessage(messageFromConfig(msgProp, uuid, *placeholders))

fun messageFromConfig(
    msgProp: KProperty1<MessagesConfig, String>,
    target: PlayerRef,
    vararg placeholders: Pair<String, String>,
): Message = messageFromConfig(msgProp, target.uuid, *placeholders)

fun messageFromConfig(
    msgProp: KProperty1<MessagesConfig, String>,
    target: UUID,
    vararg placeholders: Pair<String, String>,
): Message {
    val message = msgProp.get(HeroChat.instance.messages)
    val placeholderComponents = if (placeholders.isNotEmpty())
        mapOf(*placeholders.map { (key, value) -> (key to ComponentConfig(value)) }.toTypedArray())
    else emptyMap()

    return ComponentParser.parse(target, message, placeholderComponents)
}