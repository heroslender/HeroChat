package com.github.heroslender.herochat.ui.pages.settings.privatechannel

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.channel.PrivateChannel
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.message.MessageParser
import com.github.heroslender.herochat.ui.SubPage
import com.github.heroslender.herochat.ui.pages.settings.ChatSettingsPage
import com.github.heroslender.herochat.ui.pages.settings.UiState
import com.github.heroslender.herochat.ui.pages.settings.channel.ChannelSubPage
import com.github.heroslender.herochat.ui.pages.settings.channel.UpdateCommandPopup
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

class PrivateChannelSubPage(
    parent: ChatSettingsPage,
    val user: User,
    val channel: PrivateChannel,
) : SubPage<UiState>(parent, "HeroChat/SubPage/PrivateChannelSubPage.ui") {

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

        populateCommands(cmd, evt)

        populateComponents(cmd, evt)

        cmd["#PreviewField.Value"] = senderFormat
        cmd["#PreviewReceiverField.Value"] = receiverFormat
        cmd["#Permission #Txt.Value"] = channel.permission ?: ""

        cmd["#CapslockSettings.Visible"] = channel.capslockFilter.enabled
        cmd["#CapslockEnabled #CheckBox.Value"] = channel.capslockFilter.enabled
        cmd["#CapslockPercentage #Slider.Value"] = channel.capslockFilter.percentage
        cmd["#CapslockMinLength #Slider.Value"] = channel.capslockFilter.minLength

        evt.onValueChanged("#PreviewField", PrivateChannelEventData.FieldFormat to "#PreviewField.Value")
        evt.onValueChanged(
            "#PreviewReceiverField",
            PrivateChannelEventData.FieldReceiverFormat to "#PreviewReceiverField.Value"
        )
        evt.onValueChanged("#Permission #Txt", PrivateChannelEventData.FieldPermission to "#Permission #Txt.Value")
        evt.onValueChanged(
            "#CapslockEnabled #CheckBox",
            PrivateChannelEventData.FieldCapslockFilterEnabled to "#CapslockEnabled #CheckBox.Value"
        )
        evt.onValueChanged(
            "#CapslockPercentage #Slider",
            PrivateChannelEventData.FieldCapslockFilterPercentage to "#CapslockPercentage #Slider.Value"
        )
        evt.onValueChanged(
            "#CapslockMinLength #Slider",
            PrivateChannelEventData.FieldCapslockFilterMinLength to "#CapslockMinLength #Slider.Value"
        )

        evt.onActivating(
            "#NewComponentBtn",
            PrivateChannelEventData.Action to PrivateChannelEventData.ActionType.NewComponent
        )
        evt.onActivating(
            "#Commands #NewCommandBtn",
            PrivateChannelEventData.Action to PrivateChannelEventData.ActionType.NewCommand
        )
        evt.onActivating("#Save", PrivateChannelEventData.Action to PrivateChannelEventData.ActionType.Save)
        evt.onActivating("#CloseButton", PrivateChannelEventData.Action to PrivateChannelEventData.ActionType.Close)
        evt.onActivating("#Cancel", PrivateChannelEventData.Action to PrivateChannelEventData.ActionType.Close)
    }

    override fun handleDataEvent(
        ref: Ref<EntityStore?>,
        store: Store<EntityStore?>,
        data: UiState
    ) {
        when (data.action) {
            PrivateChannelEventData.ActionType.NewCommand,
            PrivateChannelEventData.ActionType.EditCommand -> onUpdateCommand(data.commandId, ref, store)
            PrivateChannelEventData.ActionType.DeleteCommand -> onDeleteCommand(data.commandId ?: return)

            PrivateChannelEventData.ActionType.NewComponent,
            PrivateChannelEventData.ActionType.EditComponent -> onUpdateComponent(data.componentId, ref, store)
            PrivateChannelEventData.ActionType.DeleteComponent -> onDeleteComponent(data.componentId ?: return)

            PrivateChannelEventData.ActionType.Save -> onSave()
            PrivateChannelEventData.ActionType.Close -> onClose(ref, store)

            else -> {
                if (data.permission != null) {
                    updatedData.permission = data.permission
                } else if (data.format != null) {
                    updatedData.senderFormat = data.format
                    this.senderFormat = data.format!!

                    updateSenderPreview()
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

    fun onUpdateComponent(id: String?, ref: Ref<EntityStore?>, store: Store<EntityStore?>) {
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
    }

    fun onDeleteComponent(id: String) {
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

    fun onSave() {
        if (!updatedData.hasChanges()) {
            NotificationUtil.sendNotification(
                playerRef.packetHandler, Message.raw("Nothing to update"), NotificationStyle.Warning
            )
            return
        }

        val config = HeroChat.instance.privateChannelConfig

        if (updatedData.commands != null) {
            config.commands = updatedData.commands!!.toTypedArray()
        }
        if (updatedData.senderFormat != null) {
            config.senderFormat = updatedData.senderFormat!!
        }
        if (updatedData.receiverFormat != null) {
            config.receiverFormat = updatedData.receiverFormat!!
        }
        if (updatedData.permission != null) {
            config.permission = updatedData.permission!!
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
            updateSenderPreview()
            updateReceiverPreview()
            populateComponents(cmd, evt)
        }
    }

    fun populateCommands(cmd: UICommandBuilder, evt: UIEventBuilder) {
        cmd.clear("#Commands #List")

        val cmds = updatedData.commands ?: channel.commands.toMutableList()
        for ((i, command) in cmds.withIndex()) {
            cmd.append("#Commands #List", "HeroChat/SubPage/CommandListItem.ui")

            cmd["#Commands #List[$i] #Txt.Text"] = command
            evt.onActivating(
                "#Commands #List[$i] #EditBtn",
                PrivateChannelEventData.Action to PrivateChannelEventData.ActionType.EditCommand,
                PrivateChannelEventData.CommandId to command
            )
            evt.onActivating(
                "#Commands #List[$i] #DeleteBtn",
                PrivateChannelEventData.Action to PrivateChannelEventData.ActionType.DeleteCommand,
                PrivateChannelEventData.CommandId to command
            )
        }
    }

    fun populateComponents(cmd: UICommandBuilder, evt: UIEventBuilder) {
        cmd.clear("#ListContainer")

        val components = updatedData.components ?: channel.components

        var i = 0
        for (component in components) {
            cmd.append("#ListContainer", ChannelSubPage.LAYOUT_COMPONENT_LIST_ITEM)
            cmd["#ListContainer[$i] #Tag.Text"] = "{${component.key}}"
            cmd["#ListContainer[$i] #TagText.Text"] = component.value.text
            if (component.value.permission != null) {
                cmd["#ListContainer[$i] #PermGroup.Visible"] = true
                cmd["#ListContainer[$i] #Permission.Text"] = "{${component.value.permission}}"
            }

            evt.onActivating(
                "#ListContainer[$i] #EditBtn",
                PrivateChannelEventData.ComponentId to component.key,
                PrivateChannelEventData.Action to PrivateChannelEventData.ActionType.EditComponent,
            )

            evt.onActivating(
                "#ListContainer[$i] #DeleteBtn",
                PrivateChannelEventData.ComponentId to component.key,
                PrivateChannelEventData.Action to PrivateChannelEventData.ActionType.DeleteComponent,
            )

            i++
        }
    }

    fun updateSenderPreview() = runUiCmdUpdate { cmd ->
        appendFormattedSenderPreview(cmd)
    }

    fun appendFormattedSenderPreview(cmd: UICommandBuilder, format: String = senderFormat) {
        val components = updatedData.components ?: channel.components

        val msg = MessageParser.parse(
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

        val msg = MessageParser.parse(
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

        var capslockFilterEnabled: Boolean? = null,
        var capslockFilterPercentage: Int? = null,
        var capslockFilterMinLength: Int? = null,

        var commands: MutableList<String>? = null,

        var components: MutableMap<String, ComponentConfig>? = null,
    ) {
        fun hasChanges(): Boolean =
            senderFormat != null
                    || receiverFormat != null
                    || permission != null
                    || capslockFilterEnabled != null
                    || capslockFilterPercentage != null
                    || capslockFilterMinLength != null
                    || commands != null
                    || components != null

        fun clear() {
            senderFormat = null
            receiverFormat = null
            permission = null
            capslockFilterEnabled = null
            capslockFilterPercentage = null
            capslockFilterMinLength = null
            commands = null
            components = null
        }
    }
}