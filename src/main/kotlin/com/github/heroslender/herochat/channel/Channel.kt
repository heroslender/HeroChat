package com.github.heroslender.herochat.channel

import com.github.heroslender.herochat.data.User

interface Channel {
    val id: String
    val name: String
    val commands: Array<String>
    val permission: String?

    fun sendMessage(sender: User, msg: String)
}