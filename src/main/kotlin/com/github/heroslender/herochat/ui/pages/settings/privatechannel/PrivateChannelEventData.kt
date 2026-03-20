package com.github.heroslender.herochat.ui.pages.settings.privatechannel

import com.github.heroslender.herochat.ui.event.ActionEventData
import com.github.heroslender.herochat.ui.pages.settings.ComponentManagerEventData
import com.github.heroslender.herochat.ui.pages.settings.channel.ChannelEventData
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec

interface PrivateChannelEventData : ChannelEventData {
    var receiverFormat: String?

    object ActionType {
        const val Save = "save"
        const val Close = "close"

        const val NewCommand = "newChannelCommand"
        const val EditCommand = "editChannelCommand"
        const val DeleteCommand = "deleteChannelCommand"

        const val NewComponent = ComponentManagerEventData.ActionType.NewComponent
        const val EditComponent = ComponentManagerEventData.ActionType.EditComponent
        const val DeleteComponent = ComponentManagerEventData.ActionType.DeleteComponent
    }

    companion object {
        const val Action = ActionEventData.Action

        const val CommandId = "CommandId"
        const val ComponentId = ComponentManagerEventData.ComponentId

        const val FieldReceiverFormat = "@ReceiverFormat"
        const val FieldFormat = ChannelEventData.FieldFormat
        const val FieldPermission = ChannelEventData.FieldPermission
        const val FieldCapslockFilterEnabled = ChannelEventData.FieldCapslockFilterEnabled
        const val FieldCapslockFilterPercentage = ChannelEventData.FieldCapslockFilterPercentage
        const val FieldCapslockFilterMinLength = ChannelEventData.FieldCapslockFilterMinLength

        fun <T : PrivateChannelEventData> BuilderCodec.Builder<T>.appendPrivateChannelEventData(): BuilderCodec.Builder<T> {
            return this
                .append(
                    KeyedCodec(FieldReceiverFormat, Codec.STRING),
                    { e, v -> e.receiverFormat = v },
                    { e -> e.receiverFormat }).add()
        }
    }
}