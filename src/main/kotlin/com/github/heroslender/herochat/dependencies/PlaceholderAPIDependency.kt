package com.github.heroslender.herochat.dependencies

import at.helpch.placeholderapi.PlaceholderAPIPlugin
import com.github.heroslender.herochat.HeroChat
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.*

object PlaceholderAPIDependency {
    val IsPlaceholderApiEnabled: Boolean
        get() = HeroChat.instance.isPlaceholderApiEnabled

    @JvmStatic
    fun parsePlaceholder(playerRef: PlayerRef, placeholder: String): String? {
        if (!IsPlaceholderApiEnabled) {
            return null
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
}