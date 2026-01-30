package com.github.heroslender.herochat.ui

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

abstract class SubPage<T>(
    val parent: Page<T>,
    val layoutPath: String,
) {
    val playerRef: PlayerRef get() = parent.playerRef

    abstract fun build(ref: Ref<EntityStore?>, cmd: UICommandBuilder, evt: UIEventBuilder, store: Store<EntityStore?>)

    abstract fun handleDataEvent(ref: Ref<EntityStore?>, store: Store<EntityStore?>, data: T)

    fun runUiCmdUpdate(
        clear: Boolean = false, func: (cmd: UICommandBuilder) -> Unit
    ) = parent.runUiCmdUpdate(clear, func)

    fun runUiCmdEvtUpdate(
        clear: Boolean = false, func: (cmd: UICommandBuilder, evt: UIEventBuilder) -> Unit
    ) = parent.runUiCmdEvtUpdate(clear, func)
}