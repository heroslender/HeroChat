package com.github.heroslender.herochat.commands

import com.github.heroslender.herochat.HeroChat
import com.github.heroslender.herochat.config.MessagesConfig
import com.github.heroslender.herochat.ui.settings.ChatSettingsPage
import com.github.heroslender.herochat.utils.sendMessage
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.AbstractCommand
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.concurrent.CompletableFuture

class ChatCommand : AbstractCommand("chat", "Opens the chat customization UI") {
    private val arg1 = withOptionalArg("arg1", "argument 1", ArgTypes.STRING)

    init {
        setAllowsExtraArguments(true)
        requirePermission("herochat.commands.chat")
    }

    override fun canGeneratePermission(): Boolean {
        return false
    }

    override fun execute(ctx: CommandContext): CompletableFuture<Void?>? {
        return CompletableFuture.runAsync {
            val args = ctx.inputString.split(" ").drop(1) // simple arg parsing
            val sender = ctx.sender()
            if (args.isNotEmpty() && args[0].equals("spy", true)) {
                if (!sender.hasPermission("herochat.admin.spy")) {
                    sender.sendMessage(MessagesConfig::spyNoPermission)
                    return@runAsync
                }

                HeroChat.instance.userService.updateSettings(sender.uuid) {
                    it.spyMode = !it.spyMode

                    val status = if (it.spyMode) "{#55FF55}enabled" else "{#FF5555}disabled"
                    sender.sendMessage(MessagesConfig::spyToggle, "status" to status)
                }
                return@runAsync
            }

            // Open page
            if (!ctx.isPlayer) {
                sender.sendMessage(Message.raw("You are not a player!"))
                return@runAsync
            }

            val ref = ctx.senderAsPlayerRef() ?: return@runAsync
            val store = ref.store
            val player = store.getComponent(ref, Player.getComponentType()) ?: return@runAsync
            val playerRef = store.getComponent(ref, PlayerRef.getComponentType()) ?: return@runAsync
            player.pageManager.openCustomPage(ref, store, ChatSettingsPage(playerRef, HeroChat.instance.channelManager))
        }
    }
}
