package com.github.heroslender.herochat.data

import java.util.UUID

data class UserSettings(
    val uuid: UUID,
    var focusedChannelId: String? = null,
    var focusedPrivateTarget: UUID? = null,
    var messageColor: String? = null,
    val disabledChannels: MutableSet<String> = mutableSetOf(),
    var spyMode: Boolean = false
)