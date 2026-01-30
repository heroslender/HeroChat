package com.github.heroslender.herochat.ui.popup

import com.github.heroslender.herochat.ui.Page
import com.github.heroslender.herochat.ui.event.ActionEventData
import com.github.heroslender.herochat.utils.onActivating
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

class ConfirmationPopup<T: ActionEventData>(
    parent: Page<T>,
    val title: String,
    val message: String,
    val confirmButtonText: String = "Confirm",
    val cancelButtonText: String = "Cancel",
    val onConfirm: ConfirmationPopup<T>.(data: T) -> Unit,
    val onCancel: ConfirmationPopup<T>.(data: T) -> Unit = { closePopup() },
) : Popup<T>(parent, PopupSelector, "HeroChat/Popup/ConfirmationPopup.ui") {

    override fun build(ref: Ref<EntityStore?>, cmd: UICommandBuilder, evt: UIEventBuilder, store: Store<EntityStore?>) {
        cmd["$PopupSelector #PopupTitle.Text"] = title
        cmd["$PopupSelector #PopupMessage.Text"] = message
        cmd["$PopupSelector #ConfirmBtn.Text"] = confirmButtonText
        cmd["$PopupSelector #CancelBtn.Text"] = cancelButtonText

        evt.onActivating("$PopupSelector #ConfirmBtn", "Action" to ActionConfirmPopup)
        evt.onActivating("$PopupSelector #CancelBtn", "Action" to ActionCancelPopup)
    }

    override fun handleDataEvent(
        ref: Ref<EntityStore?>,
        store: Store<EntityStore?>,
        data: T
    ) {
        when (data.action) {
            ActionConfirmPopup -> onConfirm(data)
            ActionCancelPopup -> onCancel(data)
        }
    }

    companion object {
        const val PopupSelector = "#ConfirmationPopup"
        const val ActionConfirmPopup = "ActionConfirmPopup"
        const val ActionCancelPopup = "ActionCancelPopup"
    }
}