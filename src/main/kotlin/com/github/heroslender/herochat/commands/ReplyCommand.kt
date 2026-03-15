package com.github.heroslender.herochat.commands

import com.github.heroslender.herochat.channel.PrivateChannel
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.service.UserService
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.CommandUtil
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand
import java.util.concurrent.CompletableFuture

class ReplyCommand(val channel: PrivateChannel, private val userService: UserService) :
    AbstractAsyncCommand(channel.replyCommands.first(), "Reply to the last private message"),
    IChannelCommand {
    private val msgArg: OptionalArg<String> = withOptionalArg("msg", "message", ArgTypes.STRING)
    override val aliases: Array<String>
        get() = channel.replyCommands


    init {
        setAllowsExtraArguments(true)

        if (channel.replyCommands.size > 1) {
            addAliases(*channel.replyCommands.drop(1).toTypedArray())
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
        if (message.isBlank()) {
            with(userService) {
                sender.updateSettings {
                    it.focusedChannelId = PrivateChannel.ID
                }
            }

            val target = sender.lastPrivateMessageSource?.let { userService.getUser(it)?.username } ?: "Unknown"
            sender.sendMessage(MessagesConfig::privateChatStarted, "target" to target)
            return
        }


        channel.sendMessage(sender, message)
        return
    }
}
