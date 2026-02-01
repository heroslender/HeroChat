package com.github.heroslender.herochat.commands

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.chat.PrivateChannel
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.CommandUtil
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.concurrent.CompletableFuture

class PrivateChannelCommand(val channel: PrivateChannel) :
    AbstractAsyncCommand(channel.commands.first(), "Sends a chat message in a specific channel") {
    private val targetArg: RequiredArg<PlayerRef> = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
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
            val sender = ctx.sender()
            val target = targetArg.get(ctx)
            val rawArgs = CommandUtil.stripCommandName(ctx.inputString)
            val indexOf = rawArgs.indexOf(' ')
            if (indexOf >= 0) {
                val msg = rawArgs.substring(indexOf + 1)
                channel.sendMessage(sender, msg, target)
                return@runAsync
            }

            // [NEW] Update settings to focus on this player
            HeroChat.instance.userService.updateSettings(sender.uuid) {
                it.focusedChannelId = PrivateChannel.ID
                it.focusedPrivateTarget = target.uuid
            }

            sender.sendMessage(MessagesConfig::privateChatStarted, "target" to target.username)
        }
    }
}
