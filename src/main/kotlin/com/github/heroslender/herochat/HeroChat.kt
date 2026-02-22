package com.github.heroslender.herochat

import com.github.heroslender.herochat.channel.PrivateChannel
import com.github.heroslender.herochat.commands.ChatCommand
import com.github.heroslender.herochat.commands.NicknameCommand
import com.github.heroslender.herochat.config.*
import com.github.heroslender.herochat.database.Database
import com.github.heroslender.herochat.database.UserSettingsRepository
import com.github.heroslender.herochat.listeners.AutoModListener
import com.github.heroslender.herochat.listeners.ChatListener
import com.github.heroslender.herochat.listeners.PlayerListener
import com.github.heroslender.herochat.service.ChannelService
import com.github.heroslender.herochat.service.UserService
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.util.Config
import java.io.File

class HeroChat(init: JavaPluginInit) : JavaPlugin(init) {
    private val _config: Config<ChatConfig> = withConfig(ChatConfig.CODEC)
    val config: ChatConfig
        get() = _config.get()

    private val _autoModConfig: Config<AutoModConfig> = withConfig("automod", AutoModConfig.CODEC)
    val autoModConfig: AutoModConfig
        get() = _autoModConfig.get()

    private val _messagesConfig: Config<MessagesConfig> = withConfig("messages", MessagesConfig.CODEC)
    val messages: MessagesConfig
        get() = _messagesConfig.get()

    private val _privateChannelConfig: Config<PrivateChannelConfig> =
        withConfig("channels/${PrivateChannel.ID}", PrivateChannelConfig.CODEC)
    val privateChannelConfig: PrivateChannelConfig
        get() = _privateChannelConfig.get()

    private val _channelConfigs: Map<String, Config<ChannelConfig>> = setupChannelConfigs()
    val channelConfigs: Map<String, ChannelConfig>
        get() = _channelConfigs.mapValues { it.value.get() }

    lateinit var database: Database
        private set
    lateinit var userService: UserService
        private set
    lateinit var channelService: ChannelService
        private set

    companion object {
        lateinit var instance: HeroChat
    }

    init {
        instance = this
    }

    override fun setup() {
        _config.save()
        _autoModConfig.save()
        _messagesConfig.save()
        _privateChannelConfig.save()
        _channelConfigs.values.forEach { it.save() }

        database = Database(dataDirectory.toFile())
        val repository = UserSettingsRepository(database)
        userService = UserService(repository, logger.getSubLogger("UserService"))
        channelService = ChannelService(this, config)
    }

    override fun start() {
        Universe.get().players.forEach(userService::onJoin)

        PlayerListener(userService, channelService)
        ChatListener(userService)
        AutoModListener(config, autoModConfig)

        commandRegistry.registerCommand(ChatCommand(userService))
        commandRegistry.registerCommand(NicknameCommand(userService))
    }

    override fun shutdown() {
        if (::userService.isInitialized) {
            userService.unloadAll()
        }
        if (::database.isInitialized) {
            database.close()
        }
    }

    fun saveConfig() {
        _config.save()
    }

    fun saveAutomodConfig() {
        _autoModConfig.save()
    }

    fun saveChannelConfig(channelId: String) {
        val cfg = if (channelId == PrivateChannel.ID) _privateChannelConfig else _channelConfigs[channelId]
        cfg?.save()
        channelService.reloadChannel(channelId)
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