<img src=".github/assets/icon.png" alt="HeroChat Icon" title="HeroChat" align="right"/>

## HeroChat

[![GitHub stars](https://img.shields.io/github/stars/heroslender/HeroChat.svg)](https://github.com/heroslender/HeroChat/stargazers)
[![GitHub issues](https://img.shields.io/github/issues-raw/heroslender/HeroChat.svg?label=issues)](https://github.com/heroslender/HeroChat/issues)
[![GitHub last commit](https://img.shields.io/github/last-commit/heroslender/HeroChat)](https://github.com/heroslender/HeroChat/commit)
[![GitHub last commit](https://img.shields.io/github/downloads/heroslender/HeroChat/total)](https://github.com/heroslender/HeroChat/releases)
[![GitHub Tag](https://img.shields.io/github/v/tag/heroslender/HeroChat?label=Version)](https://github.com/heroslender/HeroChat/releases/latest)
[![Discord](https://img.shields.io/discord/455337556746305538?label=Discord&logo=Discord&logoColor=white&labelColor=7289da&color=555555)](https://discord.gg/nWX7apX)

A simple and customizable chat plugin for Hytale servers.

<!-- TOC -->
  * [HeroChat](#herochat)
    * [Commands](#commands)
    * [Permissions](#permissions)
    * [Config](#config)
      * [Global config](#global-config)
      * [Private channel config](#private-channel-config)
      * [Channel config](#channel-config)
      * [Messages Config](#messages-config)
    * [Available placeholders](#available-placeholders)
    * [Formatting placeholders](#formatting-placeholders)
    * [Plugin Compatibility](#plugin-compatibility)
      * [LuckPerms](#luckperms)
    * [Screenshots](#screenshots)
<!-- TOC -->

### Commands

- `/chat` - Opens the chat customization menu;
- `/chat settings` - Opens the user settings menu;
- `/chat spy` - Toggle chat spy;
- `/tell <player>` - Change the default chat to this private channel;
- `/tell <player> <message>` - Sends a private message;
- `/<channel-id>` - Change the default chat to this channel;
- `/<channel-id> <message>` - Sends a message in a specific channel

### Permissions

- `herochat.commands.chat` - Permission for player to use the `/chat` command
- `herochat.chat.mute-channels` - Permission for player to mute channels
- `herochat.chat.message-color` - Permission for player to change the default message color on chat
- `herochat.chat.message.colors` - Permission for player to use colors on chat
- `herochat.chat.message.formatting` - Permission for player to use formatting tags on chat
- `herochat.bypass.cooldown` - Permission to bypass the cooldown checks
- `herochat.bypass.spam` - Permission to bypass the spam checks
- `herochat.bypass.capslock` - Permission to bypass the capslock spam checks
- `herochat.admin.spy` - Permission to enable chat spy
- `herochat.admin.settings` - Permission to open the admin settings menu

### Config

You can create as many chat components as you want and need. You can also create components available only on a
specific channel. A channel ID is its file name.

#### Global config

```json
{
  "DefaultChat": "global",
  "Components": {
    "prefix": {
      "Text": "{#00ffff}{bold}[HeroChatt]"
    },
    "admin_prefix": {
      "Text": "{#00AAAA}[Admin]",
      "Permission": "tag.admin" // Optional
    }
  }
}
```

#### Private channel config

```json
{
  "Name": "Whisper",
  "Commands": [
    "tell",
    "w",
    "whisper"
  ],
  "SenderFormat": "Message to {target_username}{#555555}{bold}> {#aaa}{message}",
  "ReceiverFormat": "Message from {player_username}{#555555}{bold}> {#AAAAAA}{message}",
  "Permission": "chat.tell", // Optional
  "CapslockFilter": {
    "enabled": true,
    "percentage": 50,
    "minLength": 5
  },
  "Cooldowns": {
    "chat.member": 1000, // Cooldown in miliseconds
    "chat.vip": 300,
    "chat.admin": 0
  },
  "Components": {}
}
```

#### Channel config

```json
{
  "Name": "Local",
  "Commands": [
    "g",
    "global"
  ],
  "Format": "{#FFFF55}[L] {player_username}{#555555}{bold}> {#AAAAAA}{message}",
  "Permission": "chat.local", // Optional
  "Distance": 60, // Optional
  "CrossWorld": false, // Optional
  "CapslockFilter": {}, // Optional - defaults to disabled
  "Cooldowns": {},
  "Components": {}
}
```

#### Messages Config

```json
{
  "ChannelNoPermission": "{#FF5555}You do not have permission to send messages in this channel.",
  "ChannelNotFound": "{#FF5555}Channel not found.",
  "ChannelJoined": "{#55FF55}You are now talking in {#FFFFFF}{channel}{#55FF55}.",
  "ChannelDisabled": "{#FF5555}You have disabled this channel. Enable it again to be able to talk here.",
  "ChatNoRecipients": "{#FF5555}No one hears you.",
  "ChatSpamWarning": "{#FF5555}Please do not spam.",
  "ChatCooldown": "{#FF5555}Please wait before sending another message.",
  "ChatCapslockWarning": "{#FF5555}Please do not abuse capslock.",
  "PrivateChatStarted": "{#55FF55}You are now in a private conversation with {#FFFFFF}{player}{#55FF55}.",
  "PrivateChatPlayerNotFound": "{#FF5555}Player not found.",
  "PrivateChatSelf": "{#FF5555}You cannot start a private conversation with yourself.",
  "PrivateChatNotActive": "{#FF5555}You are not in a private conversation.",
  "SpyNoPermission": "{#FF5555}You do not have permission to use chat spy.",
  "SpyToggle": "{#CCCCCC}Chat spy has been {status}{#CCCCCC}.",
  "MenuFocusedChannel": "Focused Channel",
  "MenuMutedChannels": "Muted Channels",
  "MenuMessageColor": "Message Color",
  "MenuSpyMode": "Spy Mode",
  "MenuSaveButton": "Save",
  "MenuCancelButton": "Close",
  "MenuSuccessNotificationTitle": "Settings Saved",
  "MenuSuccessNotificationDescription": "Your settings were successfully saved!"
}
```

### Available placeholders

If you are using PlaceholderAPI in your server you can use all of [its placeholders](https://wiki.placeholderapi.com/users/placeholder-list/hytale/),
but using the format `{placeholder}`.

Default placeholders available:

- `{message}` - The message to be sent by the player
- `{player_username}` - The player username

### Formatting placeholders

- `{#ffffff}` - Hex color
- `{bold}`
- `{italic}`
- `{monospaced}`

### Plugin Compatibility

#### LuckPerms

To use this plugin with LuckPerms you must disable chat formatting on the LuckPerms config:

```yaml
chat-formatter:
  enabled: false  # <== Change this to false
  message-format: "<prefix><username><suffix>: <message>"
```

### Screenshots

<img width="299" height="129" alt="{3EB12BE9-F0FE-4044-A973-B300883AD9BB}" src="https://github.com/user-attachments/assets/c5cac4a4-d4f3-403d-ad55-434a81e3168d" />

![User Settings Menu](https://github.com/user-attachments/assets/a8091df1-1e74-43a7-b259-9ae1bf5e9549)
![Discord_5k0eSX7xdW](https://github.com/user-attachments/assets/90adba8d-4dd7-486f-a9b0-597244329d0d)
![Settings Menu](https://github.com/user-attachments/assets/b9408714-9dcd-42ff-a274-3897e2315f02)
![Private Chat Settings Menu](https://github.com/user-attachments/assets/691cb48a-ac7c-4aef-bf68-9d3df3b3bb2f)
![Chat Settings Menu](https://github.com/user-attachments/assets/d4724448-63e5-42d5-ab2d-548cfc4ec996)
![Add component Menu](https://github.com/user-attachments/assets/92f0cf1a-7bcf-4fc4-a5e3-cb4d20a0c015)

