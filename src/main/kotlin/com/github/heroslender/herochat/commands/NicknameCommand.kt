package com.github.heroslender.herochat.commands

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.Permissions
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.message.ComponentParser
import com.github.heroslender.herochat.service.UserService
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.server.core.command.system.AbstractCommand
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection
import java.util.concurrent.CompletableFuture

class NicknameCommand(userService: UserService) : AbstractCommandCollection("nickname", "Change your nickname") {

    init {
        requirePermission(Permissions.COMMAND_NICKNAME)

        addSubCommand(NicknameSetCommand(userService))
        addSubCommand(NicknameClearCommand(userService))
    }

    class NicknameSetCommand(private val userService: UserService) : AbstractCommand("set", "Set your nickname") {
        val nicknameArg = withRequiredArg("nickname", "Your new nickname", ArgTypes.STRING)

        override fun execute(ctx: CommandContext): CompletableFuture<Void> {
            return CompletableFuture.runAsync {
                val sender = userService.getUser(ctx.sender().uuid) ?: return@runAsync

                var nickname = nicknameArg.get(ctx)
                val striped = ComponentParser.stripStyle(nickname)
                if (striped.length > HeroChat.instance.config.nicknameMaxLength) {
                    sender.sendMessage(MessagesConfig::nicknameTooLong)
                    return@runAsync
                }

                if (striped.contains(' ')) {
                    sender.sendMessage(MessagesConfig::nicknameContainsSpaces)
                    return@runAsync
                }

                nickname = ComponentParser.validateFormat(
                    user = sender,
                    message = nickname,
                    basePermission = Permissions.NICKNAME_BASE_PERMISSION,
                    remove = true
                )

                with(userService) {
                    sender.updateSettings { settings ->
                        settings.nickname = nickname

                    }
                }
                sender.sendMessage(MessagesConfig::nicknameSet, "nickname" to nickname)
            }
        }
    }


    class NicknameClearCommand(private val userService: UserService) : AbstractCommand("clear", "Set your nickname") {

        override fun execute(ctx: CommandContext): CompletableFuture<Void> {
            return CompletableFuture.runAsync {
                val sender = userService.getUser(ctx.sender().uuid) ?: return@runAsync

                with(userService) {
                    sender.updateSettings { settings ->
                        settings.nickname = null
                    }
                }
                sender.sendMessage(MessagesConfig::nicknameReset)
            }
        }
    }
}