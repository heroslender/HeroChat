package com.github.heroslender.herochat.ui.pages.settings.general

import com.github.heroslender.herochat.ui.event.ActionEventData
import com.github.heroslender.herochat.ui.pages.settings.ComponentManagerEventData
import com.github.heroslender.herochat.ui.pages.settings.channel.ChannelEventData
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec

interface SettingsEventData : ChannelEventData {
    var defaultChannel: String?
    var nicknameLength: Int?

    object ActionType {
        const val Save = "save"
        const val Close = "close"

        const val NewComponent = ComponentManagerEventData.ActionType.NewComponent
        const val EditComponent = ComponentManagerEventData.ActionType.EditComponent
        const val DeleteComponent = ComponentManagerEventData.ActionType.DeleteComponent
    }

    companion object {
        const val Action = ActionEventData.Action

        const val ComponentId = ComponentManagerEventData.ComponentId

        const val FieldDefaultChannel = "@DefaultChannel"
        const val FieldNicknameLength = "@NicknameLength"

        fun <T : SettingsEventData> BuilderCodec.Builder<T>.appendSettingsEventData(): BuilderCodec.Builder<T> {
            return this
                .append(
                    KeyedCodec(FieldDefaultChannel, Codec.STRING),
                    { e, v -> e.defaultChannel = v },
                    { e -> e.defaultChannel }).add()
                .append(
                    KeyedCodec(FieldNicknameLength, Codec.INTEGER),
                    { e, v -> e.nicknameLength = v },
                    { e -> e.nicknameLength }).add()
        }
    }
}