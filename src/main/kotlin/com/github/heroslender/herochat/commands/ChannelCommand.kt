package com.github.heroslender.herochat.commands

import com.github.heroslender.herochat.chat.Channel
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.CommandUtil
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import javax.annotation.Nonnull


class ChannelCommand(val channel: Channel) :
    AbstractPlayerCommand(channel.id, "Sends a chat message in a specific channel") {
    private val msgArg: RequiredArg<String> = withRequiredArg("msg", "message", ArgTypes.STRING)

    init {
        setAllowsExtraArguments(true)
    }

    override fun canGeneratePermission(): Boolean {
        return false
    }

    override fun execute(
        @Nonnull context: CommandContext,
        @Nonnull store: Store<EntityStore?>,
        @Nonnull ref: Ref<EntityStore?>,
        @Nonnull playerRef: PlayerRef,
        @Nonnull world: World
    ) {
        val rawArgs = CommandUtil.stripCommandName(context.inputString).trim { it <= ' ' }
        channel.sendMessage(playerRef, rawArgs)
    }
}
