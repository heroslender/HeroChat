package com.github.heroslender.herochat.ui.pages.settings

import com.github.heroslender.herochat.ChannelManager
import com.github.heroslender.herochat.ui.Page
import com.github.heroslender.herochat.ui.event.ActionEventData
import com.github.heroslender.herochat.ui.navigation.NavController
import com.github.heroslender.herochat.utils.onActivating
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.ui.Value
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import javax.annotation.Nonnull

class ChatSettingsPage(
    playerRef: PlayerRef,
    val channelManager: ChannelManager,
) : Page<ChatSettingsPage.UiState>(playerRef, UiState.CODEC) {
    private val navController = NavController<UiState>(Destination.Settings, "#PageContent")

    override fun build(ref: Ref<EntityStore?>, cmd: UICommandBuilder, evt: UIEventBuilder, store: Store<EntityStore?>) {
        cmd.append(LAYOUT)

        navController.addNavLocation(
            location = Destination.Settings,
            onEnter = { cmd -> cmd["#ShowSettingsBtn.Style"] = NavBtnSelectedStyle },
            onLeave = { cmd -> cmd["#ShowSettingsBtn.Style"] = NavBtnStyle },
            pageInitializer = { SettingsSubPage(this, channelManager) }
        )
        evt.onActivating("#ShowSettingsBtn", "NavigateTo" to Destination.Settings)

        cmd.append("#NavChannels", "HeroChat/Sidebar/SidebarButton.ui")
        cmd["#NavChannels[0].Text"] = channelManager.privateChannel.name
        evt.onActivating("#NavChannels[0]", "NavigateTo" to Destination.Channel(channelManager.privateChannel.id))
        navController.addNavLocation(
            location = Destination.Channel(channelManager.privateChannel.id),
            onEnter = { cmd -> cmd["#NavChannels[0].Style"] = NavBtnSelectedStyle },
            onLeave = { cmd -> cmd["#NavChannels[0].Style"] = NavBtnStyle },
            pageInitializer = { PrivateChannelSubPage(this, channelManager.privateChannel) }
        )

        channelManager.channels.values.forEachIndexed { i, channel ->
            val i = i + 1
            cmd.append("#NavChannels", "HeroChat/Sidebar/SidebarButton.ui")
            cmd["#NavChannels[$i].Text"] = channel.name
            evt.onActivating("#NavChannels[$i]", "NavigateTo" to Destination.Channel(channel.id))

            navController.addNavLocation(
                location = Destination.Channel(channel.id),
                onEnter = { cmd -> cmd["#NavChannels[$i].Style"] = NavBtnSelectedStyle },
                onLeave = { cmd -> cmd["#NavChannels[$i].Style"] = NavBtnStyle },
                pageInitializer = { ChannelSubPage(this, channel) }
            )
        }

        navController.build(ref, cmd, evt, store)
    }

    override fun handleDataEvent(
        @Nonnull ref: Ref<EntityStore?>,
        @Nonnull store: Store<EntityStore?>,
        @Nonnull data: UiState
    ) {
        super.handleDataEvent(ref, store, data)

        val navDest = data.navigateTo
        if (navDest != null) {
            runUiCmdEvtUpdate { cmd, evt ->
                navController.navigateTo(navDest, ref, cmd, evt, store)
            }

            return
        }

        navController.currentPage?.handleDataEvent(ref, store, data)
    }

    class UiState: ActionEventData {
        override var action: String? = null

        // Navigation
        var navigateTo: String? = null

        // Component popup data
        var componentId: String? = null
        var componentText: String? = null
        var componentPermission: String? = null

        // Settings page props
        var defaultChannel: String? = null

        // Private channel props
        var receiverFormat: String? = null

        // Channel page props
        var format: String? = null
        var permission: String? = null
        var crossWorld: Boolean? = null
        var distance: Double? = null

        companion object {
            val CODEC: BuilderCodec<UiState> = BuilderCodec.builder(
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
                // Private channel props
                .append(
                    KeyedCodec("@ReceiverFormat", Codec.STRING),
                    { e, v -> e.receiverFormat = v },
                    { e -> e.receiverFormat }).add()
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
}