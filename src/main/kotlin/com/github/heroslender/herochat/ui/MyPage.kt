package com.github.heroslender.herochat.ui

import com.github.heroslender.herochat.ComponentParser
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.config.ComponentConfig
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.MaybeBool
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.NotificationUtil
import javax.annotation.Nonnull
import kotlin.math.max

class MyPage(playerRef: PlayerRef) : InteractiveCustomUIPage<MyPage.EventData>(
    playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, EventData.CODEC
) {
    override fun build(
        @Nonnull ref: Ref<EntityStore?>,
        @Nonnull cmd: UICommandBuilder,
        @Nonnull evt: UIEventBuilder,
        @Nonnull store: Store<EntityStore?>
    ) {
        // Load the layout
        cmd.append(LAYOUT)

        cmd["#PreviewField.Value"] = HeroChat.instance.config.chatFormat
        cmd.appendFormattedMessage(HeroChat.instance.config.chatFormat)

        evt.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#PreviewField",
            com.hypixel.hytale.server.core.ui.builder.EventData().append("@Format", "#PreviewField.Value"),
            false
        )

        evt.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#ColorPicker",
            com.hypixel.hytale.server.core.ui.builder.EventData().append("@Color", "#ColorPicker.Value"),
            false
        )

        evt.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#MyButton",
            com.hypixel.hytale.server.core.ui.builder.EventData().append("Action", "click"),
            false
        )
    }

    override fun handleDataEvent(
        @Nonnull ref: Ref<EntityStore?>,
        @Nonnull store: Store<EntityStore?>,
        @Nonnull data: EventData
    ) {
        if ("click" == data.action) {
            NotificationUtil.sendNotification(
                playerRef.packetHandler,
                Message.raw("Button Clicked!"),
                Message.raw("You clicked the button."),
                NotificationStyle.Success
            )
        }

        if (data.color != null) {
            val cmd = UICommandBuilder()
            cmd["#ColorField.Value"] = data.color!!.substring(0, max(0, data.color!!.length - 2))
            sendUpdate(cmd, false)
        }

        if (data.format != null) {
            val cmd = UICommandBuilder()
            cmd.clear("#PreviewContainer");
            cmd.appendFormattedMessage(data.format!!)

            sendUpdate(cmd, false)
        }
    }

    fun UICommandBuilder.appendFormattedMessage(format: String) {
        val cmd = this
        val msg = ComponentParser.parse(
            playerRef,
            format,
            HeroChat.instance.config.components + ("message" to ComponentConfig("Hello!! This is a test chat message."))
        )
        var i = -1

        fun append(msg: Message, prevColor: String? = null, prevBold: Boolean? = null) {
            val color: String? = msg.color ?: prevColor
            val bold: Boolean? =
                if (msg.formattedMessage.bold == MaybeBool.Null) prevBold else msg.formattedMessage.bold == MaybeBool.True
            val txt = msg.rawText
            if (txt != null) {
                i++
                cmd.append("#PreviewContainer", PREVIEW_LABEL_LAYOUT)
                cmd["#PreviewContainer[$i] #PreviewLbl.Text"] = txt
                if (color != null) {
                    if (!color.isValidColor()) {
                        NotificationUtil.sendNotification(
                            playerRef.packetHandler,
                            Message.raw("Invalid Color!"),
                            Message.raw("The color \"$color\" is not a valid color."),
                            NotificationStyle.Danger
                        )
                    } else {
                        cmd["#PreviewContainer[$i] #PreviewLbl.Style.TextColor"] = color
                    }
                }
                if (bold != null)
                    cmd["#PreviewContainer[$i] #PreviewLbl.Style.RenderBold"] = bold
            }

            for (child in msg.children) {
                append(child, color, bold)
            }
        }

        append(msg)
    }

    // Event data class with codec
    class EventData {
        var action: String? = null
        var format: String? = null
        var color: String? = null

        companion object {
            val CODEC: BuilderCodec<EventData?> = BuilderCodec.builder<EventData?>(
                EventData::class.java, { EventData() }
            )
                .append<String?>(
                    KeyedCodec<String?>("Action", Codec.STRING),
                    { e: EventData?, v: String? -> e!!.action = v },
                    { e: EventData? -> e!!.action }).add()
                .append<String?>(
                    KeyedCodec<String?>("@Format", Codec.STRING),
                    { e: EventData?, v: String? -> e!!.format = v },
                    { e: EventData? -> e!!.format }).add()
                .append<String?>(
                    KeyedCodec<String?>("@Color", Codec.STRING),
                    { e: EventData?, v: String? -> e!!.color = v },
                    { e: EventData? -> e!!.color }).add()
                .build()
        }
    }

    companion object {
        // Path relative to Common/UI/Custom/
        const val LAYOUT: String = "HeroChat/ChatPage.ui"
        const val PREVIEW_LABEL_LAYOUT: String = "HeroChat/PreviewLabel.ui"
    }

    fun String.isValidColor(): Boolean {
        if (this.indexOf('#') < 0) {
            return false
        }

        if (length != 9 && length != 7 && length != 4) {
            return false
        }

        for (i in 1 until length) {
            if (this[i] !in '0'..'9' && this[i] !in 'a'..'f' && this[i] !in 'A'..'F') {
                return false
            }
        }

        return true
    }
}