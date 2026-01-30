package com.github.heroslender.herochat.ui

import com.github.heroslender.herochat.ui.popup.Popup
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

abstract class Page<T>(
    playerRef: PlayerRef,
    eventDataCodec: BuilderCodec<T>,
    lifetime: CustomPageLifetime = CustomPageLifetime.CanDismiss,
) : InteractiveCustomUIPage<T>(playerRef, lifetime, eventDataCodec) {
    val playerRef: PlayerRef get() = super.playerRef
    private val components: MutableList<UiComponent<T>> = mutableListOf()

    override fun handleDataEvent(
        ref: Ref<EntityStore?>,
        store: Store<EntityStore?>,
        data: T & Any
    ) {
        if (components.isEmpty()) {
            return
        }

        if (components.size == 1) {
            components[0].handleDataEvent(ref, store, data)
            return
        }

        ArrayList(components).forEach { it.handleDataEvent(ref, store, data) }
    }

    fun closePage() {
        close()
    }

    fun runUiCmdUpdate(
        clear: Boolean = false, func: (cmd: UICommandBuilder) -> Unit
    ) {
        val cmd = UICommandBuilder()
        func(cmd)
        sendUpdate(cmd, clear)
    }

    fun runUiCmdEvtUpdate(
        clear: Boolean = false, func: (cmd: UICommandBuilder, evt: UIEventBuilder) -> Unit
    ) {
        val cmd = UICommandBuilder()
        val evt = UIEventBuilder()
        func(cmd, evt)
        sendUpdate(cmd, evt, clear)
    }

    fun openPopup(ref: Ref<EntityStore?>, store: Store<EntityStore?>, popup: Popup<T>) =
        runUiCmdEvtUpdate { cmd, evt ->
            cmd.append(popup.layoutPath)
            popup.build(ref, cmd, evt, store)
            registerComponent(popup)
        }

    fun registerComponent(component: UiComponent<T>) {
        components.add(component)
    }

    fun unregisterComponent(component: UiComponent<T>) {
        components.remove(component)
    }
}