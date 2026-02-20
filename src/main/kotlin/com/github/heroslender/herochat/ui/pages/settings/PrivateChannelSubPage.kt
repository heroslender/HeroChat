package com.github.heroslender.herochat.ui.pages.settings

import com.github.heroslender.herochat.message.ComponentParser
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.channel.PrivateChannel
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.data.User
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

class PrivateChannelSubPage(
    parent: ChatSettingsPage,
    val user: User,
    val channel: PrivateChannel,
) : SubPage<ChatSettingsPage.UiState>(parent, "HeroChat/SubPage/PrivateChannelSubPage.ui") {

    companion object {
        const val LAYOUT_COMPONENT_LIST_ITEM: String = "HeroChat/ChatComponentListItem.ui"
    }

    private var senderFormat: String = channel.format
    private var receiverFormat: String = channel.receiverFormat

    private val updatedData: UpdatedData = UpdatedData()

    override fun build(
        ref: Ref<EntityStore?>,
        cmd: UICommandBuilder,
        evt: UIEventBuilder,
        store: Store<EntityStore?>
    ) {
        appendFormattedSenderPreview(cmd)
        appendFormattedReceiverPreview(cmd)

        populateComponents(cmd, evt)

        cmd["#PreviewField.Value"] = senderFormat
        cmd["#PreviewReceiverField.Value"] = receiverFormat
        cmd["#Permission #Txt.Value"] = channel.permission ?: ""

        evt.onValueChanged("#PreviewField", "@Format" to "#PreviewField.Value")
        evt.onValueChanged("#PreviewReceiverField", "@ReceiverFormat" to "#PreviewReceiverField.Value")
        evt.onValueChanged("#Permission #Txt", "@Permission" to "#Permission #Txt.Value")

        evt.onActivating("#NewComponentBtn", "Action" to "newComponent")
        evt.onActivating("#Save", "Action" to "save")
        evt.onActivating("#CloseButton", "Action" to "closeUI")
        evt.onActivating("#Cancel", "Action" to "closeUI")
    }

    override fun handleDataEvent(
        ref: Ref<EntityStore?>,
        store: Store<EntityStore?>,
        data: ChatSettingsPage.UiState
    ) {
        when (data.action) {
            "newComponent", "editComponent" -> {
                val id = data.componentId
                val component = if (id != null) {
                    updatedData.components?.get(id) ?: channel.components[id]
                } else null

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
                        updatedData.components = components
                    }

                    components.remove(id)

                    runUiCmdEvtUpdate { cmd, evt ->
                        updateSenderPreview()
                        updateReceiverPreview()
                        populateComponents(cmd, evt)
                    }
                }
                return
            }

            "save" -> {
                val config = HeroChat.instance.privateChannelConfig ?: return
                var hasUpdatedData = false
                if (updatedData.senderFormat != null) {
                    config.senderFormat = updatedData.senderFormat!!
                    hasUpdatedData = true
                }
                if (updatedData.receiverFormat != null) {
                    config.receiverFormat = updatedData.receiverFormat!!
                    hasUpdatedData = true
                }
                if (updatedData.permission != null) {
                    config.permission = updatedData.permission!!
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
        } else if (data.format != null) {
            updatedData.senderFormat = data.format
            this.senderFormat = data.format!!

            updateSenderPreview()
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
            updateSenderPreview()
            updateReceiverPreview()
            populateComponents(cmd, evt)
        }
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

    fun updateSenderPreview() = runUiCmdUpdate { cmd ->
        appendFormattedSenderPreview(cmd)
    }

    fun appendFormattedSenderPreview(cmd: UICommandBuilder, format: String = senderFormat) {
        val components = updatedData.components ?: channel.components

        val msg = ComponentParser.parse(
            user,
            format,
            HeroChat.instance.config.components +
                    components +
                    ("message" to ComponentConfig("Hello!! This is a test chat message.")) +
                    ("target_username" to ComponentConfig("Someone"))
        )

        cmd["#PreviewLbl.TextSpans"] = msg
    }

    fun updateReceiverPreview() = runUiCmdUpdate { cmd ->
        appendFormattedReceiverPreview(cmd)
    }

    fun appendFormattedReceiverPreview(cmd: UICommandBuilder, format: String = receiverFormat) {
        val components = updatedData.components ?: channel.components

        val msg = ComponentParser.parse(
            user,
            format,
            HeroChat.instance.config.components +
                    components +
                    ("message" to ComponentConfig("Hello!! This is a test chat message.")) +
                    ("target_username" to ComponentConfig("Someone"))
        )

        cmd["#PreviewReceiverLbl.TextSpans"] = msg
    }

    data class UpdatedData(
        var senderFormat: String? = null,
        var receiverFormat: String? = null,
        var permission: String? = null,
        var components: MutableMap<String, ComponentConfig>? = null,
    ) {
        fun hasChanges(): Boolean =
            senderFormat != null || receiverFormat != null || permission != null || components != null

        fun clear() {
            senderFormat = null
            receiverFormat = null
            permission = null
            components = null
        }
    }
}