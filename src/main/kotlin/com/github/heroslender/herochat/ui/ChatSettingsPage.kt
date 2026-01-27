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
            pageInitializer = { SettingsSubPage(this, channelManager, playerRef) }
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

    class UiState {
        var action: String? = null

        // Navigation
        var navigateTo: String? = null

        // Component popup data
        var componentId: String? = null
        var componentText: String? = null
        var componentPermission: String? = null

        // Settings page props
        var defaultChannel: String? = null

        // Channel page props
        var format: String? = null
        var permission: String? = null
        var crossWorld: Boolean? = null
        var distance: Double? = null

        companion object {
            val CODEC: BuilderCodec<UiState?> = BuilderCodec.builder(
                UiState::class.java, { UiState() })
                .append(
                    KeyedCodec("Action", Codec.STRING),
                    { e, v -> e.action = v },
                    { e -> e.action }).add()
                .append(
                    KeyedCodec("NavigateTo", Codec.STRING),
                    { e, v -> e.navigateTo = v },
                    { e -> e.navigateTo }).add()
                .append(
                    KeyedCodec("CId", Codec.STRING),
                    { e, v -> e.componentId = v },
                    { e -> e.componentId }).add()
                .append(
                    KeyedCodec("@CId", Codec.STRING),
                    { e, v -> e.componentId = v },
                    { e -> e.componentId }).add()
                .append(
                    KeyedCodec("@CText", Codec.STRING),
                    { e, v -> e.componentText = v },
                    { e -> e.componentText }).add()
                .append(
                    KeyedCodec("@CPerm", Codec.STRING),
                    { e, v -> e.componentPermission = v },
                    { e -> e.componentPermission }).add()
                // Settings page props
                .append(
                    KeyedCodec("@DefaultChannel", Codec.STRING),
                    { e, v -> e.defaultChannel = v },
                    { e -> e.defaultChannel }).add()
                // Channel page props
                .append(
                    KeyedCodec("@Format", Codec.STRING),
                    { e, v -> e.format = v },
                    { e -> e.format }).add()
                .append(
                    KeyedCodec("@Permission", Codec.STRING),
                    { e, v -> e.permission = v },
                    { e -> e.permission }).add()
                .append(
                    KeyedCodec("@CrossWorld", Codec.BOOLEAN),
                    { e, v -> e.crossWorld = v },
                    { e -> e.crossWorld }).add()
                .append(
                    KeyedCodec("@Distance", Codec.DOUBLE),
                    { e, v -> e.distance = v },
                    { e -> e.distance }).add()
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