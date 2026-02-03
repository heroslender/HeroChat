package com.github.heroslender.herochat.ui.pages.usersettings

import com.github.heroslender.herochat.ChannelManager
import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.data.UserSettings
import com.github.heroslender.herochat.ui.Page
import com.github.heroslender.herochat.ui.event.ActionEventData
import com.github.heroslender.herochat.utils.hasPermission
import com.github.heroslender.herochat.utils.messageFromConfig
import com.github.heroslender.herochat.utils.onActivating
import com.github.heroslender.herochat.utils.onValueChanged
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
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.NotificationUtil
import javax.annotation.Nonnull

class UserSettingsPage(
    playerRef: PlayerRef,
    val settings: UserSettings,
    val channelManager: ChannelManager,
) : Page<UserSettingsPage.UiState>(playerRef, UiState.CODEC) {

    override fun build(ref: Ref<EntityStore?>, cmd: UICommandBuilder, evt: UIEventBuilder, store: Store<EntityStore?>) {
        cmd.append(LAYOUT)

        val channels = channelManager.channels.values
            .filter { if (it.permission != null) playerRef.hasPermission(it.permission) else true }
            .map { DropdownEntryInfo(LocalizableString.fromString(it.name), it.id) }
            .let {
                val p = channelManager.privateChannel
                if (p.permission == null || playerRef.hasPermission(p.permission)) {
                    listOf(DropdownEntryInfo(LocalizableString.fromString(p.name), p.id), *it.toTypedArray())
                } else it
            }

        cmd["#FocusedChannel #Dropdown.Entries"] = channels
        cmd["#FocusedChannel #Dropdown.Value"] =
            settings.focusedChannelId ?: channelManager.defaultChannel?.id ?: ""

        if (playerRef.hasPermission("herochat.chat.mute-channels")) {
            cmd["#MutedChannels.Visible"] = true

            val pCh = channelManager.privateChannel
            val chs = channelManager.channels
            settings.disabledChannels.forEachIndexed { i, ch ->
                val name = if (ch == pCh.id) {
                    pCh.name
                } else {
                    chs[ch]?.name ?: return@forEachIndexed
                }
                cmd.append("#MutedChannelsList", "HeroChat/MutedChatBadge.ui")
                cmd["#MutedChannelsList[$i] #Txt.Text"] = name
            }

            cmd["#MutedChannels #Dropdown.Entries"] = channels
            cmd["#MutedChannels #Dropdown.Value"] = channelManager.defaultChannel?.id ?: ""
            cmd["#MutedChannels #Dropdown.SelectedValues"] =
                settings.disabledChannels.map(LocalizableString::fromString).toTypedArray()
        }

        if (playerRef.hasPermission("herochat.chat.message-color")) {
            cmd["#MessageColor.Visible"] = true
            if (settings.messageColor != null && settings.messageColor!!.isValidColor()) {
                cmd["#MessageColor #ColorPicker.Color"] = settings.messageColor!!
            }
        }

        if (playerRef.hasPermission("herochat.admin.spy")) {
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

        if (data.action == "save") {
            val focusedChannel = data.focusedChannel
            HeroChat.instance.userService.updateSettings(playerRef.uuid) { settings ->
                settings.focusedChannelId =
                    if (focusedChannel == channelManager.defaultChannel?.id) null else focusedChannel

                if (playerRef.hasPermission("herochat.chat.mute-channels")) {
                    settings.disabledChannels.clear()
                    settings.disabledChannels.addAll(data.mutedChannels ?: emptyArray())
                }

                if (playerRef.hasPermission("herochat.chat.message-color")) {
                    val color = data.color?.substring(0, 7)
                    settings.messageColor = if (color == "#ffffff") null else color
                }

                if (playerRef.hasPermission("herochat.admin.spy")) {
                    settings.spyMode = data.spyMode ?: false
                }
            }

            NotificationUtil.sendNotification(
                playerRef.packetHandler,
                messageFromConfig(MessagesConfig::menuSuccessNotificationTitle, playerRef),
                messageFromConfig(MessagesConfig::menuSuccessNotificationDescription, playerRef),
                NotificationStyle.Success
            )

            closePage()
            return
        } else if (data.action == "cancel") {
            closePage()
            return
        } else if (data.action == "reset-color") {
            runUiCmdUpdate { cmd ->
                cmd["#MessageColor #ColorPicker.Color"] = "#ffffffFF"
            }
        }

        if (data.mutedChannels != null) {
//            println(data.mutedChannels?.joinToString(", "))

            runUiCmdEvtUpdate { cmd, evt ->
                cmd.clear("#MutedChannelsList")
                val pCh = channelManager.privateChannel
                val channels = channelManager.channels
                data.mutedChannels!!.forEachIndexed { i, ch ->
                    val name = if (ch == pCh.id) {
                        pCh.name
                    } else {
                        channels[ch]?.name ?: return@forEachIndexed
                    }
                    cmd.append("#MutedChannelsList", "HeroChat/MutedChatBadge.ui")
                    cmd["#MutedChannelsList[$i] #Txt.Text"] = name
                }
            }
        }
    }

    class UiState : ActionEventData {
        override var action: String? = null

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