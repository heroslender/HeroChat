package com.github.heroslender.herochat.commands

import com.github.heroslender.herochat.channel.PrivateChannel
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.service.UserService
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.CommandUtil
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.concurrent.CompletableFuture

class PrivateChannelCommand(val channel: PrivateChannel, private val userService: UserService) :
    AbstractAsyncCommand(channel.commands.first(), "Sends a chat message in a specific channel"),
    IChannelCommand {
    private val targetArg: RequiredArg<PlayerRef> = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val msgArg: OptionalArg<String> = withOptionalArg("msg", "message", ArgTypes.STRING)
    override val aliases: Array<String>
        get() = channel.commands

    init {
        setAllowsExtraArguments(true)

        if (channel.commands.size > 1) {
            addAliases(*channel.commands.drop(1).toTypedArray())
        }
    }

    override fun canGeneratePermission(): Boolean {
        return false
    }

    override fun executeAsync(ctx: CommandContext): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val sender = userService.getUser(ctx.sender().uuid) ?: return@runAsync
            val rawArgs = CommandUtil.stripCommandName(ctx.inputString)

            execute(sender, rawArgs)
        }
    }

    override fun execute(sender: User, message: String) {
        if (message.isEmpty()) {
            sender.sendMessage(Message.raw("Usage: $name <player> [message]"))
            return
        }

        val indexOf = message.indexOf(' ')
        val targetName = if (indexOf == -1) message else message.substring(0, indexOf)
        val target = userService.getUser(targetName)
        if (target == null) {
            sender.sendMessage(MessagesConfig::privateChatPlayerNotFound)
            return
        }

        if (indexOf == -1) {
            with(userService) {
                sender.updateSettings {
                    it.focusedChannelId = PrivateChannel.ID
                    it.focusedPrivateTarget = target.uuid
                }
            }

            sender.sendMessage(MessagesConfig::privateChatStarted, "target" to target.username)
            return
        }

        channel.sendMessage(sender, target, message.substring(indexOf + 1))
    }
}
