package com.github.heroslender.herochat.message

import com.github.heroslender.herochat.data.PlayerUser
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.dependencies.PlaceholderAPIDependency

object PlaceholderManager {

    @JvmStatic
    fun parsePlaceholder(user: User, placeholder: String): String? {
        var result = parseSelfPlaceholders(user, placeholder)
        if (result != null) {
            return result
        }

        if (user is PlayerUser) {
            result = PlaceholderAPIDependency.parsePlaceholder(user.player, placeholder)
            if (result != null) {
                return result
            }
        }

        return null
    }

    fun parseSelfPlaceholders(user: User, placeholder: String): String? = when (placeholder) {
        "player_username" -> user.username
        "player_nickname" -> user.settings.nickname ?: user.username
        else -> null
    }
}