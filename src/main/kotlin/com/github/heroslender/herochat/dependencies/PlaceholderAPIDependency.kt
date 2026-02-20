package com.github.heroslender.herochat.dependencies

import at.helpch.placeholderapi.PlaceholderAPIPlugin
import com.hypixel.hytale.common.plugin.PluginIdentifier
import com.hypixel.hytale.common.semver.SemverRange
import com.hypixel.hytale.server.core.plugin.PluginManager
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.*

object PlaceholderAPIDependency {
    val PlaceholderApiId = PluginIdentifier("HelpChat", "PlaceholderAPI")
    val IsPlaceholderApiEnabled: Boolean =
        PluginManager.get().hasPlugin(PlaceholderApiId, SemverRange.fromString(">= 1.0.2"))

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