package com.github.heroslender.herochat.channel

import com.hypixel.hytale.server.core.command.system.CommandSender

interface Channel {
    val id: String
    val name: String
    val commands: Array<String>
    val permission: String?

    fun sendMessage(sender: CommandSender, msg: String)
}