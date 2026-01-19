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
import com.hypixel.hytale.server.core.ui.builder.EventData
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.NotificationUtil
import javax.annotation.Nonnull

class MyPage(playerRef: PlayerRef) : InteractiveCustomUIPage<MyPage.UiState>(
    playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, UiState.CODEC
) {
    private var format: String = HeroChat.instance.config.chatFormat
    override fun build(
        @Nonnull ref: Ref<EntityStore?>,
        @Nonnull cmd: UICommandBuilder,
        @Nonnull evt: UIEventBuilder,
        @Nonnull store: Store<EntityStore?>
    ) {
        // Load the layout
        cmd.append(LAYOUT)

        cmd["#PreviewField.Value"] = HeroChat.instance.config.chatFormat
        cmd.appendFormattedMessage()

        var i = 0
        for (component in HeroChat.instance.config.components) {
            cmd.append("#ListContainer", ComponentListItemLayout)
            cmd["#ListContainer[$i] #PanelTitle.Text"] = "Tag: {${component.key}}"
            cmd["#ListContainer[$i] #Id.Value"] = component.key
            cmd["#ListContainer[$i] #Text.Value"] = component.value.text
            cmd["#ListContainer[$i] #Permission.Value"] = component.value.permission ?: ""

            evt.addEventBinding(
                CustomUIEventBindingType.ValueChanged,
                "#ListContainer[$i] #Text",
                EventData.of("@CText", "#ListContainer[$i] #Text.Value")
                    .put("CId", component.key),
                false
            )

            evt.addEventBinding(
                CustomUIEventBindingType.ValueChanged,
                "#ListContainer[$i] #Permission",
                EventData.of("@CPerm", "#ListContainer[$i] #Permission.Value")
                    .put("CId", component.key),
                false
            )

            i++
        }

        evt.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CloseButton",
            EventData.of("Action", "closeUI"),
            false
        )

        evt.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#Cancel",
            EventData.of("Action", "closeUI"),
            false
        )

        evt.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#PreviewField",
            EventData.of("@Format", "#PreviewField.Value"),
            false
        )
    }

    override fun handleDataEvent(
        @Nonnull ref: Ref<EntityStore?>,
        @Nonnull store: Store<EntityStore?>,
        @Nonnull data: UiState
    ) {
        if ("closeUI" == data.action) {
            close()
            return
        }

        if (data.format != null) {
            this.format = data.format!!

            updatePreview()
        }

        if (data.componentId != null) {
            val id = data.componentId!!
            val text = data.componentText
            val perm = data.componentPermission

            text?.also { HeroChat.instance.config.components[id]?.text = text}
            perm?.also { HeroChat.instance.config.components[id]?.permission = perm.ifEmpty { null } }

            updatePreview()
        }
    }

    fun updatePreview() {
        val cmd = UICommandBuilder()
        cmd.clear("#PreviewContainer");
        cmd.appendFormattedMessage()
        sendUpdate(cmd, false)
    }

    fun UICommandBuilder.appendFormattedMessage(format: String = this@MyPage.format) {
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
    class UiState {
        var action: String? = null
        var format: String? = null

        var componentId: String? = null
        var componentText: String? = null
        var componentPermission: String? = null

        companion object {
            val CODEC: BuilderCodec<UiState?> = BuilderCodec.builder<UiState?>(
                UiState::class.java, { UiState() }
            )
                .append<String?>(
                    KeyedCodec<String?>("Action", Codec.STRING),
                    { e: UiState?, v: String? -> e!!.action = v },
                    { e: UiState? -> e!!.action }).add()
                .append<String?>(
                    KeyedCodec<String?>("@Format", Codec.STRING),
                    { e: UiState?, v: String? -> e!!.format = v },
                    { e: UiState? -> e!!.format }).add()
                .append<String?>(
                    KeyedCodec<String?>("CId", Codec.STRING),
                    { e: UiState?, v: String? -> e!!.componentId = v },
                    { e: UiState? -> e!!.componentId }).add()
                .append<String?>(
                    KeyedCodec<String?>("@CText", Codec.STRING),
                    { e: UiState?, v: String? -> e!!.componentText = v },
                    { e: UiState? -> e!!.componentText }).add()
                .append<String?>(
                    KeyedCodec<String?>("@CPerm", Codec.STRING),
                    { e: UiState?, v: String? -> e!!.componentPermission = v },
                    { e: UiState? -> e!!.componentPermission }).add()
                .build()
        }
    }

    companion object {
        // Path relative to Common/UI/Custom/
        const val LAYOUT: String = "HeroChat/ChatPage.ui"
        const val ComponentListItemLayout: String = "HeroChat/ChatComponentListItem.ui"
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