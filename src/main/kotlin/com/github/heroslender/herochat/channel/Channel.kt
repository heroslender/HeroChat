package com.github.heroslender.herochat.channel

import com.github.heroslender.herochat.data.PlayerUser
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.event.TestPlayerChatEvent
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent
import java.util.concurrent.CompletableFuture

interface Channel {
    val id: String
    val name: String
    val commands: Array<String>
    val permission: String?
    val capslockFilter: CapslockFilter

    fun sendMessage(sender: User, msg: String)

    /**
     * Dispatch the test chat event to the server.
     *
     * This event will be used by other plugins such as moderation
     * plugins with the mute feature.
     */
    fun dispatchTestChatEvent(
        sender: User,
        recipients: MutableList<User>,
        message: String,
    ): CompletableFuture<PlayerChatEvent?> {
        return if (sender is PlayerUser) {
            HytaleServer.get()
                .eventBus
                .dispatchForAsync(PlayerChatEvent::class.java)
                .dispatch(
                    TestPlayerChatEvent(
                        sender = sender.player,
                        recipients = recipients.filterIsInstance<PlayerUser>().map { it.player }.toMutableList(),
                        message = message
                    )
                )
        } else CompletableFuture.completedFuture(null)
    }
}