package com.github.heroslender.herochat.event

import com.github.heroslender.herochat.channel.Channel
import com.github.heroslender.herochat.data.User
import com.hypixel.hytale.event.IAsyncEvent
import com.hypixel.hytale.event.ICancellable

class PreChatEvent(
    val sender: User,
    val channel: Channel,
    var message: String
): IAsyncEvent<String>, ICancellable {
    private var cancelled = false

    override fun isCancelled() = cancelled

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }
}