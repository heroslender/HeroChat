package com.github.heroslender.herochat.commands

import com.github.heroslender.herochat.chat.Channel
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.CommandUtil
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand
import java.util.concurrent.CompletableFuture


class ChannelCommand(val channel: Channel) :
    AbstractAsyncCommand(channel.commands.first(), "Sends a chat message in a specific channel") {
    private val msgArg: RequiredArg<String> = withRequiredArg("msg", "message", ArgTypes.STRING)

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
            val rawArgs = CommandUtil.stripCommandName(ctx.inputString).trim { it <= ' ' }
            channel.sendMessage(ctx.sender(), rawArgs)
        }
    }
}
