package com.github.heroslender.herochat.ui.pages.settings

import com.github.heroslender.herochat.service.ChannelService
import com.github.heroslender.herochat.HeroChat
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
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo
import com.hypixel.hytale.server.core.ui.LocalizableString
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.NotificationUtil
import kotlin.collections.iterator

class SettingsSubPage(
    parent: ChatSettingsPage,
    private val channelService: ChannelService,
) : SubPage<ChatSettingsPage.UiState>(parent, "HeroChat/SubPage/SettingsSubPage.ui") {

    private val updatedData: UpdatedData = UpdatedData()

    override fun build(
        ref: Ref<EntityStore?>,
        cmd: UICommandBuilder,
        evt: UIEventBuilder,
        store: Store<EntityStore?>
    ) {
        val channels = channelService.channels.values.map {
            DropdownEntryInfo(LocalizableString.fromString(it.name), it.id)
        }

        cmd["#Dropdown.Entries"] = channels
        cmd["#Dropdown.Value"] = channelService.defaultChannel?.id ?: ""
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
            "newComponent", "editComponent" -> {
                val id = data.componentId
                val component = if (id != null) {
                    updatedData.components?.get(id) ?: HeroChat.instance.config.components[id]
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
                        components = HashMap(HeroChat.instance.config.components)
                    }
                    components.remove(id)

                    runUiCmdEvtUpdate { cmd, evt ->
                        populateComponents(cmd, evt)
                    }
                }
                return
            }

            "save" -> {
                var hasUpdatedData = false
                if (updatedData.defaultChannel != null) {
                    channelService.updateDefaultChannel(updatedData.defaultChannel!!)
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

        if (data.defaultChannel != null) {
            updatedData.defaultChannel = data.defaultChannel
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
            components = HashMap(HeroChat.instance.config.components)
            updatedData.components = components
        }
        components[id] = ComponentConfig(text, perm?.ifEmpty { null })

        runUiCmdEvtUpdate { cmd, evt ->
            populateComponents(cmd, evt)
        }
    }

    fun populateComponents(cmd: UICommandBuilder, evt: UIEventBuilder) {
        cmd.clear("#ListContainer")
        val components = updatedData.components ?: HeroChat.instance.config.components

        var i = 0
        for (component in components) {
            cmd.append("#ListContainer", ChannelSubPage.Companion.LAYOUT_COMPONENT_LIST_ITEM)
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