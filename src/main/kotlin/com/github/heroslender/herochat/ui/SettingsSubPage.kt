package com.github.heroslender.herochat.ui

import com.github.heroslender.herochat.ChannelManager
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.ui.ChannelSubPage.Companion.LAYOUT_COMPONENT_LIST_ITEM
import com.github.heroslender.herochat.ui.popup.ConfirmationPopup
import com.github.heroslender.herochat.utils.onActivating
import com.github.heroslender.herochat.utils.onValueChanged
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo
import com.hypixel.hytale.server.core.ui.LocalizableString
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.NotificationUtil

class SettingsSubPage(
    val parent: ChatSettingsPage,
    private val channelManager: ChannelManager,
    override val playerRef: PlayerRef
) : SubPage<ChatSettingsPage.UiState> {
    override val layoutPath: String = "HeroChat/SubPage/SettingsSubPage.ui"

    private val updatedData: UpdatedData = UpdatedData()

    override fun build(
        ref: Ref<EntityStore?>,
        cmd: UICommandBuilder,
        evt: UIEventBuilder,
        store: Store<EntityStore?>
    ) {
        val channels = channelManager.channels.values.map {
            DropdownEntryInfo(LocalizableString.fromString(it.name), it.id)
        }

        cmd["#Dropdown.Entries"] = channels
        cmd["#Dropdown.Value"] = channelManager.defaultChannel?.id ?: ""
        evt.onValueChanged("#Dropdown", "@DefaultChannel" to "#Dropdown.Value")

        populateComponents(cmd, evt)

        evt.onActivating("#NewComponentBtn", "Action" to "newComponent")
        evt.onActivating("#CloseButton", "Action" to "closeUI")
        evt.onActivating("#Cancel", "Action" to "closeUI")
        evt.onActivating("#Save", "Action" to "save")
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
                    val component = updatedData.components?.get(id) ?: HeroChat.instance.config.components[id] ?: return
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

                var components = updatedData.components
                if (components == null) {
                    components = HashMap(HeroChat.instance.config.components)
                    updatedData.components = components
                }
                components[id] = ComponentConfig(text, perm?.ifEmpty { null })

                parent.runUiCmdEvtUpdate { cmd, evt ->
                    populateComponents(cmd, evt)
                    cmd.remove("#Popup");
                }
                return
            }

            "deleteComponent" -> {
                val id = data.componentId
                if (id != null) {
                    var components = updatedData.components
                    if (components == null) {
                        components = HashMap(HeroChat.instance.config.components)
                    }
                    components.remove(id)

                    parent.runUiCmdEvtUpdate { cmd, evt ->
                        populateComponents(cmd, evt)
                    }
                }
                return
            }

            "save" -> {
                var hasUpdatedData = false
                if (updatedData.defaultChannel != null) {
                    channelManager.updateDefaultChannel(updatedData.defaultChannel!!)
                    hasUpdatedData = true
                }
                if (updatedData.components != null) {
                    HeroChat.instance.config.components.clear()
                    HeroChat.instance.config.components.putAll(updatedData.components!!)
                    HeroChat.instance.saveConfig()
                    hasUpdatedData = true
                }

                if (hasUpdatedData) {
                    NotificationUtil.sendNotification(
                        playerRef.packetHandler, Message.raw("Config saved!"), NotificationStyle.Success
                    )
                } else {
                    NotificationUtil.sendNotification(
                        playerRef.packetHandler, Message.raw("Nothing to update"), NotificationStyle.Warning
                    )
                }

                updatedData.clear()
                return
            }

            "closeUI" -> {
                if (updatedData.hasChanges()) {
                    parent.runUiCmdEvtUpdate { cmd, evt ->
                        val confirmationPopup = ConfirmationPopup<ChatSettingsPage.UiState>(
                            title = "Unsaved Changes",
                            message = "Are you sure you want to leave without saving the changes made?",
                        )
                        cmd.append(confirmationPopup.layoutPath)
                        confirmationPopup.build(ref, cmd, evt, store)
                    }

                    return
                }

                parent.closePage()
                return
            }

            ConfirmationPopup.ActionConfirmPopup -> {
                parent.closePage()
                return
            }
            ConfirmationPopup.ActionCancelPopup -> {
                parent.runUiCmdUpdate { cmd ->
                    cmd.remove(ConfirmationPopup.PopupSelector);
                }
                return
            }

            "closePopup" -> {
                parent.runUiCmdUpdate { cmd ->
                    cmd.remove("#Popup");
                }
                return
            }
        }

        if (data.defaultChannel != null) {
            updatedData.defaultChannel = data.defaultChannel
        }
    }

    fun populateComponents(cmd: UICommandBuilder, evt: UIEventBuilder) {
        cmd.clear("#ListContainer")
        val components = updatedData.components ?: HeroChat.instance.config.components

        var i = 0
        for (component in components) {
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

    data class UpdatedData(
        var defaultChannel: String? = null,
        var components: MutableMap<String, ComponentConfig>? = null,
    ) {
        fun hasChanges(): Boolean = defaultChannel != null || components != null

        fun clear() {
            defaultChannel = null
            components = null
        }
    }
}