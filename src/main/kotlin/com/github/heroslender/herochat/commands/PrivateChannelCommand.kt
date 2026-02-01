package com.github.heroslender.herochat.commands

import com.github.heroslender.herochat.chat.PrivateChannel
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.CommandUtil
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.concurrent.CompletableFuture

class PrivateChannelCommand(val channel: PrivateChannel) :
    AbstractAsyncCommand(channel.commands.first(), "Sends a chat message in a specific channel") {
    private val targetArg: RequiredArg<PlayerRef> = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
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
            val rawArgs = CommandUtil.stripCommandName(ctx.inputString)
                .trim { it <= ' ' }.apply {
                    println(this)
                    substring(indexOf(' ') + 1)
                }.also { println(it) }
            channel.sendMessage(ctx.sender(), rawArgs, targetArg.get(ctx))
        }
    }
}
