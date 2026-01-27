package com.github.heroslender.herochat.ui

import com.github.heroslender.herochat.ComponentParser
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.chat.Channel
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.utils.onActivating
import com.github.heroslender.herochat.utils.onValueChanged
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.NotificationUtil

class ChannelSubPage(
    val parent: ChatSettingsPage,
    val channel: Channel,
    override val playerRef: PlayerRef,
) : SubPage<ChatSettingsPage.UiState> {

    override val layoutPath: String = "HeroChat/ChannelSubPage.ui"

    companion object {
        const val LAYOUT_COMPONENT_LIST_ITEM: String = "HeroChat/ChatComponentListItem.ui"
    }

    private var format: String = channel.format

    override fun build(
        ref: Ref<EntityStore?>,
        cmd: UICommandBuilder,
        evt: UIEventBuilder,
        store: Store<EntityStore?>
    ) {
        cmd["#PreviewField.Value"] = format
        appendFormattedPreview(cmd)

        populateComponents(cmd, evt)

        cmd["#Permission #Txt.Value"] = channel.permission ?: ""
        cmd["#CrossWorld #CheckBox.Value"] = channel.crossWorld
        cmd["#Distance #Slider.Value"] = channel.distance?.toInt() ?: 1

        evt.onActivating("#NewComponentBtn", "Action" to "newComponent")
        evt.onActivating("#Save", "Action" to "save")
        evt.onActivating("#CloseButton", "Action" to "closeUI")
        evt.onActivating("#Cancel", "Action" to "closeUI")
        evt.onValueChanged("#PreviewField", "@Format" to "#PreviewField.Value")
    }

    override fun handleDataEvent(
        ref: Ref<EntityStore?>,
        store: Store<EntityStore?>,
        data: ChatSettingsPage.UiState
    ) {
        when (data.action) {
            "newComponent" -> {
                parent.runUiCmdEvtUpdate { cmd, evt ->
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

            "editComponent" -> {
                val id = data.componentId
                if (id != null) {
                    val component = HeroChat.instance.config.components[id] ?: return
                    parent.runUiCmdEvtUpdate { cmd, evt ->
                        cmd.append("HeroChat/AddChatComponent.ui")
                        cmd["#Popup #PopupTitle.Text"] = "Edit Component"
                        cmd["#Popup #AddBtn.Text"] = "Save"
                        cmd["#Popup #Tag.Value"] = id
                        cmd["#Popup #Permission.Value"] = component.permission ?: ""
                        cmd["#Popup #Format.Value"] = component.text

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

                return
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

                parent.runUiCmdEvtUpdate { cmd, evt ->
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
                    parent.runUiCmdEvtUpdate { cmd, evt ->
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
                parent.closePage()
                return
            }

            "closeUI" -> {
                parent.closePage()
                return
            }

            "closePopup" -> {
                parent.runUiCmdUpdate { cmd ->
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
            cmd.append("#ListContainer", LAYOUT_COMPONENT_LIST_ITEM)
            cmd["#ListContainer[$i] #Tag.Text"] = "{${component.key}}"
            cmd["#ListContainer[$i] #TagText.Text"] = component.value.text
            if (component.value.permission != null) {
                cmd["#ListContainer[$i] #PermGroup.Visible"] = true
                cmd["#ListContainer[$i] #Permission.Text"] = "{${component.key}}"
            }

            evt.onActivating(
                "#ListContainer[$i] #EditBtn",
                "CId" to component.key,
                "Action" to "editComponent",
            )

            evt.onActivating(
                "#ListContainer[$i] #DeleteBtn",
                "CId" to component.key,
                "Action" to "deleteComponent",
            )

            i++
        }
    }

    fun updatePreview() = parent.runUiCmdUpdate { cmd ->
        appendFormattedPreview(cmd)
    }

    fun appendFormattedPreview(cmd: UICommandBuilder, format: String = this.format) {
        val msg = ComponentParser.parse(
            playerRef.uuid,
            format,
            HeroChat.instance.config.components + ("message" to ComponentConfig("Hello!! This is a test chat message."))
        )

        cmd["#PreviewLbl.TextSpans"] = msg
    }
}