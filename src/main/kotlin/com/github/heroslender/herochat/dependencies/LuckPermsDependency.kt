package com.github.heroslender.herochat.dependencies

import com.github.heroslender.herochat.HeroChat
import com.hypixel.hytale.server.core.universe.PlayerRef
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.user.User

object LuckPermsDependency {
    val IsLuckpermsEnabled: Boolean
        get() = HeroChat.instance.isLuckpermsEnabled

    fun getUser(playerRef: PlayerRef): User? =
        if (IsLuckpermsEnabled) LuckPermsProvider.get().userManager.getUser(playerRef.uuid) else null

    val User.prefix: String?
        get() = this.cachedData.metaData.prefix
    val User.suffix: String?
        get() = this.cachedData.metaData.prefix
}