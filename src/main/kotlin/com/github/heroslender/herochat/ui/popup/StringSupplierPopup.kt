package com.github.heroslender.herochat.ui.popup

import com.github.heroslender.herochat.ui.Page
import com.github.heroslender.herochat.ui.event.ActionEventData
import com.github.heroslender.herochat.utils.onActivating
import com.github.heroslender.herochat.utils.onValueChanged
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

open class StringSupplierPopup<T: StringSupplierPopup.EventData>(
    parent: Page<T>,
    val title: String,
    val label: String = "Text",
    val value: String? = null,
    val confirmBtnText: String = "Save",
    val cancelBtnText: String = "Cancel",
    val valueValidator: (String) -> String? = { null },
    val onCancel: StringSupplierPopup<T>.(data: T) -> Unit = { closePopup() },
    val onConfirm: StringSupplierPopup<T>.(value: String?, data: T) -> Unit,
) : Popup<T>(parent, PopupSelector, "HeroChat/Popup/StringSupplierPopup.ui") {

    var error: String? = null

    override fun build(ref: Ref<EntityStore?>, cmd: UICommandBuilder, evt: UIEventBuilder, store: Store<EntityStore?>) {
        cmd["$PopupSelector #PopupTitle.Text"] = title
        cmd["$PopupSelector #TfLabel.Text"] = label
        cmd["$PopupSelector #Confirm.Text"] = confirmBtnText
        cmd["$PopupSelector #Cancel.Text"] = cancelBtnText

        if (value != null) {
            cmd["$PopupSelector #Tf.Value"] = value
        }

        evt.onValueChanged(
            "$PopupSelector #Tf",
            EventData.FieldSuppliedString to "$PopupSelector #Tf.Value"
        )

        evt.onActivating("$PopupSelector #Cancel", EventData.Action to ActionCancel)
        evt.onActivating(
            "$PopupSelector #Confirm",
            EventData.Action to ActionConfirm,
            EventData.FieldSuppliedString to "$PopupSelector #Tf.Value",
        )
    }

    fun showError(error: String) {
        parent.runUiCmdUpdate { cmd ->
            cmd["$PopupSelector #Error.Visible"] = true
            cmd["$PopupSelector #Error #Txt.Text"] = error
            this.error = error
        }
    }

    override fun handleDataEvent(
        ref: Ref<EntityStore?>,
        store: Store<EntityStore?>,
        data: T
    ) {
        if (data.suppliedString != null) {
            val error = valueValidator(data.suppliedString!!)
            if (this.error != null && error == null) {
                parent.runUiCmdUpdate { cmd ->
                    cmd["$PopupSelector #Error.Visible"] = false
                    this.error = null
                }

                return
            }

            if (error != null) {
                showError(error)
                return
            }
        }

        when (data.action) {
            ActionConfirm -> onConfirm(data.suppliedString!!, data)
            ActionCancel -> onCancel(data)
        }
    }

    companion object {
        const val PopupSelector = "#StringSupplierPopup"
        const val ActionConfirm = "ActionConfirmStringSupplierPopup"
        const val ActionCancel = "ActionCancelStringSupplierPopup"
    }

    interface EventData: ActionEventData {
        var suppliedString: String?

        companion object {
            const val Action = ActionEventData.Action

            const val FieldSuppliedString = "@SuppliedString"

            fun <T : EventData> BuilderCodec.Builder<T>.appendStringSupplierPopupEventData(): BuilderCodec.Builder<T> {
                return this
                    .append(
                        KeyedCodec(FieldSuppliedString, Codec.STRING),
                        { e, v -> e.suppliedString = v },
                        { e -> e.suppliedString }).add()
            }
        }
    }
}