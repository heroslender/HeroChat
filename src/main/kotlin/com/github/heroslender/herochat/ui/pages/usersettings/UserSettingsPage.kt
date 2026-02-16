package com.github.heroslender.herochat.ui.pages.usersettings

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.Permissions
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.data.PlayerUser
import com.github.heroslender.herochat.service.ChannelService
import com.github.heroslender.herochat.ui.Page
import com.github.heroslender.herochat.ui.event.ActionEventData
import com.github.heroslender.herochat.utils.*
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo
import com.hypixel.hytale.server.core.ui.LocalizableString
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.NotificationUtil
import javax.annotation.Nonnull

class UserSettingsPage(
    val user: PlayerUser,
    val channelService: ChannelService,
) : Page<UserSettingsPage.UiState>(user.player, UiState.CODEC) {
    private val settings = user.settings

    override fun build(ref: Ref<EntityStore?>, cmd: UICommandBuilder, evt: UIEventBuilder, store: Store<EntityStore?>) {
        cmd.append(LAYOUT)

        cmd["#FocusedChannel #Name.Text"] = messageStrFromConfig(MessagesConfig::menuFocusedChannel)
        cmd["#MutedChannels #Name.Text"] = messageStrFromConfig(MessagesConfig::menuMutedChannels)
        cmd["#MessageColor #Name.Text"] = messageStrFromConfig(MessagesConfig::menuMessageColor)
        cmd["#SpyMode #Name.Text"] = messageStrFromConfig(MessagesConfig::menuSpyMode)
        cmd["#Save.Text"] = messageStrFromConfig(MessagesConfig::menuSaveButton)
        cmd["#Cancel.Text"] = messageStrFromConfig(MessagesConfig::menuCancelButton)

        val channels = channelService.channels.values
            .filter { it.permission?.let { perm -> playerRef.hasPermission(perm) } ?: true }
            .map { DropdownEntryInfo(LocalizableString.fromString(it.name), it.id) }

        cmd["#FocusedChannel #Dropdown.Entries"] = channels
        cmd["#FocusedChannel #Dropdown.Value"] =
            settings.focusedChannelId ?: channelService.defaultChannel?.id ?: ""

        if (playerRef.hasPermission(Permissions.SETTINGS_MUTE_CHANNEL)) {
            cmd["#MutedChannels.Visible"] = true

            populateMutedChannels(settings.disabledChannels.toTypedArray(), cmd, evt)

            cmd["#MutedChannels #Dropdown.Entries"] = channels
            cmd["#MutedChannels #Dropdown.SelectedValues"] =
                settings.disabledChannels.map(LocalizableString::fromString).toTypedArray()
        }

        if (playerRef.hasPermission(Permissions.SETTINGS_MESSAGE_COLOR)) {
            cmd["#MessageColor.Visible"] = true
            if (settings.messageColor != null && settings.messageColor!!.isValidColor()) {
                cmd["#MessageColor #ColorPicker.Color"] = settings.messageColor!!
            }
        }

        if (playerRef.hasPermission(Permissions.ADMIN_SPY)) {
            cmd["#SpyMode.Visible"] = true
            cmd["#SpyMode #CheckBox.Value"] = settings.spyMode
        }

        evt.onValueChanged("#MutedChannels #Dropdown", "@MutedChannels" to "#MutedChannels #Dropdown.SelectedValues")

        evt.onActivating("#MessageColor #ResetBtn", "Action" to "reset-color")
        evt.onActivating("#Cancel", "Action" to "cancel")
        evt.onActivating(
            "#Save",
            "Action" to "save",
            "@FocusedChannel" to "#FocusedChannel #Dropdown.Value",
            "@MutedChannels" to "#MutedChannels #Dropdown.SelectedValues",
            "@Color" to "#MessageColor #ColorPicker.Color",
            "@SpyMode" to "#SpyMode #CheckBox.Value"
        )
    }

    override fun handleDataEvent(
        @Nonnull ref: Ref<EntityStore?>,
        @Nonnull store: Store<EntityStore?>,
        @Nonnull data: UiState
    ) {
        super.handleDataEvent(ref, store, data)

        when {
            data.action == "save" -> {
                with(HeroChat.instance.userService) {
                    user.updateSettings { settings ->
                        val focusedChannel = data.focusedChannel
                        settings.focusedChannelId =
                            if (focusedChannel == channelService.defaultChannel?.id) null else focusedChannel

                        if (playerRef.hasPermission(Permissions.SETTINGS_MUTE_CHANNEL)) {
                            settings.disabledChannels.clear()
                            settings.disabledChannels.addAll(data.mutedChannels ?: emptyArray())
                        }

                        if (playerRef.hasPermission(Permissions.SETTINGS_MESSAGE_COLOR)) {
                            val color = data.color?.substring(0, 7)
                            settings.messageColor = if (color == "#ffffff") null else color
                        }

                        if (playerRef.hasPermission(Permissions.ADMIN_SPY)) {
                            settings.spyMode = data.spyMode ?: false
                        }
                    }
                }

                NotificationUtil.sendNotification(
                    playerRef.packetHandler,
                    messageFromConfig(MessagesConfig::menuSuccessNotificationTitle, user),
                    messageFromConfig(MessagesConfig::menuSuccessNotificationDescription, user),
                    NotificationStyle.Success
                )

                closePage()
                return
            }
            data.action == "cancel" -> {
                closePage()
                return
            }
            data.action == "reset-color" -> {
                runUiCmdUpdate { cmd ->
                    cmd["#MessageColor #ColorPicker.Color"] = "#ffffffFF"
                }
            }
            data.action == "remove-muted-channel" -> {
                val ch = data.channel ?: return
                val muted = data.mutedChannels ?: return

                runUiCmdEvtUpdate { cmd, evt ->
                    val newList = muted.filter { it != ch }.toTypedArray()
                    cmd["#MutedChannels #Dropdown.SelectedValues"] = newList.map(LocalizableString::fromString).toTypedArray()

                    cmd.clear("#MutedChannelsList")
                    populateMutedChannels(newList, cmd, evt)
                }
            }
            data.mutedChannels != null -> {
                runUiCmdEvtUpdate { cmd, evt ->
                    cmd.clear("#MutedChannelsList")
                    populateMutedChannels(data.mutedChannels!!, cmd, evt)
                }
            }
        }
    }

    fun populateMutedChannels(mutedChannels: Array<String>, cmd: UICommandBuilder, evt: UIEventBuilder) {
        mutedChannels.forEachIndexed { i, ch ->
            val name = channelService.channels[ch]?.name ?: return@forEachIndexed
            cmd.append("#MutedChannelsList", "HeroChat/MutedChatBadge.ui")
            cmd["#MutedChannelsList[$i] #Txt.Text"] = name
            evt.onActivating(
                "#MutedChannelsList[$i] #DeleteBtn",
                "Action" to "remove-muted-channel",
                "Channel" to ch,
                "@MutedChannels" to "#MutedChannels #Dropdown.SelectedValues",
            )
        }
    }

    class UiState : ActionEventData {
        override var action: String? = null

        var channel: String? = null

        // Settings page props
        var mutedChannels: Array<String>? = null
        var focusedChannel: String? = null
        var color: String? = null
        var spyMode: Boolean? = null

        companion object {
            val CODEC: BuilderCodec<UiState> = BuilderCodec.builder(
                UiState::class.java, { UiState() })
                .append(
                    KeyedCodec("Action", Codec.STRING),
                    { e, v -> e.action = v },
                    { e -> e.action }).add()
                .appendStringOpt(UiState::channel)
//                .append(
//                    KeyedCodec("@Channel", Codec.STRING),
//                    { e, v -> e.channel = v },
//                    { e -> e.channel }).add()
                // Settings page props
                .append(
                    KeyedCodec("@MutedChannels", Codec.STRING_ARRAY),
                    { e, v -> e.mutedChannels = v },
                    { e -> e.mutedChannels }).add()
                .append(
                    KeyedCodec("@FocusedChannel", Codec.STRING),
                    { e, v -> e.focusedChannel = v },
                    { e -> e.focusedChannel }).add()
                .append(
                    KeyedCodec("@Color", Codec.STRING),
                    { e, v -> e.color = v },
                    { e -> e.color }).add()
                .append(
                    KeyedCodec("@SpyMode", Codec.BOOLEAN),
                    { e, v -> e.spyMode = v },
                    { e -> e.spyMode }).add()
                .build()
        }
    }

    companion object {
        const val LAYOUT: String = "HeroChat/UserSettingsPage.ui"
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