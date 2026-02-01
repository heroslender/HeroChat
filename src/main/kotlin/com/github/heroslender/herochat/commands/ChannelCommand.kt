package com.github.heroslender.herochat.commands

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.chat.Channel
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.CommandUtil
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand
import java.util.concurrent.CompletableFuture

class ChannelCommand(val channel: Channel) :
    AbstractAsyncCommand(channel.commands.first(), "Sends a chat message in a specific channel") {
    private val msgArg: OptionalArg<String> = withOptionalArg("msg", "message", ArgTypes.STRING)

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
            if (ctx.inputString.indexOf(' ') >= 0) {
                val msg = CommandUtil.stripCommandName(ctx.inputString)
                channel.sendMessage(ctx.sender(), msg)
                return@runAsync
            }

            val sender = ctx.sender()
            HeroChat.instance.userService.updateSettings(sender.uuid) {
                it.focusedChannelId = channel.id
            }

            sender.sendMessage(MessagesConfig::channelJoined, "channel" to channel.name)
        }
    }
}
