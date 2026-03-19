package com.github.heroslender.herochat.ui.pages.settings.channel

import com.github.heroslender.herochat.ui.event.ActionEventData
import com.github.heroslender.herochat.ui.pages.settings.ComponentManagerEventData
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec

interface ChannelEventData : ActionEventData, ComponentManagerEventData, UpdateCommandPopup.EventData {
    var format: String?
    var permission: String?
    var crossWorld: Boolean?
    var distance: Double?

    var capslockFilterEnabled: Boolean?
    var capslockFilterPercentage: Int?
    var capslockFilterMinLength: Int?

    var commandId: String?

    object ActionType {
        const val Save = "save"
        const val Close = "close"

        const val NewCommand = "newChannelCommand"
        const val EditCommand = "editChannelCommand"
        const val DeleteCommand = "deleteChannelCommand"

        const val NewShoutCommand = "newChannelShoutCommand"
        const val EditShoutCommand = "editChannelShoutCommand"
        const val DeleteShoutCommand = "deleteChannelShoutCommand"

        const val NewComponent = ComponentManagerEventData.ActionType.NewComponent
        const val EditComponent = ComponentManagerEventData.ActionType.EditComponent
        const val DeleteComponent = ComponentManagerEventData.ActionType.DeleteComponent
    }

    companion object {
        const val Action = ActionEventData.Action

        const val CommandId = "CommandId"
        const val ComponentId = ComponentManagerEventData.ComponentId

        const val FieldFormat = "@Format"
        const val FieldPermission = "@Permission"
        const val FieldCrossWorld = "@CrossWorld"
        const val FieldDistance = "@Distance"
        const val FieldCapslockFilterEnabled = "@CapslockFilterEnabled"
        const val FieldCapslockFilterPercentage = "@CapslockFilterPercentage"
        const val FieldCapslockFilterMinLength = "@CapslockFilterMinLength"

        fun <T : ChannelEventData> BuilderCodec.Builder<T>.appendChannelEventData(): BuilderCodec.Builder<T> {
            return this
                .append(
                    KeyedCodec(FieldFormat, Codec.STRING),
                    { e, v -> e.format = v },
                    { e -> e.format }).add()
                .append(
                    KeyedCodec(FieldPermission, Codec.STRING),
                    { e, v -> e.permission = v },
                    { e -> e.permission }).add()
                .append(
                    KeyedCodec(FieldCrossWorld, Codec.BOOLEAN),
                    { e, v -> e.crossWorld = v },
                    { e -> e.crossWorld }).add()
                .append(
                    KeyedCodec(FieldDistance, Codec.DOUBLE),
                    { e, v -> e.distance = v },
                    { e -> e.distance }).add()
                .append(
                    KeyedCodec(FieldCapslockFilterEnabled, Codec.BOOLEAN),
                    { e, v -> e.capslockFilterEnabled = v },
                    { e -> e.capslockFilterEnabled }).add()
                .append(
                    KeyedCodec(FieldCapslockFilterPercentage, Codec.INTEGER),
                    { e, v -> e.capslockFilterPercentage = v },
                    { e -> e.capslockFilterPercentage }).add()
                .append(
                    KeyedCodec(FieldCapslockFilterMinLength, Codec.INTEGER),
                    { e, v -> e.capslockFilterMinLength = v },
                    { e -> e.capslockFilterMinLength }).add()
                .append(
                    KeyedCodec(CommandId, Codec.STRING),
                    { e, v -> e.commandId = v },
                    { e -> e.commandId }).add()
        }
    }
}