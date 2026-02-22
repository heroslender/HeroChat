package com.github.heroslender.herochat.ui.popup

import com.github.heroslender.herochat.ui.Page
import com.github.heroslender.herochat.ui.event.ActionEventData
import com.github.heroslender.herochat.utils.onActivating
import com.github.heroslender.herochat.utils.onValueChanged
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

class RulePatternPopup<T>(
    parent: Page<T>,
    val pattern: String? = null,
    val onCancel: RulePatternPopup<T>.(data: T) -> Unit = { closePopup() },
    val onConfirm: RulePatternPopup<T>.(pattern: String?, data: T) -> Unit,
) : Popup<T>(parent, PopupSelector, "HeroChat/SubPage/Automod/RulePatternPopup.ui")
        where T : RulePatternPopup.EventData, T : ActionEventData {

    var error: String? = null

    override fun build(ref: Ref<EntityStore?>, cmd: UICommandBuilder, evt: UIEventBuilder, store: Store<EntityStore?>) {
        if (pattern != null) {
            cmd["$PopupSelector #PopupTitle.Text"] = "Edit Pattern"
            cmd["$PopupSelector #AddBtn.Text"] = "Save"

            cmd["$PopupSelector #Pattern.Value"] = pattern
        }

        evt.onValueChanged("$PopupSelector #Pattern", "@RulePopupPattern" to "$PopupSelector #Pattern.Value")
        evt.onActivating("$PopupSelector #Close", "Action" to ActionCancel)
        evt.onActivating(
            "$PopupSelector #AddBtn",
            "Action" to ActionConfirm,
            "@RulePopupPattern" to "$PopupSelector #Pattern.Value",
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
        if (data.rulePopupPattern != null && error != null) {
            parent.runUiCmdUpdate { cmd ->
                cmd["$PopupSelector #Error.Visible"] = false
                this.error = null
            }
        }

        when (data.action) {
            ActionConfirm -> onConfirm(data.rulePopupPattern!!, data)
            ActionCancel -> onCancel(data)
        }
    }

    companion object {
        const val PopupSelector = "#RulePatternPopup"
        const val ActionConfirm = "ActionConfirmRulePatternPopup"
        const val ActionCancel = "ActionCancelRulePatternPopup"
    }

    interface EventData {
        val rulePopupPattern: String?
    }
}