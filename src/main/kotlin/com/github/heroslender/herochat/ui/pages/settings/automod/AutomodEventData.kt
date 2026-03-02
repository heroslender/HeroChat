package com.github.heroslender.herochat.ui.pages.settings.automod

import com.github.heroslender.herochat.ui.event.ActionEventData
import com.github.heroslender.herochat.ui.popup.RulePatternPopup
import com.github.heroslender.herochat.ui.popup.RulePopup
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec

interface AutomodEventData : ActionEventData, RulePatternPopup.EventData, RulePopup.EventData {

    var automodEnabled: Boolean?
    var automodDefaultBlockMessage: String?

    override var ruleIndex: Int?
    var rulePattern: Array<String>?
    var ruleIsRegex: Boolean?
    var ruleReplacement: String?
    var ruleBlockMessage: String?
    override var rulePopupPattern: String?

    object ActionType {
        const val Save = "save"
        const val Close = "close"

        const val NewRule = "newRule"
        const val EditRule = "editRule"
        const val DeleteRule = "deleteRule"
    }

    companion object {
        const val Action = ActionEventData.Action

        const val FieldEnabled = "@AutomodEnabled"
        const val FieldDefaultBlockMessage = "@AutomodDefaultBlockMessage"
        const val FieldRuleIsRegex = "@RuleIsRegex"
        const val FieldRuleReplacement = "@RuleReplacement"
        const val FieldRuleBlockMessage = "@RuleBlockMessage"
        const val FieldRulePopupPattern = "@RulePopupPattern"

        const val RuleIndex = "RuleIndex"
        const val RulePattern = "RulePattern"

        fun <T : AutomodEventData> BuilderCodec.Builder<T>.appendAutomodEventData(): BuilderCodec.Builder<T> {
            return this
                .append(
                    KeyedCodec(FieldEnabled, Codec.BOOLEAN),
                    { e, v -> e.automodEnabled = v },
                    { e -> e.automodEnabled }).add()
                .append(
                    KeyedCodec(FieldDefaultBlockMessage, Codec.STRING),
                    { e, v -> e.automodDefaultBlockMessage = v },
                    { e -> e.automodDefaultBlockMessage }).add()
                .append(
                    KeyedCodec(RuleIndex, Codec.STRING),
                    { e, v -> e.ruleIndex = v?.toInt() },
                    { e -> e.ruleIndex?.toString() }).add()
                .append(
                    KeyedCodec(RulePattern, Codec.STRING),
                    { e, v -> e.rulePattern = v.split("√¿").toTypedArray() },
                    { e -> e.rulePattern?.joinToString("√¿") }).add()
                .append(
                    KeyedCodec(FieldRuleIsRegex, Codec.BOOLEAN),
                    { e, v -> e.ruleIsRegex = v },
                    { e -> e.ruleIsRegex }).add()
                .append(
                    KeyedCodec(FieldRuleReplacement, Codec.STRING),
                    { e, v -> e.ruleReplacement = v },
                    { e -> e.ruleReplacement }).add()
                .append(
                    KeyedCodec(FieldRuleBlockMessage, Codec.STRING),
                    { e, v -> e.ruleBlockMessage = v },
                    { e -> e.ruleBlockMessage }).add()
                .append(
                    KeyedCodec(FieldRulePopupPattern, Codec.STRING),
                    { e, v -> e.rulePopupPattern = v },
                    { e -> e.rulePopupPattern }).add()
        }
    }
}