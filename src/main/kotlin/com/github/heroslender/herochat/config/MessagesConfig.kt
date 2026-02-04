package com.github.heroslender.herochat.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec

class MessagesConfig {
    var channelNoPermission: String = "{#FF5555}You do not have permission to send messages in this channel."
    var channelNotFound: String = "{#FF5555}Channel not found."
    var channelJoined: String = "{#55FF55}You are now talking in {#FFFFFF}{channel}{#55FF55}."
    var channelDisabled: String = "{#FF5555}You have disabled this channel. Enable it again to be able to talk here."
    var chatNoRecipients: String = "{#FF5555}No one hears you."
    var privateChatStarted: String = "{#55FF55}You are now in a private conversation with {#FFFFFF}{target}{#55FF55}."
    var privateChatPlayerNotFound: String = "{#FF5555}The player is not online."
    var privateChatSelf: String = "{#FF5555}You cannot start a private conversation with yourself."
    var privateChatNotActive: String = "{#FF5555}You are not in a private conversation."
    var spyNoPermission: String = "{#FF5555}You do not have permission to use chat spy."
    var spyToggle: String = "{#CCCCCC}Chat spy has been {status}{#CCCCCC}."
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
            .append(
                KeyedCodec("ChannelNoPermission", Codec.STRING, false),
                { config, value -> config.channelNoPermission = value },
                { config -> config.channelNoPermission }
            ).add()
            .append(
                KeyedCodec("ChannelNotFound", Codec.STRING, false),
                { config, value -> config.channelNotFound = value },
                { config -> config.channelNotFound }
            ).add()
            .append(
                KeyedCodec("ChannelJoined", Codec.STRING, false),
                { config, value -> config.channelJoined = value },
                { config -> config.channelJoined }
            ).add()
            .append(
                KeyedCodec("ChannelDisabled", Codec.STRING, false),
                { config, value -> config.channelDisabled = value },
                { config -> config.channelDisabled }
            ).add()
            .append(
                KeyedCodec("ChatNoRecipients", Codec.STRING, false),
                { config, value -> config.chatNoRecipients = value },
                { config -> config.chatNoRecipients }
            ).add()
            .append(
                KeyedCodec("PrivateChatStarted", Codec.STRING, false),
                { config, value -> config.privateChatStarted = value },
                { config -> config.privateChatStarted }
            ).add()
            .append(
                KeyedCodec("PrivateChatPlayerNotFound", Codec.STRING, false),
                { config, value -> config.privateChatPlayerNotFound = value },
                { config -> config.privateChatPlayerNotFound }
            ).add()
            .append(
                KeyedCodec("PrivateChatSelf", Codec.STRING, false),
                { config, value -> config.privateChatSelf = value },
                { config -> config.privateChatSelf }
            ).add()
            .append(
                KeyedCodec("PrivateChatNotActive", Codec.STRING, false),
                { config, value -> config.privateChatNotActive = value },
                { config -> config.privateChatNotActive }
            ).add()
            .append(
                KeyedCodec("MenuFocusedChannel", Codec.STRING, false),
                { config, value -> config.menuFocusedChannel = value },
                { config -> config.menuFocusedChannel }
            ).add()
            .append(
                KeyedCodec("MenuMutedChannels", Codec.STRING, false),
                { config, value -> config.menuMutedChannels = value },
                { config -> config.menuMutedChannels }
            ).add()
            .append(
                KeyedCodec("MenuMessageColor", Codec.STRING, false),
                { config, value -> config.menuMessageColor = value },
                { config -> config.menuMessageColor }
            ).add()
            .append(
                KeyedCodec("MenuSpyMode", Codec.STRING, false),
                { config, value -> config.menuSpyMode = value },
                { config -> config.menuSpyMode }
            ).add()
            .append(
                KeyedCodec("MenuSaveButton", Codec.STRING, false),
                { config, value -> config.menuSaveButton = value },
                { config -> config.menuSaveButton }
            ).add()
            .append(
                KeyedCodec("MenuCancelButton", Codec.STRING, false),
                { config, value -> config.menuCancelButton = value },
                { config -> config.menuCancelButton }
            ).add()
            .append(
                KeyedCodec("MenuSuccessNotificationTitle", Codec.STRING, false),
                { config, value -> config.menuSuccessNotificationTitle = value },
                { config -> config.menuSuccessNotificationTitle }
            ).add()
            .append(
                KeyedCodec("MenuSuccessNotificationDescription", Codec.STRING, false),
                { config, value -> config.menuSuccessNotificationDescription = value },
                { config -> config.menuSuccessNotificationDescription }
            ).add()
            .build()
    }
}