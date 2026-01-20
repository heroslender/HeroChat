package com.github.heroslender.herochat

import com.github.heroslender.herochat.commands.OpenUICommand
import com.github.heroslender.herochat.config.ChatConfig
import com.github.heroslender.herochat.config.ComponentConfig
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.util.Config

class HeroChat(init: JavaPluginInit) : JavaPlugin(init) {
    private val _config: Config<ChatConfig> = withConfig(ChatConfig.CODEC)
    val config: ChatConfig
        get() = _config.get()

    companion object {
        lateinit var instance: HeroChat
    }

    init {
        instance = this
    }

    override fun setup() {
        _config.save()
    }

    override fun start() {
        eventRegistry.register(PlayerChatEvent::class.java) { event ->
            event.formatter = formatter
        }

        commandRegistry.registerCommand(OpenUICommand())
    }

    val formatter = PlayerChatEvent.Formatter { player: PlayerRef, msg: String ->
        ComponentParser.parse(player, config.chatFormat, config.components + ("message" to ComponentConfig(msg)))
    }

    fun saveConfig() {
        _config.save()
    }
}