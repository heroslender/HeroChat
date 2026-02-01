package com.github.heroslender.herochat.commands

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.ui.settings.ChatSettingsPage
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.concurrent.CompletableFuture

class ChatCommand : AbstractAsyncCommand("chat", "Opens the chat customization UI") {
    private val arg1 = withOptionalArg("arg1", "argument 1", ArgTypes.STRING)

    init {
        setAllowsExtraArguments(true)
        requirePermission("herochat.commands.chat")
    }

    override fun canGeneratePermission(): Boolean {
        return false
    }

    override fun executeAsync(ctx: CommandContext): CompletableFuture<Void> {
        val args = ctx.inputString.split(" ").drop(1) // simple arg parsing
        val sender = ctx.sender()
        if (args.isNotEmpty() && args[0].equals("spy", true)) {
            if (!sender.hasPermission("herochat.admin.spy")) {
                sender.sendMessage(MessagesConfig::spyNoPermission)
                return CompletableFuture.completedFuture(null)
            }

            return CompletableFuture.runAsync {
                HeroChat.instance.userService.updateSettings(sender.uuid) {
                    it.spyMode = !it.spyMode

                    val status = if (it.spyMode) "{#55FF55}enabled" else "{#FF5555}disabled"
                    sender.sendMessage(MessagesConfig::spyToggle, "status" to status)
                }
            }
        }

        // Open page
        if (!ctx.isPlayer) {
            sender.sendMessage(Message.raw("You are not a player!"))
            return CompletableFuture.completedFuture(null)
        }

        val ref = ctx.senderAsPlayerRef() ?: return CompletableFuture.completedFuture<Void>(null)
        val store = ref.store
        return CompletableFuture.runAsync({
            val player = store.getComponent(ref, Player.getComponentType()) ?: return@runAsync
            val playerRef = store.getComponent(ref, PlayerRef.getComponentType()) ?: return@runAsync
            player.pageManager.openCustomPage(
                ref,
                store,
                ChatSettingsPage(playerRef, HeroChat.instance.channelManager)
            )
        }, store.externalData.world)
    }
}
