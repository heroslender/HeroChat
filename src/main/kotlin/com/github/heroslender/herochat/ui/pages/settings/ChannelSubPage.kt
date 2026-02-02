package com.github.heroslender.herochat.ui.pages.settings

import com.github.heroslender.herochat.ComponentParser
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.chat.Channel
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.ui.SubPage
import com.github.heroslender.herochat.ui.popup.ComponentPopup
import com.github.heroslender.herochat.ui.popup.ConfirmationPopup
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
import kotlin.collections.iterator

class ChannelSubPage(
    parent: ChatSettingsPage,
    val channel: Channel,
) : SubPage<ChatSettingsPage.UiState>(parent, "HeroChat/SubPage/ChannelSubPage.ui") {

    companion object {
        const val LAYOUT_COMPONENT_LIST_ITEM: String = "HeroChat/ChatComponentListItem.ui"
    }

    private var format: String = channel.format

    private val updatedData: UpdatedData = UpdatedData()

    override fun build(
        ref: Ref<EntityStore?>,
        cmd: UICommandBuilder,
        evt: UIEventBuilder,
        store: Store<EntityStore?>
    ) {
        appendFormattedPreview(cmd)

        populateComponents(cmd, evt)

        cmd["#PreviewField.Value"] = format
        cmd["#Permission #Txt.Value"] = channel.permission ?: ""
        cmd["#CrossWorld #CheckBox.Value"] = channel.crossWorld
        cmd["#Distance #Slider.Value"] = channel.distance?.toInt() ?: 1

        evt.onValueChanged("#PreviewField", "@Format" to "#PreviewField.Value")
        evt.onValueChanged("#Permission #Txt", "@Permission" to "#Permission #Txt.Value")
        evt.onValueChanged("#CrossWorld #CheckBox", "@CrossWorld" to "#CrossWorld #CheckBox.Value")
        evt.onValueChanged("#Distance #Slider", "@Distance" to "#Distance #Slider.Value")

        evt.onActivating("#NewComponentBtn", "Action" to "newComponent")
        evt.onActivating("#CloseButton", "Action" to "closeUI")
        evt.onActivating("#Cancel", "Action" to "closeUI")
        evt.onActivating("#Save", "Action" to "save")
    }

    override fun handleDataEvent(ref: Ref<EntityStore?>, store: Store<EntityStore?>, data: ChatSettingsPage.UiState) {
        when (data.action) {
            "newComponent", "editComponent" -> {
                val id = data.componentId
                val component = id?.let { id -> updatedData.components?.get(id) ?: channel.components[id] }

                ComponentPopup(parent, id, component) { data ->
                    when (data.action) {
                        ComponentPopup.ActionCancelPopup -> closePopup()
                        ComponentPopup.ActionConfirmPopup -> {
                            onSaveChatComponent(data)
                            closePopup()
                        }
                    }
                }.openPopup(ref, store)

                return
            }

            "deleteComponent" -> {
                val id = data.componentId
                if (id != null) {
                    var components = updatedData.components
                    if (components == null) {
                        components = HashMap(channel.components)
                    }

                    components.remove(id)

                    runUiCmdEvtUpdate { cmd, evt ->
                        updatePreview()
                        populateComponents(cmd, evt)
                    }
                }
                return
            }

            "save" -> {
                val config = HeroChat.instance.channelConfigs[channel.id] ?: return
                var hasUpdatedData = false
                if (updatedData.format != null) {
                    config.format = updatedData.format!!
                    hasUpdatedData = true
                }
                if (updatedData.permission != null) {
                    config.permission = updatedData.permission!!
                    hasUpdatedData = true
                }
                if (updatedData.crossWorld != null) {
                    config.crossWorld = updatedData.crossWorld
                    hasUpdatedData = true
                }
                if (updatedData.distance != null) {
                    config.distance = updatedData.distance
                    hasUpdatedData = true
                }
                if (updatedData.components != null) {
                    config.components = updatedData.components!!
                    hasUpdatedData = true
                }

                if (hasUpdatedData) {
                    HeroChat.instance.saveChannelConfig(channel.id)
                    updatedData.clear()
                    NotificationUtil.sendNotification(
                        playerRef.packetHandler, Message.raw("Config saved!"), NotificationStyle.Success
                    )
                } else {
                    NotificationUtil.sendNotification(
                        playerRef.packetHandler, Message.raw("Nothing to update"), NotificationStyle.Warning
                    )
                }
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

        if (data.permission != null) {
            updatedData.permission = data.permission
        } else if (data.crossWorld != null) {
            updatedData.crossWorld = data.crossWorld
        } else if (data.distance != null) {
            updatedData.distance = data.distance
        } else if (data.format != null) {
            updatedData.format = data.format
            this.format = data.format!!

            updatePreview()
        }
    }

    fun onSaveChatComponent(data: ChatSettingsPage.UiState) {
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

        var components = updatedData.components
        if (components == null) {
            components = HashMap(channel.components)
            updatedData.components = components
        }
        components[id] = ComponentConfig(text, perm?.ifEmpty { null })

        runUiCmdEvtUpdate { cmd, evt ->
            updatePreview()
            populateComponents(cmd, evt)
        }
        return
    }


    fun populateComponents(cmd: UICommandBuilder, evt: UIEventBuilder) {
        cmd.clear("#ListContainer")

        val components = updatedData.components ?: channel.components

        var i = 0
        for (component in components) {
            cmd.append("#ListContainer", LAYOUT_COMPONENT_LIST_ITEM)
            cmd["#ListContainer[$i] #Tag.Text"] = "{${component.key}}"
            cmd["#ListContainer[$i] #TagText.Text"] = component.value.text
            if (component.value.permission != null) {
                cmd["#ListContainer[$i] #PermGroup.Visible"] = true
                cmd["#ListContainer[$i] #Permission.Text"] = "{${component.value.permission}}"
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

    fun updatePreview() = runUiCmdUpdate { cmd ->
        appendFormattedPreview(cmd)
    }

    fun appendFormattedPreview(cmd: UICommandBuilder, format: String = this.format) {
        val components = updatedData.components ?: channel.components

        val msg = ComponentParser.parse(
            playerRef.uuid,
            format,
            HeroChat.instance.config.components + components + ("message" to ComponentConfig("Hello!! This is a test chat message."))
        )

        cmd["#PreviewLbl.TextSpans"] = msg
    }

    data class UpdatedData(
        var format: String? = null,
        var permission: String? = null,
        var distance: Double? = null,
        var crossWorld: Boolean? = null,
        var components: MutableMap<String, ComponentConfig>? = null,
    ) {
        fun hasChanges(): Boolean =
            format != null || permission != null || distance != null || crossWorld != null || components != null

        fun clear() {
            format = null
            permission = null
            distance = null
            crossWorld = null
            components = null
        }
    }
}