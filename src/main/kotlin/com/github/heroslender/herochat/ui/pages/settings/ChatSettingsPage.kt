package com.github.heroslender.herochat.ui.pages.settings

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.channel.PrivateChannel
import com.github.heroslender.herochat.channel.StandardChannel
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.service.ChannelService
import com.github.heroslender.herochat.ui.Page
import com.github.heroslender.herochat.ui.navigation.NavController
import com.github.heroslender.herochat.ui.pages.settings.automod.AutomodSubPage
import com.github.heroslender.herochat.ui.pages.settings.channel.ChannelSubPage
import com.github.heroslender.herochat.ui.pages.settings.general.SettingsSubPage
import com.github.heroslender.herochat.ui.pages.settings.privatechannel.PrivateChannelSubPage
import com.github.heroslender.herochat.utils.onActivating
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
    val channelService: ChannelService,
) : Page<UiState>(playerRef, UiState.CODEC) {
    private val navController = NavController<UiState>(Destination.Settings, "#PageContent")
    val user: User = HeroChat.instance.userService.getUser(playerRef)!!

    override fun build(ref: Ref<EntityStore?>, cmd: UICommandBuilder, evt: UIEventBuilder, store: Store<EntityStore?>) {
        cmd.append(LAYOUT)

        navController.addNavLocation(
            location = Destination.Settings,
            onEnter = { cmd -> cmd["#ShowSettingsBtn.Style"] = NavBtnSelectedStyle },
            onLeave = { cmd -> cmd["#ShowSettingsBtn.Style"] = NavBtnStyle },
            pageInitializer = { SettingsSubPage(this, channelService) }
        )
        evt.onActivating("#ShowSettingsBtn", "NavigateTo" to Destination.Settings)

        navController.addNavLocation(
            location = Destination.Automod,
            onEnter = { cmd -> cmd["#ShowAutomodBtn.Style"] = NavBtnSelectedStyle },
            onLeave = { cmd -> cmd["#ShowAutomodBtn.Style"] = NavBtnStyle },
            pageInitializer = { AutomodSubPage(this, user, HeroChat.instance.autoModConfig) }
        )
        evt.onActivating("#ShowAutomodBtn", "NavigateTo" to Destination.Automod)

        channelService.channels.values.forEachIndexed { i, channel ->
            cmd.append("#NavChannels", "HeroChat/Sidebar/SidebarButton.ui")
            cmd["#NavChannels[$i].Text"] = channel.name
            evt.onActivating("#NavChannels[$i]", "NavigateTo" to Destination.Channel(channel.id))

            navController.addNavLocation(
                location = Destination.Channel(channel.id),
                onEnter = { cmd -> cmd["#NavChannels[$i].Style"] = NavBtnSelectedStyle },
                onLeave = { cmd -> cmd["#NavChannels[$i].Style"] = NavBtnStyle },
                pageInitializer = {
                    when (channel) {
                        is StandardChannel -> ChannelSubPage(this, user, channel)
                        is PrivateChannel -> PrivateChannelSubPage(this, user, channel)
                        else -> throw IllegalStateException("Unknown channel type: ${channel.javaClass.name}")
                    }
                }
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


    companion object {
        const val LAYOUT: String = "HeroChat/ChatPage.ui"

        val NavBtnStyle: Value<String> = Value.ref("HeroChat/Sidebar/Sidebar.ui", "NavigationButtonStyle")
        val NavBtnSelectedStyle: Value<String> =
            Value.ref("HeroChat/Sidebar/Sidebar.ui", "NavigationButtonSelectedStyle")
    }

    object Destination {
        const val Settings = "Settings"
        const val Automod = "Automod"

        fun Channel(channel: String): String = "channel-$channel"
    }
}