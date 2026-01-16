package com.github.heroslender.herochat

import com.github.heroslender.herochat.config.ChatConfig
import com.github.heroslender.herochat.config.ComponentConfig
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.util.Config

class HeroChat(init: JavaPluginInit) : JavaPlugin(init) {
    private val _config: Config<ChatConfig> = withConfig(ChatConfig.CODEC)
    lateinit var config: ChatConfig
        private set

    override fun setup() {
        config = _config.get()
        _config.save()

        eventRegistry.register(PlayerChatEvent::class.java) { event ->
            event.formatter = formatter
        }
    }

    val formatter = PlayerChatEvent.Formatter { player: PlayerRef, msg: String ->
        ComponentParser.parse(player, config.chatFormat, config.components + ("message" to ComponentConfig(msg)))
    }
}