package com.github.heroslender.herochat.commands

import com.github.heroslender.herochat.Permissions
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.service.UserService
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.concurrent.CompletableFuture

class UnblockCommand(private val userService: UserService) :
    AbstractAsyncCommand("unblock", "Unblock a player in your chat") {
    private val targetArg: RequiredArg<PlayerRef> = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)

    init {
        requirePermission(Permissions.COMMAND_UNBLOCK)
    }

    override fun canGeneratePermission(): Boolean {
        return false
    }

    override fun executeAsync(ctx: CommandContext): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val sender = userService.getUser(ctx.sender().uuid) ?: return@runAsync
            val target = targetArg.get(ctx)

            if (sender.uuid == target.uuid) {
                sender.sendMessage(MessagesConfig::unblockSelf)
                return@runAsync
            }

            if (userService.unblockPlayer(sender.uuid, target.uuid)) {
                sender.sendMessage(MessagesConfig::unblockSuccess, "player" to target.username)
                return@runAsync
            }

            sender.sendMessage(MessagesConfig::unblockNotBlocked, "player" to target.username)
        }
    }
}
