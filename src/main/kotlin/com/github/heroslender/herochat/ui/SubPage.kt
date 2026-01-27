package com.github.heroslender.herochat.ui

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

interface SubPage<T> {
    val playerRef: PlayerRef
    val layoutPath: String

    fun build(ref: Ref<EntityStore?>, cmd: UICommandBuilder, evt: UIEventBuilder, store: Store<EntityStore?>)

    fun handleDataEvent(
        ref: Ref<EntityStore?>,
        store: Store<EntityStore?>,
        data: T
    )
}