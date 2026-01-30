package com.github.heroslender.herochat.ui.popup

import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.ui.Page
import com.github.heroslender.herochat.utils.onActivating
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

class ComponentPopup<T>(
    parent: Page<T>,
    val componentId: String? = null,
    val component: ComponentConfig? = null,
    val onEvent: ComponentPopup<T>.(data: T) -> Unit,
): Popup<T>(parent, PopupSelector, "HeroChat/Popup/AddChatComponent.ui") {

    override fun build(ref: Ref<EntityStore?>, cmd: UICommandBuilder, evt: UIEventBuilder, store: Store<EntityStore?>) {
        if (component != null) {
            cmd["$PopupSelector #PopupTitle.Text"] = "Edit Component"
            cmd["$PopupSelector #AddBtn.Text"] = "Save"
            cmd["$PopupSelector #Tag.Value"] = componentId!!
            cmd["$PopupSelector #Permission.Value"] = component.permission ?: ""
            cmd["$PopupSelector #Format.Value"] = component.text
        }

        evt.onActivating("$PopupSelector #Close", "Action" to ActionCancelPopup)
        evt.onActivating(
            "$PopupSelector #AddBtn",
            "Action" to ActionConfirmPopup,
            "@CId" to "$PopupSelector #Tag.Value",
            "@CText" to "$PopupSelector #Format.Value",
            "@CPerm" to "$PopupSelector #Permission.Value",
        )
    }

    override fun handleDataEvent(
        ref: Ref<EntityStore?>,
        store: Store<EntityStore?>,
        data: T
    ) {
        onEvent(data)
    }

    companion object {
        const val PopupSelector = "#ComponentPopup"
        const val ActionConfirmPopup = "ActionConfirmComponentPopup"
        const val ActionCancelPopup = "ActionCancelComponentPopup"
    }
}