package com.github.heroslender.herochat.ui.pages.settings.channel

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.channel.StandardChannel
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.message.MessageParser
import com.github.heroslender.herochat.ui.SubPage
import com.github.heroslender.herochat.ui.pages.settings.ChatSettingsPage
import com.github.heroslender.herochat.ui.pages.settings.UiState
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
import kotlin.collections.indexOf

class ChannelSubPage(
    parent: ChatSettingsPage,
    val user: User,
    val channel: StandardChannel,
) : SubPage<UiState>(parent, "HeroChat/SubPage/ChannelSubPage.ui") {

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

        populateCommands(cmd, evt)
        populateShoutCommands(cmd, evt)

        cmd["#PreviewField.Value"] = format
        cmd["#Permission #Txt.Value"] = channel.permission ?: ""
        cmd["#CrossWorld #CheckBox.Value"] = channel.crossWorld
        cmd["#Distance #Slider.Value"] = channel.distance?.toInt() ?: 1

        cmd["#CapslockEnabled #CheckBox.Value"] = channel.capslockFilter.enabled
        cmd["#CapslockPercentage #Slider.Value"] = channel.capslockFilter.percentage
        cmd["#CapslockMinLength #Slider.Value"] = channel.capslockFilter.minLength

        if (channel.capslockFilter.enabled) {
            cmd["#CapslockSettings.Visible"] = true
        }

        evt.onValueChanged("#PreviewField", ChannelEventData.FieldFormat to "#PreviewField.Value")
        evt.onValueChanged("#Permission #Txt", ChannelEventData.FieldPermission to "#Permission #Txt.Value")
        evt.onValueChanged("#CrossWorld #CheckBox", ChannelEventData.FieldCrossWorld to "#CrossWorld #CheckBox.Value")
        evt.onValueChanged("#Distance #Slider", ChannelEventData.FieldDistance to "#Distance #Slider.Value")
        evt.onValueChanged(
            "#CapslockEnabled #CheckBox",
            ChannelEventData.FieldCapslockFilterEnabled to "#CapslockEnabled #CheckBox.Value"
        )
        evt.onValueChanged(
            "#CapslockPercentage #Slider",
            ChannelEventData.FieldCapslockFilterPercentage to "#CapslockPercentage #Slider.Value"
        )
        evt.onValueChanged(
            "#CapslockMinLength #Slider",
            ChannelEventData.FieldCapslockFilterMinLength to "#CapslockMinLength #Slider.Value"
        )

        evt.onActivating("#Commands #NewCommandBtn", ChannelEventData.Action to ChannelEventData.ActionType.NewCommand)
        evt.onActivating("#ShoutCommands #NewCommandBtn", ChannelEventData.Action to ChannelEventData.ActionType.NewShoutCommand)
        evt.onActivating("#NewComponentBtn", ChannelEventData.Action to ChannelEventData.ActionType.NewComponent)
        evt.onActivating("#CloseButton", ChannelEventData.Action to ChannelEventData.ActionType.Close)
        evt.onActivating("#Cancel", ChannelEventData.Action to ChannelEventData.ActionType.Close)
        evt.onActivating("#Save", ChannelEventData.Action to ChannelEventData.ActionType.Save)
    }

    override fun handleDataEvent(ref: Ref<EntityStore?>, store: Store<EntityStore?>, data: UiState) {
        when (data.action) {
            ChannelEventData.ActionType.NewCommand,
            ChannelEventData.ActionType.EditCommand -> onUpdateCommand(data.commandId, ref, store)

            ChannelEventData.ActionType.DeleteCommand -> onDeleteCommand(data.commandId ?: return)

            ChannelEventData.ActionType.NewShoutCommand,
            ChannelEventData.ActionType.EditShoutCommand -> onUpdateShoutCommand(data.commandId, ref, store)
            ChannelEventData.ActionType.DeleteShoutCommand -> onDeleteShoutCommand(data.commandId ?: return)

            ChannelEventData.ActionType.NewComponent,
            ChannelEventData.ActionType.EditComponent -> onUpdateComponent(data.componentId, ref, store)

            ChannelEventData.ActionType.DeleteComponent -> onDeleteComponent(data.componentId)

            ChannelEventData.ActionType.Save -> onSave()
            ChannelEventData.ActionType.Close -> onClose(ref, store)

            else -> {
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
                } else if (data.capslockFilterEnabled != null) {
                    updatedData.capslockFilterEnabled = data.capslockFilterEnabled!!

                    runUiCmdUpdate { cmd ->
                        cmd["#CapslockSettings.Visible"] = data.capslockFilterEnabled!!
                    }
                } else if (data.capslockFilterPercentage != null) {
                    updatedData.capslockFilterPercentage = data.capslockFilterPercentage!!
                } else if (data.capslockFilterMinLength != null) {
                    updatedData.capslockFilterMinLength = data.capslockFilterMinLength!!
                }
            }
        }
    }

    fun onUpdateCommand(command: String?, ref: Ref<EntityStore?>, store: Store<EntityStore?>) {
        UpdateCommandPopup(parent, command) { cmd, _ ->
            val cmds = updatedData.commands ?: channel.commands.toMutableList()
            val i = cmds.indexOf(command)
            if (i != -1) {
                cmds[i] = cmd!!
            } else {
                cmds.add(cmd!!)
            }
            updatedData.commands = cmds

            runUiCmdEvtUpdate { cmd, evt ->
                populateCommands(cmd, evt)
            }
            closePopup()
        }.openPopup(ref, store)
    }

    fun onDeleteCommand(cmd: String) {
        val cmds = updatedData.commands ?: channel.commands.toMutableList()
        if (cmds.remove(cmd)) {
            updatedData.commands = cmds

            runUiCmdEvtUpdate { cmd, evt ->
                populateCommands(cmd, evt)
            }
        }
    }

    fun onUpdateShoutCommand(command: String?, ref: Ref<EntityStore?>, store: Store<EntityStore?>) {
        UpdateCommandPopup(parent, command) { cmd, _ ->
            val cmds = updatedData.shoutCommands ?: channel.shoutCommands?.toMutableList() ?: mutableListOf()
            val i = cmds.indexOf(command)
            if (i != -1) {
                cmds[i] = cmd!!
            } else {
                cmds.add(cmd!!)
            }
            updatedData.shoutCommands = cmds

            runUiCmdEvtUpdate { cmd, evt ->
                populateShoutCommands(cmd, evt)
            }
            closePopup()
        }.openPopup(ref, store)
    }

    fun onDeleteShoutCommand(cmd: String) {
        val cmds = updatedData.shoutCommands ?: channel.shoutCommands?.toMutableList()
        if (cmds == null) {
            return
        }

        if (cmds.remove(cmd)) {
            updatedData.shoutCommands = cmds

            runUiCmdEvtUpdate { cmd, evt ->
                populateShoutCommands(cmd, evt)
            }
        }
    }

    fun onUpdateComponent(id: String?, ref: Ref<EntityStore?>, store: Store<EntityStore?>) {
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
    }

    fun onDeleteComponent(id: String?) {
        if (id == null) {
            return
        }

        var components = updatedData.components
        if (components == null) {
            components = HashMap(channel.components)
            updatedData.components = components
        }

        components.remove(id)

        runUiCmdEvtUpdate { cmd, evt ->
            updatePreview()
            populateComponents(cmd, evt)
        }
    }

    fun onSave() {
        if (!updatedData.hasChanges()) {
            NotificationUtil.sendNotification(
                playerRef.packetHandler, Message.raw("Nothing to update"), NotificationStyle.Warning
            )
            return
        }
        val config = HeroChat.instance.channelConfigs[channel.id] ?: return

        if (updatedData.commands != null) {
            config.commands = updatedData.commands!!.toTypedArray()
        }
        if (updatedData.shoutCommands != null) {
            config.shoutCommands = updatedData.shoutCommands!!.toTypedArray()
        }
        if (updatedData.format != null) {
            config.format = updatedData.format!!
        }
        if (updatedData.permission != null) {
            config.permission = updatedData.permission!!
        }
        if (updatedData.crossWorld != null) {
            config.crossWorld = updatedData.crossWorld
        }
        if (updatedData.distance != null) {
            config.distance = updatedData.distance
        }
        if (updatedData.capslockFilterEnabled != null) {
            config.capslockFilter.enabled = updatedData.capslockFilterEnabled!!
        }
        if (updatedData.capslockFilterPercentage != null) {
            config.capslockFilter.percentage = updatedData.capslockFilterPercentage!!
        }
        if (updatedData.capslockFilterMinLength != null) {
            config.capslockFilter.minLength = updatedData.capslockFilterMinLength!!
        }
        if (updatedData.components != null) {
            config.components = updatedData.components!!
        }

        HeroChat.instance.saveChannelConfig(channel.id)
        updatedData.clear()
        NotificationUtil.sendNotification(
            playerRef.packetHandler, Message.raw("Config saved!"), NotificationStyle.Success
        )
    }

    fun onClose(ref: Ref<EntityStore?>, store: Store<EntityStore?>) {
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

    fun onSaveChatComponent(data: UiState) {
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

    fun populateCommands(cmd: UICommandBuilder, evt: UIEventBuilder) {
        cmd.clear("#Commands #List")

        val cmds = updatedData.commands ?: channel.commands.toMutableList()
        for ((i, command) in cmds.withIndex()) {
            cmd.append("#Commands #List", "HeroChat/SubPage/CommandListItem.ui")

            cmd["#Commands #List[$i] #Txt.Text"] = command
            evt.onActivating(
                "#Commands #List[$i] #EditBtn",
                ChannelEventData.Action to ChannelEventData.ActionType.EditCommand,
                ChannelEventData.CommandId to command
            )
            evt.onActivating(
                "#Commands #List[$i] #DeleteBtn",
                ChannelEventData.Action to ChannelEventData.ActionType.DeleteCommand,
                ChannelEventData.CommandId to command
            )
        }
    }

    fun populateShoutCommands(cmd: UICommandBuilder, evt: UIEventBuilder) {
        cmd.clear("#ShoutCommands #List")

        val cmds = updatedData.shoutCommands ?: channel.shoutCommands?.toMutableList()
        if (cmds == null) {
            return
        }

        for ((i, command) in cmds.withIndex()) {
            cmd.append("#ShoutCommands #List", "HeroChat/SubPage/CommandListItem.ui")

            cmd["#ShoutCommands #List[$i] #Txt.Text"] = command
            evt.onActivating(
                "#ShoutCommands #List[$i] #EditBtn",
                ChannelEventData.Action to ChannelEventData.ActionType.EditShoutCommand,
                ChannelEventData.CommandId to command
            )
            evt.onActivating(
                "#ShoutCommands #List[$i] #DeleteBtn",
                ChannelEventData.Action to ChannelEventData.ActionType.DeleteShoutCommand,
                ChannelEventData.CommandId to command
            )
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
                ChannelEventData.ComponentId to component.key,
                ChannelEventData.Action to ChannelEventData.ActionType.EditComponent,
            )

            evt.onActivating(
                "#ListContainer[$i] #DeleteBtn",
                ChannelEventData.ComponentId to component.key,
                ChannelEventData.Action to ChannelEventData.ActionType.DeleteComponent,
            )

            i++
        }
    }

    fun updatePreview() = runUiCmdUpdate { cmd ->
        appendFormattedPreview(cmd)
    }

    fun appendFormattedPreview(cmd: UICommandBuilder, format: String = this.format) {
        val components = updatedData.components ?: channel.components

        val msg = MessageParser.parse(
            user,
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

        var capslockFilterEnabled: Boolean? = null,
        var capslockFilterPercentage: Int? = null,
        var capslockFilterMinLength: Int? = null,

        var commands: MutableList<String>? = null,
        var shoutCommands: MutableList<String>? = null,

        var components: MutableMap<String, ComponentConfig>? = null,
    ) {
        fun hasChanges(): Boolean =
            format != null
                    || permission != null
                    || distance != null
                    || crossWorld != null
                    || capslockFilterEnabled != null
                    || capslockFilterPercentage != null
                    || capslockFilterMinLength != null
                    || commands != null
                    || shoutCommands != null
                    || components != null

        fun clear() {
            format = null
            permission = null
            distance = null
            crossWorld = null
            capslockFilterEnabled = null
            capslockFilterPercentage = null
            capslockFilterMinLength = null
            commands = null
            shoutCommands = null
            components = null
        }
    }
}