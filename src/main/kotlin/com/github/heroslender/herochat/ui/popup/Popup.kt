package com.github.heroslender.herochat.ui.popup

import com.github.heroslender.herochat.ui.Page
import com.github.heroslender.herochat.ui.UiComponent
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

abstract class Popup<T>(
    override val parent: Page<T>,
    override val selector: String,
    override val layoutPath: String
) : UiComponent<T> {

    fun openPopup(ref: Ref<EntityStore?>, store: Store<EntityStore?>) {
        parent.openPopup(ref, store, this)
    }

    fun closePopup() {
        parent.runUiCmdUpdate { cmd ->
            cmd.remove(selector)
        }

        unregister()
    }
}