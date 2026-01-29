package com.github.heroslender.herochat

import com.github.heroslender.herochat.chat.PrivateChannel
import com.github.heroslender.herochat.commands.ChatCommand
import com.github.heroslender.herochat.config.ChannelConfig
import com.github.heroslender.herochat.config.ChatConfig
import com.github.heroslender.herochat.config.PrivateChannelConfig
import com.hypixel.hytale.common.plugin.PluginIdentifier
import com.hypixel.hytale.common.semver.SemverRange
import com.hypixel.hytale.event.EventPriority
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.plugin.PluginManager
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.util.Config
import java.io.File
import java.util.concurrent.CompletableFuture

class HeroChat(init: JavaPluginInit) : JavaPlugin(init) {
    private val _config: Config<ChatConfig> = withConfig(ChatConfig.CODEC)
    val config: ChatConfig
        get() = _config.get()

    private val _privateChannelConfig: Config<PrivateChannelConfig> = withConfig("channels/${PrivateChannel.ID}", PrivateChannelConfig.CODEC)
    val privateChannelConfig: PrivateChannelConfig
        get() = _privateChannelConfig.get()

    private val _channelConfigs: Map<String, Config<ChannelConfig>> = setupChannelConfigs()
    val channelConfigs: Map<String, ChannelConfig>
        get() = _channelConfigs.mapValues { it.value.get() }

    lateinit var channelManager: ChannelManager
        private set

    var isLuckpermsEnabled: Boolean = false
        private set

    companion object {
        val LuckPermsId = PluginIdentifier("LuckPerms", "LuckPerms")
        lateinit var instance: HeroChat
    }

    init {
        instance = this
    }

    override fun setup() {
        _config.save()
        _privateChannelConfig.save()
        _channelConfigs.values.forEach { it.save() }

        isLuckpermsEnabled = PluginManager.get().hasPlugin(LuckPermsId, SemverRange.WILDCARD)

        channelManager = ChannelManager(this, config, channelConfigs, privateChannelConfig)
    }

    override fun start() {
        eventRegistry.register(EventPriority.EARLY, PlayerChatEvent::class.java) { event ->
            event.isCancelled = true
            val executor = Universe.get().getWorld(event.sender.worldUuid ?: return@register)
            val player = CompletableFuture.supplyAsync({
                event.sender.reference?.store?.getComponent(
                    event.sender.reference ?: return@supplyAsync null,
                    Player.getComponentType()
                )
            }, executor).join() ?: return@register

            channelManager.defaultChannel?.sendMessage(player, event.content)
        }

        commandRegistry.registerCommand(ChatCommand())
    }

    fun saveConfig() {
        _config.save()
    }

    fun saveChannelConfig(channelId: String) {
        val cfg = if (channelId == PrivateChannel.ID) _privateChannelConfig else _channelConfigs[channelId]
        cfg?.save()
        channelManager.reloadChannel(channelId)
    }

    private fun setupChannelConfigs(): Map<String, Config<ChannelConfig>> {
        val channelsFolder = File(dataDirectory.toFile(), "channels")
        if (!channelsFolder.exists()) {
            channelsFolder.mkdirs()
        }

        val files = channelsFolder.listFiles()
        if (files == null) {
            logger.atSevere().log("Could not load channels folder!")
            return emptyMap()
        }

        if (files.size == 0) {
            val channelConfig = withConfig("channels/global", ChannelConfig.CODEC)
            return mapOf("global" to channelConfig)
        }

        val channels = mutableMapOf<String, Config<ChannelConfig>>()
        for (file in files) {
            if (!file.extension.equals("json", true)
                || file.nameWithoutExtension == PrivateChannel.ID
            ) {
                continue
            }

            val channelConfig = withConfig("channels/${file.nameWithoutExtension}", ChannelConfig.CODEC)
            channels[file.nameWithoutExtension] = channelConfig
        }

        return channels
    }
}