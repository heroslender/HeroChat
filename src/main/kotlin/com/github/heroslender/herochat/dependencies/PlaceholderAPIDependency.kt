package com.github.heroslender.herochat.dependencies

import at.helpch.placeholderapi.PlaceholderAPIPlugin
import com.github.heroslender.herochat.HeroChat
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.*

object PlaceholderAPIDependency {
    val IsPlaceholderApiEnabled: Boolean
        get() = HeroChat.instance.isPlaceholderApiEnabled


    fun parsePlaceholder(playerRef: PlayerRef?, placeholder: String): String? {
        if (playerRef == null) {
            return null
        }

        if (!IsPlaceholderApiEnabled) {
            return parsePlaceholderSimple(playerRef, placeholder)
        }

        val indexOf = placeholder.indexOf('_')
        if (indexOf == -1) {
            return null
        }

        val identifier = placeholder.substring(0, indexOf).lowercase(Locale.ROOT)
        val manager = PlaceholderAPIPlugin.instance().localExpansionManager()
        val expansion = manager.getExpansion(identifier) ?: return null
        return expansion.onPlaceholderRequest(playerRef, placeholder.substring(indexOf + 1))
    }

    private fun parsePlaceholderSimple(playerRef: PlayerRef, placeholder: String): String? {
        return when (placeholder) {
            "player_username" -> playerRef.username
            else -> null
        }
    }
}