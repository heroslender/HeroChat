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

class BlockCommand(private val userService: UserService) :
    AbstractAsyncCommand("block", "Block a player from appearing in your chat") {
    private val targetArg: RequiredArg<PlayerRef> = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)

    init {
        requirePermission(Permissions.COMMAND_BLOCK)
    }

    override fun canGeneratePermission(): Boolean {
        return false
    }

    override fun executeAsync(ctx: CommandContext): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val sender = userService.getUser(ctx.sender().uuid) ?: return@runAsync
            val target = targetArg.get(ctx)

            if (sender.uuid == target.uuid) {
                sender.sendMessage(MessagesConfig::blockSelf)
                return@runAsync
            }

            if (userService.blockPlayer(sender.uuid, target.uuid)) {
                sender.sendMessage(MessagesConfig::blockSuccess, "player" to target.username)
                return@runAsync
            }

            sender.sendMessage(MessagesConfig::blockAlready, "player" to target.username)
        }
    }
}
