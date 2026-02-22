package com.github.heroslender.herochat.ui.pages.settings

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.config.AutoModConfig
import com.github.heroslender.herochat.config.AutoModRule
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.ui.SubPage
import com.github.heroslender.herochat.ui.popup.ConfirmationPopup
import com.github.heroslender.herochat.ui.popup.RulePopup
import com.github.heroslender.herochat.utils.onActivating
import com.github.heroslender.herochat.utils.onValueChanged
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.NotificationUtil

class AutomodSubPage(
    parent: ChatSettingsPage,
    val user: User,
    val config: AutoModConfig,
) : SubPage<ChatSettingsPage.UiState>(parent, "HeroChat/SubPage/Automod/AutomodSubPage.ui") {

    companion object {
        const val LAYOUT_RULE_LIST_ITEM: String = "HeroChat/SubPage/Automod/RuleListItem.ui"
    }

    private val updatedData: UpdatedData = UpdatedData()

    override fun build(
        ref: Ref<EntityStore?>,
        cmd: UICommandBuilder,
        evt: UIEventBuilder,
        store: Store<EntityStore?>
    ) {
        populateRules(cmd, evt)

        cmd["#Enabled #CheckBox.Value"] = config.enabled
        cmd["#DefaultBlockMessageField.Value"] = config.defaultBlockMessage
        cmd["#AutomodSettings.Visible"] = config.enabled

        evt.onValueChanged("#Enabled #CheckBox", "@AutomodEnabled" to "#Enabled #CheckBox.Value")
        evt.onValueChanged(
            "#DefaultBlockMessageField",
            "@AutomodDefaultBlockMessage" to "#DefaultBlockMessageField.Value"
        )

        evt.onActivating("#NewRuleBtn", "Action" to "newRule")
        evt.onActivating("#CloseButton", "Action" to "closeUI")
        evt.onActivating("#Cancel", "Action" to "closeUI")
        evt.onActivating("#Save", "Action" to "save")
    }

    override fun handleDataEvent(ref: Ref<EntityStore?>, store: Store<EntityStore?>, data: ChatSettingsPage.UiState) {
        when (data.action) {
            "newRule", "editRule" -> {
                val i = data.ruleIndex
                val rule = i?.let { updatedData.rules?.get(it) ?: config.rules[it] }

                RulePopup(parent, rule) { data ->
                    when (data.action) {
                        RulePopup.ActionCancel -> closePopup()
                        RulePopup.ActionConfirm -> {
                            data.ruleIndex = i
                            onSaveRule(data)
                            closePopup()
                        }
                    }
                }.openPopup(ref, store)

                return
            }

            "deleteRule" -> {
                val id = data.ruleIndex
                if (id != null) {
                    var rules = updatedData.rules
                    if (rules == null) {
                        rules = config.rules.toMutableList()
                        updatedData.rules = rules
                    }

                    rules.removeAt(id)

                    runUiCmdEvtUpdate { cmd, evt ->
                        populateRules(cmd, evt)
                    }
                }
                return
            }

            "save" -> {
                if (!updatedData.hasChanges()) {
                    NotificationUtil.sendNotification(
                        playerRef.packetHandler, Message.raw("Nothing to update"), NotificationStyle.Warning
                    )
                    return
                }
                if (updatedData.enabled != null) {
                    config.enabled = updatedData.enabled!!
                }
                if (updatedData.defaultBlockMessage != null) {
                    config.defaultBlockMessage = updatedData.defaultBlockMessage!!
                }
                if (updatedData.rules != null) {
                    config.rules = updatedData.rules!!.toTypedArray()
                }

                HeroChat.instance.saveAutomodConfig()
                updatedData.clear()
                NotificationUtil.sendNotification(
                    playerRef.packetHandler, Message.raw("Config saved!"), NotificationStyle.Success
                )
                return
            }

            "closeUI" -> {
                if (updatedData.hasChanges()) {
                    ConfirmationPopup(
                        parent,
                        title = "Unsaved Changes",
                        message = "Are you sure you want to leave without saving the changes made?",
                        onConfirm = {
                            parent.closePage()
                        }
                    ).openPopup(ref, store)
                    return
                }

                parent.closePage()
                return
            }
        }

        if (data.automodEnabled != null) {
            updatedData.enabled = data.automodEnabled
            runUiCmdUpdate { cmd ->
                cmd["#AutomodSettings.Visible"] = data.automodEnabled!!
            }
        } else if (data.automodDefaultBlockMessage != null) {
            updatedData.defaultBlockMessage = data.automodDefaultBlockMessage
        }
    }

    fun onSaveRule(data: ChatSettingsPage.UiState): Boolean {
        val i = data.ruleIndex
        val patterns = data.rulePattern!!
        val isRegex = data.ruleIsRegex
        val replacement = data.ruleReplacement
        val blockMessage = data.ruleBlockMessage

        if (isRegex == null) {
            NotificationUtil.sendNotification(
                playerRef.packetHandler,
                Message.raw("Missing fields"),
                Message.raw("Rule IsRegexx is not defined."),
                NotificationStyle.Danger
            )
            return false
        }

        if (replacement == null && blockMessage == null) {
            NotificationUtil.sendNotification(
                playerRef.packetHandler,
                Message.raw("Missing fields"),
                Message.raw("Rule replacement and block message is not defined."),
                NotificationStyle.Danger
            )
            return false
        }

        var rules = updatedData.rules
        if (rules == null) {
            rules = config.rules.toMutableList()
            updatedData.rules = rules
        }

        val rule = AutoModRule(
            patterns = patterns,
            isRegex = isRegex,
            replacement = replacement,
            blockMessage = blockMessage
        )
        if (i != null) {
            rules[i] = rule
        } else {
            rules.add(rule)
        }

        runUiCmdEvtUpdate { cmd, evt ->
            populateRules(cmd, evt)
        }
        return true
    }


    fun populateRules(cmd: UICommandBuilder, evt: UIEventBuilder) {
        cmd.clear("#ListContainer")

        val rules = updatedData.rules?.toTypedArray() ?: config.rules

        for ((i, rule) in rules.withIndex()) {
            cmd.append("#ListContainer", LAYOUT_RULE_LIST_ITEM)
            cmd["#ListContainer[$i] #Tag.Text"] = if (rule.isRegex) "Regex Rule #${i}" else "Rule #${i}"

            cmd["#ListContainer[$i] #Action.Text"] = if (rule.replacement != null) {
                "Replace with: ${rule.replacement}"
            } else {
                "Block with message: ${rule.blockMessage ?: config.defaultBlockMessage}"
            }

            cmd["#ListContainer[$i] #Patterns.Text"] = "Patterns: \n" + rule.patterns.joinToString("\n  - ", "  - ")

            evt.onActivating(
                "#ListContainer[$i] #EditBtn",
                "RuleIndex" to i.toString(),
                "Action" to "editRule",
            )

            evt.onActivating(
                "#ListContainer[$i] #DeleteBtn",
                "RuleIndex" to i.toString(),
                "Action" to "deleteRule",
            )
        }
    }

    data class UpdatedData(
        var enabled: Boolean? = null,
        var defaultBlockMessage: String? = null,
        var rules: MutableList<AutoModRule>? = null
    ) {
        fun hasChanges(): Boolean = enabled != null || defaultBlockMessage != null || rules != null

        fun clear() {
            enabled = null
            defaultBlockMessage = null
            rules = null
        }
    }
}