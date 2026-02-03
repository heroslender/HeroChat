package com.github.heroslender.herochat.dependencies

import com.github.heroslender.herochat.HeroChat
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.user.User
import java.util.*

object LuckPermsDependency {
    val IsLuckpermsEnabled: Boolean
        get() = HeroChat.instance.isLuckpermsEnabled

    fun getUser(uuid: UUID): User? =
        if (IsLuckpermsEnabled) LuckPermsProvider.get().userManager.getUser(uuid) else null

    val User.prefix: String?
        get() = this.cachedData.metaData.prefix
    val User.suffix: String?
        get() = this.cachedData.metaData.suffix
}