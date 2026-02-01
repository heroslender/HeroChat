package com.github.heroslender.herochat.utils

import com.github.heroslender.herochat.HeroChat
import com.hypixel.hytale.event.EventPriority
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.console.ConsoleSender
import com.hypixel.hytale.server.core.universe.Universe
import java.util.*


inline fun <reified E> registerEvent(
    priority: EventPriority = EventPriority.NORMAL,
    noinline handler: (e: E) -> Unit
) {
    HeroChat.instance.eventRegistry.register(priority, E::class.java, handler)
}

fun Universe.sendMessage(uuid: UUID, message: Message) {
    if (uuid == ConsoleSender.INSTANCE.uuid) {
        ConsoleSender.INSTANCE.sendMessage(message)
    } else {
        getPlayer(uuid)?.sendMessage(message)
    }
}