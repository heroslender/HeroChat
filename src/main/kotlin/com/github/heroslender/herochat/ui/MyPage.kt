package com.github.heroslender.herochat.ui

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.utils.onActivating
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage
import com.hypixel.hytale.server.core.ui.Value
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import javax.annotation.Nonnull

class MyPage(playerRef: PlayerRef) : InteractiveCustomUIPage<MyPage.UiState>(
    playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, UiState.CODEC
) {
    private var subPage: SubPage = SettingsSubPage(this, playerRef)
    private var currentNavDest = "settings"

    override fun build(
        @Nonnull ref: Ref<EntityStore?>,
        @Nonnull cmd: UICommandBuilder,
        @Nonnull evt: UIEventBuilder,
        @Nonnull store: Store<EntityStore?>
    ) {
        // Load the layout
        cmd.append(LAYOUT)

        cmd["#ShowSettingsBtn.Style"] = NavBtnSelectedStyle
        evt.onActivating("#ShowSettingsBtn", "Action" to "showSettings")
        HeroChat.instance.channelManager.channels.values.forEachIndexed { i, channel ->
            cmd.append("#NavChannels", "HeroChat/Sidebar/SidebarButton.ui")
            cmd["#NavChannels[$i].Text"] = channel.name
            evt.onActivating(
                "#NavChannels[$i]",
                "Action" to "showChannel",
                "Channel" to channel.id,
                "NavIndex" to i.toString()
            )
        }

        cmd.append("#PageContent", subPage.layoutPath)
        subPage.build(ref, cmd, evt, store)
    }

    override fun handleDataEvent(
        @Nonnull ref: Ref<EntityStore?>,
        @Nonnull store: Store<EntityStore?>,
        @Nonnull data: UiState
    ) {
        when (data.action) {
            "showSettings" -> {
                if (currentNavDest == "settings") {
                    return
                }

                currentNavDest = "settings"
                runUiCmdEvtUpdate { cmd, evt ->
                    cmd.clear("#PageContent")

                    subPage = SettingsSubPage(this, playerRef)
                    cmd.append("#PageContent", subPage.layoutPath)
                    subPage.build(ref, cmd, evt, store)

                    cmd["#ShowSettingsBtn.Style"] = NavBtnSelectedStyle
                    for (i in 0 until HeroChat.instance.channelManager.channels.size) {
                        cmd["#NavChannels[$i].Style"] = NavBtnStyle
                    }
                }
            }

            "showChannel" -> {
                val channelId = data.channel ?: return
                val index = data.navIndex?.toInt() ?: return

                if (currentNavDest == "channel-$channelId") {
                    return
                }

                val channel = HeroChat.instance.channelManager.channels[channelId] ?: return

                currentNavDest = "channel-$channelId"
                runUiCmdEvtUpdate { cmd, evt ->
                    cmd.clear("#PageContent")

                    subPage = ChannelSubPage(this, channel, playerRef)
                    cmd.append("#PageContent", subPage.layoutPath)
                    subPage.build(ref, cmd, evt, store)

                    cmd["#ShowSettingsBtn.Style"] = NavBtnStyle
                    for (i in 0 until HeroChat.instance.channelManager.channels.size) {
                        cmd["#NavChannels[$i].Style"] = if (i == index) NavBtnSelectedStyle else NavBtnStyle
                    }
                }
            }
        }

        subPage.handleDataEvent(ref, store, data)
    }

    // Event data class with codec
    class UiState {
        var action: String? = null
        var format: String? = null
        var channel: String? = null

        var navIndex: String? = null

        var componentId: String? = null
        var componentText: String? = null
        var componentPermission: String? = null

        companion object {
            val CODEC: BuilderCodec<UiState?> = BuilderCodec.builder(
                UiState::class.java, { UiState() })
                .append(
                    KeyedCodec("Action", Codec.STRING),
                    { e: UiState, v: String? -> e.action = v },
                    { e: UiState -> e.action }).add()
                .append(
                    KeyedCodec("@Format", Codec.STRING),
                    { e: UiState, v: String? -> e.format = v },
                    { e: UiState -> e.format }).add()
                .append(
                    KeyedCodec("Channel", Codec.STRING),
                    { e: UiState, v: String? -> e.channel = v },
                    { e: UiState -> e.channel }).add()
                .append(
                    KeyedCodec("NavIndex", Codec.STRING),
                    { e: UiState, v: String? -> e.navIndex = v },
                    { e: UiState -> e.navIndex }).add()
                .append(
                    KeyedCodec("CId", Codec.STRING),
                    { e: UiState, v: String? -> e.componentId = v },
                    { e: UiState -> e.componentId }).add()
                .append(
                    KeyedCodec("@CId", Codec.STRING),
                    { e: UiState, v: String? -> e.componentId = v },
                    { e: UiState -> e.componentId }).add()
                .append(
                    KeyedCodec("@CText", Codec.STRING),
                    { e: UiState, v: String? -> e.componentText = v },
                    { e: UiState -> e.componentText }).add()
                .append(
                    KeyedCodec("@CPerm", Codec.STRING),
                    { e: UiState, v: String? -> e.componentPermission = v },
                    { e: UiState -> e.componentPermission }).add()
                .build()
        }
    }

    companion object {
        const val LAYOUT: String = "HeroChat/ChatPage.ui"

        val NavBtnStyle: Value<String> = Value.ref("HeroChat/Sidebar/Sidebar.ui", "NavigationButtonStyle")
        val NavBtnSelectedStyle: Value<String> =
            Value.ref("HeroChat/Sidebar/Sidebar.ui", "NavigationButtonSelectedStyle")
    }

    fun closePage() {
        close()
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