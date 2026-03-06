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

class IgnoreCommand(private val userService: UserService) :
    AbstractAsyncCommand("ignore", "Ignore a player, hiding their messages.") {
    private val targetArg: RequiredArg<PlayerRef> = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)

    init {
        requirePermission(Permissions.COMMAND_IGNORE)
    }

    override fun canGeneratePermission(): Boolean {
        return false
    }

    override fun executeAsync(ctx: CommandContext): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val sender = userService.getUser(ctx.sender().uuid) ?: return@runAsync
            val target = targetArg.get(ctx)

            if (sender.uuid == target.uuid) {
                sender.sendMessage(MessagesConfig::ignoreSelf)
                return@runAsync
            }

            if (sender.settings.ignoredUsers.contains(target.uuid)) {
                sender.sendMessage(MessagesConfig::ignoreAlready, "player" to target.username)
                return@runAsync
            }

            with(userService) {
                sender.ignorePlayer(target.uuid)
                sender.sendMessage(MessagesConfig::ignoreSuccess, "player" to target.username)
            }
        }
    }
}
