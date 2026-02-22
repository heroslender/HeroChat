package com.github.heroslender.herochat.ui.popup

import com.github.heroslender.herochat.config.AutoModRule
import com.github.heroslender.herochat.ui.Page
import com.github.heroslender.herochat.ui.event.ActionEventData
import com.github.heroslender.herochat.ui.pages.settings.ChatSettingsPage
import com.github.heroslender.herochat.utils.onActivating
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.NotificationUtil

class RulePopup<T>(
    parent: Page<T>,
    val rule: AutoModRule? = null,
    val onEvent: RulePopup<T>.(data: T) -> Unit,
) : Popup<T>(parent, PopupSelector, "HeroChat/SubPage/Automod/AddRulePopup.ui")
        where T : ActionEventData, T : RulePatternPopup.EventData, T : RulePopup.EventData {

    private val updatedData: UpdatedData = UpdatedData()

    override fun build(ref: Ref<EntityStore?>, cmd: UICommandBuilder, evt: UIEventBuilder, store: Store<EntityStore?>) {
        if (rule != null) {
            cmd["$PopupSelector #PopupTitle.Text"] = "Edit Rule"
            cmd["$PopupSelector #AddBtn.Text"] = "Save"

            cmd["$PopupSelector #IsRegexCb #CheckBox.Value"] = rule.isRegex
            cmd["$PopupSelector #Replacement.Value"] = rule.replacement ?: ""
            cmd["$PopupSelector #BlockMessage.Value"] = rule.blockMessage ?: ""

            updatePatternList(cmd, evt)
        }

        evt.onActivating("$PopupSelector #NewPatternBtn", "Action" to ActionNewPattern)
        evt.onActivating("$PopupSelector #Close", "Action" to ActionCancel)

    }

    override fun handleDataEvent(
        ref: Ref<EntityStore?>,
        store: Store<EntityStore?>,
        data: T
    ) {
        if (data.action == ActionNewPattern || data.action == ActionEditPattern) {
            val i = data.ruleIndex
            val pattern = i?.let { updatedData.patterns?.get(it) ?: rule?.patterns?.get(it) }

            RulePatternPopup(parent, pattern) { pattern, data ->
                if (pattern.isNullOrBlank()) {
                    NotificationUtil.sendNotification(
                        parent.playerRef.packetHandler,
                        Message.raw("Pattern is empty!"),
                        NotificationStyle.Danger
                    )
                    return@RulePatternPopup
                }

                addPattern(i, data.rulePopupPattern!!,this)
            }.openPopup(ref, store)
            return
        } else if (data.action == ActionDeletePattern) {
            println("Deleting pattern ${data.ruleIndex}")
            val i = data.ruleIndex!!
            var patterns = updatedData.patterns
            if (patterns == null) {
                patterns = rule?.patterns?.toMutableList() ?: mutableListOf()
                updatedData.patterns = patterns
            }

            patterns.removeAt(i)
            parent.runUiCmdEvtUpdate { cmd, evt ->
                updatePatternList(cmd, evt)
            }
            return
        }
        onEvent(data)
    }

    fun addPattern(index: Int?, pattern: String, popup: RulePatternPopup<*>) {
        var patterns = updatedData.patterns
        if (patterns == null) {
            patterns = rule?.patterns?.toMutableList() ?: mutableListOf()
            updatedData.patterns = patterns
        }

        patterns.forEachIndexed { i, p ->
            if (p == pattern) {
                if (index == null || i != index) {
                    popup.showError("Pattern already exists!")
                    return
                }
            }
        }

        if (index != null) {
            patterns[index] = pattern
        } else {
            patterns.add(pattern)
        }
        popup.closePopup()
        parent.runUiCmdEvtUpdate { cmd, evt ->
            updatePatternList(cmd, evt)
        }
    }

    fun updatePatternList(cmd: UICommandBuilder, evt: UIEventBuilder) {
        val patterns = updatedData.patterns ?: rule?.patterns?.toList()
        evt.onActivating(
            "$PopupSelector #AddBtn",
            "Action" to ActionConfirm,
            "RuleIndex" to "1",
            "RulePattern" to patterns?.joinToString("√¿").orEmpty(),
            "@RuleIsRegex" to "$PopupSelector #IsRegexCb #CheckBox.Value",
            "@RuleReplacement" to "$PopupSelector #Replacement.Value",
            "@RuleBlockMessage" to "$PopupSelector #BlockMessage.Value",
        )

        cmd.clear("$PopupSelector #PatternList")
        if (patterns.isNullOrEmpty()) {
            return
        }

        for ((i, pattern) in patterns.withIndex()) {
            cmd.append("$PopupSelector #PatternList", "HeroChat/SubPage/Automod/PatternBadge.ui")
            cmd["$PopupSelector #PatternList[$i] #Txt.Text"] = pattern
            evt.onActivating(
                "$PopupSelector #PatternList[$i] #EditBtn",
                "Action" to ActionEditPattern,
                "RuleIndex" to i.toString()
            )
            evt.onActivating(
                "$PopupSelector #PatternList[$i] #DeleteBtn",
                "Action" to ActionDeletePattern,
                "RuleIndex" to i.toString()
            )
        }
    }

    data class UpdatedData(
        var patterns: MutableList<String>? = null
    ) {
        fun hasChanges(): Boolean = patterns != null

        fun clear() {
            patterns = null
        }
    }

    companion object {
        const val PopupSelector = "#RulePopup"
        const val ActionConfirm = "ActionConfirmRulePopup"
        const val ActionCancel = "ActionCancelRulePopup"

        const val ActionNewPattern = "PopupRuleNewPattern"
        const val ActionEditPattern = "PopupRuleEditPattern"
        const val ActionDeletePattern = "PopupRuleDeletePattern"
    }

    interface EventData {
        val ruleIndex: Int?
    }
}