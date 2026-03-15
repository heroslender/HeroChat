package com.github.heroslender.herochat.channel

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.Permissions
import com.github.heroslender.herochat.commands.IChannelCommand
import com.github.heroslender.herochat.commands.PrivateChannelCommand
import com.github.heroslender.herochat.commands.ReplyCommand
import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.config.PrivateChannelConfig
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.event.PreChatEvent
import com.github.heroslender.herochat.event.PrivateChannelChatEvent
import com.github.heroslender.herochat.message.ColorParser
import com.github.heroslender.herochat.message.MessageParser
import com.github.heroslender.herochat.service.UserService
import com.github.heroslender.herochat.utils.runInWorld
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.command.system.CommandRegistration

class PrivateChannel(
    config: PrivateChannelConfig,
    private val userService: UserService,
    private val logger: HytaleLogger,
) : Channel {
    override val id: String = ID
    override val name: String = config.name
    override val commands: Array<String> = config.commands
    val replyCommands: Array<String> = config.replyCommands
    val format: String = config.senderFormat
    val receiverFormat: String = config.receiverFormat
    override val permission: String? = config.permission
    override val capslockFilter: CapslockFilter = CapslockFilter(config.capslockFilter)
    val cooldowns: Map<String, Long> = config.cooldowns
    val components: Map<String, ComponentConfig> = config.components

    var command: IChannelCommand? = null
        private set
    private var commandRegistration: CommandRegistration? = null

    var replyCommand: IChannelCommand? = null
        private set
    private var replyCommandRegistration: CommandRegistration? = null

    override fun load() {
        val commandRegistry = HeroChat.instance.commandRegistry
        if (commands.isNotEmpty()) {
            val cmd = PrivateChannelCommand(this, userService)
            command = cmd
            commandRegistration = commandRegistry.registerCommand(cmd)
            logger.atInfo()
                .log("Registered channel command ${cmd.name}${cmd.aliases.joinToString(", ", " with aliases: ")}.")
        }

        if (replyCommands.isNotEmpty()) {
            val cmd = ReplyCommand(this, userService)
            replyCommand = cmd
            replyCommandRegistration = commandRegistry.registerCommand(cmd)
            logger.atInfo()
                .log("Registered channel command ${cmd.name}${cmd.aliases.joinToString(", ", " with aliases: ")}.")
        }
    }

    override fun unload() {
        commandRegistration?.unregister()
        command = null
        commandRegistration = null

        replyCommandRegistration?.unregister()
        replyCommand = null
        replyCommandRegistration = null
    }

    override fun sendMessage(sender: User, msg: String) {
        val settings = sender.settings
        val targetUuid = settings.focusedPrivateTarget
        if (targetUuid == null) {
            sender.sendMessage(MessagesConfig::privateChatNotActive)
            return
        }

        val target = userService.getUser(targetUuid)
        if (target == null) {
            sender.sendMessage(MessagesConfig::privateChatPlayerNotFound)
            return
        }

        sendMessage(sender, target, msg)
    }

    fun sendMessage(
        sender: User,
        target: User,
        msg: String
    ) {
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(MessagesConfig::channelNoPermission)
            return
        }

        if (target.uuid == sender.uuid) {
            sender.sendMessage(MessagesConfig::privateChatSelf)
            return
        }

        HytaleServer.get()
            .eventBus
            .dispatchForAsync(PreChatEvent::class.java)
            .dispatch(
                PreChatEvent(
                    sender = sender,
                    channel = this,
                    message = msg
                )
            ).whenComplete { event, throwable -> onPreChatEvent(event, target, throwable) }
    }

    fun onPreChatEvent(event: PreChatEvent, target: User, throwable: Throwable?) {
        if (throwable != null) {
            throwable.printStackTrace()
            return
        }

        dispatchTestChatEvent(
            sender = event.sender,
            recipients = mutableListOf(target),
            message = event.message
        ).whenComplete { e, throwable ->
            if (throwable != null) {
                throwable.printStackTrace()
                return@whenComplete
            }

            if (e != null && e.isCancelled) {
                return@whenComplete
            }

            HytaleServer.get()
                .eventBus
                .dispatchForAsync(PrivateChannelChatEvent::class.java)
                .dispatch(
                    PrivateChannelChatEvent(
                        sender = event.sender,
                        channel = this,
                        target = target,
                        message = event.message
                    )
                ).whenComplete(::onChatEvent)
        }
    }

    fun onChatEvent(event: PrivateChannelChatEvent, throwable: Throwable?) {
        if (throwable != null) {
            throwable.printStackTrace()
            return
        }

        val sender = event.sender
        val target = event.target
        val message = ColorParser.validateFormat(
            user = event.sender,
            message = event.message,
            basePermission = Permissions.CHAT_MESSAGE_STYLE,
        )
        val comp = HeroChat.instance.config.components +
                components +
                ("message" to ComponentConfig(message)) +
                ("target_username" to ComponentConfig(target.username))

        // Some placeholders require the world thread to work
        sender.runInWorld {
            val message = MessageParser.parse(sender, format, comp)
            val receivedMessage = MessageParser.parse(sender, receiverFormat, comp)

            sender.sendMessage(message)
            target.sendMessage(receivedMessage)

            sender.lastPrivateMessageSource = target.uuid
            target.lastPrivateMessageSource = sender.uuid
        }
    }

    companion object {
        const val ID = "tell"
    }
}
