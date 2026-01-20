package com.github.heroslender.herochat.ui

import com.github.heroslender.herochat.ComponentParser
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.utils.onActivating
import com.github.heroslender.herochat.utils.onValueChanged
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.MaybeBool
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.NotificationUtil
import java.util.concurrent.atomic.AtomicInteger
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
        appendFormattedPreview(cmd)

        populateComponents(cmd, evt)

        evt.onActivating("#NewComponentBtn", "Action" to "newComponent")
        evt.onActivating("#Save", "Action" to "save")
        evt.onActivating("#CloseButton", "Action" to "closeUI")
        evt.onActivating("#Cancel", "Action" to "closeUI")
        evt.onValueChanged("#PreviewField", "@Format" to "#PreviewField.Value")
    }

    override fun handleDataEvent(
        @Nonnull ref: Ref<EntityStore?>,
        @Nonnull store: Store<EntityStore?>,
        @Nonnull data: UiState
    ) {
        when (data.action) {
            "newComponent" -> {
                runUiCmdEvtUpdate { cmd, evt ->
                    cmd.append("HeroChat/AddChatComponent.ui")
                    evt.onActivating("#Popup #Close", "Action" to "closePopup")
                    evt.onActivating(
                        "#Popup #AddBtn",
                        "Action" to "saveComponent",
                        "@CId" to "#Popup #Tag.Value",
                        "@CText" to "#Popup #Format.Value",
                        "@CPerm" to "#Popup #Permission.Value",
                    )
                }
            }

            "saveComponent" -> {
                val id = data.componentId
                val text = data.componentText
                val perm = data.componentPermission

                if (id == null) {
                    NotificationUtil.sendNotification(
                        playerRef.packetHandler,
                        Message.raw("Missing fields"),
                        Message.raw("Component tag is not defined."),
                        NotificationStyle.Danger
                    )
                    return
                }

                if (text == null) {
                    NotificationUtil.sendNotification(
                        playerRef.packetHandler,
                        Message.raw("Missing fields"),
                        Message.raw("Component format is not defined."),
                        NotificationStyle.Danger
                    )
                    return
                }

                HeroChat.instance.config.components[id] = ComponentConfig(text, perm?.ifEmpty { null })

                runUiCmdEvtUpdate { cmd, evt ->
                    updatePreview()
                    populateComponents(cmd, evt)
                    cmd.remove("#Popup");
                }
                return
            }

            "deleteComponent" -> {
                val id = data.componentId
                if (id != null) {
                    HeroChat.instance.config.components.remove(id)
                    runUiCmdEvtUpdate { cmd, evt ->
                        updatePreview()
                        populateComponents(cmd, evt)
                    }
                }
                return
            }

            "save" -> {
                HeroChat.instance.saveConfig()
                NotificationUtil.sendNotification(
                    playerRef.packetHandler, Message.raw("Config saved!"), NotificationStyle.Success
                )
                return
            }

            "closeUI" -> {
                close()
                return
            }

            "closePopup" -> {
                runUiCmdUpdate { cmd ->
                    cmd.remove("#Popup");
                }
                return
            }
        }

        if (data.format != null) {
            this.format = data.format!!

            updatePreview()
        }

        if (data.componentId != null) {
            val id = data.componentId!!
            val text = data.componentText
            val perm = data.componentPermission

            text?.also { HeroChat.instance.config.components[id]?.text = text }
            perm?.also { HeroChat.instance.config.components[id]?.permission = perm.ifEmpty { null } }

            updatePreview()
        }
    }

    fun populateComponents(cmd: UICommandBuilder, evt: UIEventBuilder) {
        cmd.clear("#ListContainer")

        var i = 0
        for (component in HeroChat.instance.config.components) {
            cmd.append("#ListContainer", ComponentListItemLayout)
            cmd["#ListContainer[$i] #PanelTitle.Text"] = "Tag: {${component.key}}"
            cmd["#ListContainer[$i] #Id.Value"] = component.key
            cmd["#ListContainer[$i] #Text.Value"] = component.value.text
            cmd["#ListContainer[$i] #Permission.Value"] = component.value.permission ?: ""

            evt.onValueChanged(
                "#ListContainer[$i] #Text",
                "CId" to component.key,
                "@CText" to "#ListContainer[$i] #Text.Value",
            )

            evt.onValueChanged(
                "#ListContainer[$i] #Permission",
                "CId" to component.key,
                "@CPerm" to "#ListContainer[$i] #Permission.Value",
            )

            evt.onActivating(
                "#ListContainer[$i] #DeleteBtn",
                "CId" to component.key,
                "Action" to "deleteComponent",
            )

            i++
        }
    }

    fun updatePreview() = runUiCmdUpdate { cmd ->
        cmd.clear("#PreviewContainer");
        appendFormattedPreview(cmd)
    }

    fun appendFormattedPreview(cmd: UICommandBuilder, format: String = this@MyPage.format) {
        val msg = ComponentParser.parse(
            playerRef,
            format,
            HeroChat.instance.config.components + ("message" to ComponentConfig("Hello!! This is a test chat message."))
        )

        append(cmd, msg)
    }

    fun append(
        cmd: UICommandBuilder,
        msg: Message,
        prevColor: String? = null,
        prevBold: Boolean? = null,
        inc: AtomicInteger = AtomicInteger(0)
    ) {
        val color: String? = msg.color ?: prevColor
        val bold: Boolean? =
            if (msg.formattedMessage.bold == MaybeBool.Null) prevBold else msg.formattedMessage.bold == MaybeBool.True
        val txt = msg.rawText
        if (txt != null) {
            val i = inc.getAndIncrement()
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

            if (bold != null) {
                cmd["#PreviewContainer[$i] #PreviewLbl.Style.RenderBold"] = bold
            }
        }

        for (child in msg.children) {
            append(cmd, child, color, bold, inc)
        }
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
                UiState::class.java, { UiState() }).append<String?>(
                    KeyedCodec<String?>("Action", Codec.STRING),
                    { e: UiState?, v: String? -> e!!.action = v },
                    { e: UiState? -> e!!.action }).add().append<String?>(
                    KeyedCodec<String?>("@Format", Codec.STRING),
                    { e: UiState?, v: String? -> e!!.format = v },
                    { e: UiState? -> e!!.format }).add().append<String?>(
                    KeyedCodec<String?>("CId", Codec.STRING),
                    { e: UiState?, v: String? -> e!!.componentId = v },
                    { e: UiState? -> e!!.componentId }).add().append<String?>(
                    KeyedCodec<String?>("@CId", Codec.STRING),
                    { e: UiState?, v: String? -> e!!.componentId = v },
                    { e: UiState? -> e!!.componentId }).add()
                .append<String?>(
                    KeyedCodec<String?>("@CText", Codec.STRING),
                    { e: UiState?, v: String? -> e!!.componentText = v },
                    { e: UiState? -> e!!.componentText }).add().append<String?>(
                    KeyedCodec<String?>("@CPerm", Codec.STRING),
                    { e: UiState?, v: String? -> e!!.componentPermission = v },
                    { e: UiState? -> e!!.componentPermission }).add().build()
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

        if (length != 9 && length != 7) {
            return false
        }

        for (i in 1 until length) {
            if (this[i] !in '0'..'9' && this[i] !in 'a'..'f' && this[i] !in 'A'..'F') {
                return false
            }
        }

        return true
    }

    fun runUiCmdUpdate(
        clear: Boolean = false, func: (cmd: UICommandBuilder) -> Unit
    ) {
        val cmd = UICommandBuilder()
        func(cmd)
        sendUpdate(cmd, clear)
    }

    fun runUiCmdEvtUpdate(
        clear: Boolean = false, func: (cmd: UICommandBuilder, evt: UIEventBuilder) -> Unit
    ) {
        val cmd = UICommandBuilder()
        val evt = UIEventBuilder()
        func(cmd, evt)
        sendUpdate(cmd, evt, clear)
    }
}