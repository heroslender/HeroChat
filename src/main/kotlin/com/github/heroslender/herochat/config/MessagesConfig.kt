package com.github.heroslender.herochat.config

import com.github.heroslender.herochat.utils.appendString
import com.hypixel.hytale.codec.builder.BuilderCodec

class MessagesConfig {
    var channelNoPermission: String = "{#FF5555}You do not have permission to send messages in this channel."
    var channelNotFound: String = "{#FF5555}Channel not found."
    var channelJoined: String = "{#55FF55}You are now talking in {#FFFFFF}{channel}{#55FF55}."
    var channelDisabled: String = "{#FF5555}You have disabled this channel. Enable it again to be able to talk here."
    var chatNoRecipients: String = "{#FF5555}No one hears you."
    var chatCooldown: String = "{#FF5555}Please wait before sending another message."
    var chatSpamWarning: String = "{#FF5555}Please do not spam."
    var chatCapslockWarning: String = "{#FF5555}Please do not abuse capslock!"
    var privateChatStarted: String = "{#55FF55}You are now in a private conversation with {#FFFFFF}{target}{#55FF55}."
    var privateChatPlayerNotFound: String = "{#FF5555}The player is not online."
    var privateChatSelf: String = "{#FF5555}You cannot start a private conversation with yourself."
    var privateChatNotActive: String = "{#FF5555}You are not in a private conversation."
    var spyNoPermission: String = "{#FF5555}You do not have permission to use chat spy."
    var spyToggle: String = "{#CCCCCC}Chat spy has been {status}{#CCCCCC}."
    var nicknameNoPermission: String = "{#FF5555}You do not have permission to change your nickname."
    var nicknameTooLong: String = "{#FF5555}Nickname is too long."
    var nicknameSet: String = "{#55FF55}Your nickname has been set to {#FFFFFF}{nickname}{#55FF55}."
    var nicknameReset: String = "{#55FF55}Your nickname has been reset."
    var menuSuccessNotificationTitle: String = "Settings Saved"
    var menuSuccessNotificationDescription: String = "Your settings were successfully saved!"
    var menuFocusedChannel: String = "Focused Channel"
    var menuMutedChannels: String = "Muted Channels"
    var menuMessageColor: String = "Message Color"
    var menuSpyMode: String = "Spy Mode"
    var menuSaveButton: String = "Save"
    var menuCancelButton: String = "Cancel"

    companion object {
        @JvmField
        val CODEC: BuilderCodec<MessagesConfig> = BuilderCodec.builder(
            MessagesConfig::class.java,
            ::MessagesConfig
        )
            .appendString(MessagesConfig::channelNoPermission)
            .appendString(MessagesConfig::channelNotFound)
            .appendString(MessagesConfig::channelJoined)
            .appendString(MessagesConfig::channelDisabled)
            .appendString(MessagesConfig::chatNoRecipients)
            .appendString(MessagesConfig::chatCooldown)
            .appendString(MessagesConfig::chatSpamWarning)
            .appendString(MessagesConfig::chatCapslockWarning)
            .appendString(MessagesConfig::privateChatStarted)
            .appendString(MessagesConfig::privateChatPlayerNotFound)
            .appendString(MessagesConfig::privateChatSelf)
            .appendString(MessagesConfig::privateChatNotActive)
            .appendString(MessagesConfig::spyNoPermission)
            .appendString(MessagesConfig::spyToggle)
            .appendString(MessagesConfig::nicknameNoPermission)
            .appendString(MessagesConfig::nicknameTooLong)
            .appendString(MessagesConfig::nicknameSet)
            .appendString(MessagesConfig::nicknameReset)
            .appendString(MessagesConfig::menuFocusedChannel)
            .appendString(MessagesConfig::menuMutedChannels)
            .appendString(MessagesConfig::menuMessageColor)
            .appendString(MessagesConfig::menuSpyMode)
            .appendString(MessagesConfig::menuSaveButton)
            .appendString(MessagesConfig::menuCancelButton)
            .appendString(MessagesConfig::menuSuccessNotificationTitle)
            .appendString(MessagesConfig::menuSuccessNotificationDescription)
            .build()
    }
}