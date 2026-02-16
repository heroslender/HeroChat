package com.github.heroslender.herochat.commands

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.Permissions
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.data.PlayerUser
import com.github.heroslender.herochat.service.UserService
import com.github.heroslender.herochat.ui.pages.settings.ChatSettingsPage
import com.github.heroslender.herochat.ui.pages.usersettings.UserSettingsPage
import com.github.heroslender.herochat.utils.EmptyFuture
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.concurrent.CompletableFuture

class ChatCommand(private val userService: UserService) :
    AbstractAsyncCommand("chat", "Opens the chat customization UI") {
    private val arg1 = withOptionalArg("arg1", "argument 1", ArgTypes.STRING)

    init {
        setAllowsExtraArguments(true)
        requirePermission(Permissions.COMMAND_CHAT)
    }

    override fun canGeneratePermission(): Boolean {
        return false
    }

    override fun executeAsync(ctx: CommandContext): CompletableFuture<Void> {
        val args = ctx.inputString.split(" ").drop(1) // simple arg parsing
        val user = userService.getUser(ctx.sender().uuid) ?: return EmptyFuture

        if (args.isNotEmpty() && args[0].equals("spy", true)) {
            if (!user.hasPermission(Permissions.ADMIN_SPY)) {
                user.sendMessage(MessagesConfig::spyNoPermission)
                return EmptyFuture
            }

            return CompletableFuture.runAsync {
                with(userService) {
                    user.updateSettings {
                        it.spyMode = !it.spyMode

                        val status = if (it.spyMode) "{#55FF55}enabled" else "{#FF5555}disabled"
                        user.sendMessage(MessagesConfig::spyToggle, "status" to status)
                    }
                }
            }
        }

        // Open page
        if (!ctx.isPlayer) {
            user.sendMessage(Message.raw("You are not a player!"))
            return EmptyFuture
        }

        val openUserSettings = !user.hasPermission(Permissions.ADMIN_SETTINGS)
                || (args.isNotEmpty() && args[0].equals("settings", true))

        val ref = ctx.senderAsPlayerRef() ?: return EmptyFuture
        val store = ref.store
        return CompletableFuture.runAsync({
            val player = store.getComponent(ref, Player.getComponentType()) ?: return@runAsync
            if (!openUserSettings) {
                val playerRef = store.getComponent(ref, PlayerRef.getComponentType()) ?: return@runAsync
                player.pageManager.openCustomPage(
                    ref,
                    store,
                    ChatSettingsPage(playerRef, HeroChat.instance.channelService)
                )
                return@runAsync
            }

            player.pageManager.openCustomPage(
                ref,
                store,
                UserSettingsPage(user as? PlayerUser ?: return@runAsync, HeroChat.instance.channelService)
            )

        }, store.externalData.world)
    }
}
