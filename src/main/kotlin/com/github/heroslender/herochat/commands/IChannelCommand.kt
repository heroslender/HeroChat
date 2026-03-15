package com.github.heroslender.herochat.commands

import com.github.heroslender.herochat.data.User

interface IChannelCommand {
    val aliases: Array<String>

    fun execute(sender: User, message: String)
}