package com.github.heroslender.herochat.event

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent
import com.hypixel.hytale.server.core.universe.PlayerRef

class TestPlayerChatEvent(
    sender: PlayerRef,
    recipients: MutableList<PlayerRef>,
    message: String,
): PlayerChatEvent(sender, recipients, message)
