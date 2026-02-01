package com.github.heroslender.herochat.utils

import com.github.heroslender.herochat.HeroChat
import com.hypixel.hytale.event.EventPriority


inline fun <reified E> registerEvent(
    priority: EventPriority = EventPriority.NORMAL,
    noinline handler: (e: E) -> Unit
) {
    HeroChat.instance.eventRegistry.register(priority, E::class.java, handler)
}