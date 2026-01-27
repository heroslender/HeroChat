package com.github.heroslender.herochat.ui

import com.github.heroslender.herochat.ChannelManager
import com.github.heroslender.herochat.ui.navigation.NavController
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

class ChatSettingsPage(
    playerRef: PlayerRef,
    val channelManager: ChannelManager,
) : InteractiveCustomUIPage<ChatSettingsPage.UiState>(
    playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, UiState.CODEC
) {
    private val navController = NavController<UiState>(Destination.Settings, "#PageContent")

    override fun build(
        ref: Ref<EntityStore?>,
        cmd: UICommandBuilder,
        evt: UIEventBuilder,
        store: Store<EntityStore?>
    ) {
        // Load the layout
        cmd.append(LAYOUT)

        navController.addNavLocation(
            location = Destination.Settings,
            onEnter = { cmd -> cmd["#ShowSettingsBtn.Style"] = NavBtnSelectedStyle },
            onLeave = { cmd -> cmd["#ShowSettingsBtn.Style"] = NavBtnStyle },
            pageInitializer = { SettingsSubPage(this, playerRef) }
        )
        evt.onActivating("#ShowSettingsBtn", "NavigateTo" to Destination.Settings)

        channelManager.channels.values.forEachIndexed { i, channel ->
            cmd.append("#NavChannels", "HeroChat/Sidebar/SidebarButton.ui")
            cmd["#NavChannels[$i].Text"] = channel.name
            evt.onActivating("#NavChannels[$i]", "NavigateTo" to Destination.Channel(channel.id))

            navController.addNavLocation(
                location = Destination.Channel(channel.id),
                onEnter = { cmd -> cmd["#NavChannels[$i].Style"] = NavBtnSelectedStyle },
                onLeave = { cmd -> cmd["#NavChannels[$i].Style"] = NavBtnStyle },
                pageInitializer = { ChannelSubPage(this, channel, playerRef) }
            )
        }

        navController.build(ref, cmd, evt, store)
    }

    override fun handleDataEvent(
        @Nonnull ref: Ref<EntityStore?>,
        @Nonnull store: Store<EntityStore?>,
        @Nonnull data: UiState
    ) {
        val navDest = data.navigateTo
        if (navDest != null) {
            runUiCmdEvtUpdate { cmd, evt ->
                navController.navigateTo(navDest, ref, cmd, evt, store)
            }

            return
        }

        navController.currentPage?.handleDataEvent(ref, store, data)
    }

    // Event data class with codec
    class UiState {
        var navigateTo: String? = null
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
                    KeyedCodec("NavigateTo", Codec.STRING),
                    { e: UiState, v: String? -> e.navigateTo = v },
                    { e: UiState -> e.navigateTo }).add()
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

    object Destination {
        const val Settings = "Settings"

        fun Channel(channel: String): String = "channel-$channel"
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