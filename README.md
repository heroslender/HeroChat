## HeroChat

A simple and customizable chat plugin for Hytale servers.

![Discord_5k0eSX7xdW](https://github.com/user-attachments/assets/90adba8d-4dd7-486f-a9b0-597244329d0d)

<!-- TOC -->
  * [HeroChat](#herochat)
    * [Commands](#commands)
    * [Config](#config)
      * [Global config](#global-config)
      * [Channel config](#channel-config)
    * [Available placeholders](#available-placeholders)
    * [Formatting placeholders](#formatting-placeholders)
    * [Plugin Compatibility](#plugin-compatibility)
      * [LuckPerms](#luckperms)
    * [Screenshots](#screenshots)
<!-- TOC -->

### Commands

- `/chat` - Opens the chat customization menu;
- `/tell <player> <message>` - Sends a private message;
- `/<channel-id> <message>` - Sends a message in a specific channel

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
  "SenderFormat": "Message to {target_username}{#555555}{bold}> {#aaa}{message}",
  "ReceiverFormat": "Message from {player_username}{#555555}{bold}> {#AAAAAA}{message}",
  "Permission": "chat.tell", // Optional
  "Components": {}
}
```

#### Channel config

```json
{
  "Name": "Local",
  "Format": "{#FFFF55}[L] {player_username}{#555555}{bold}> {#AAAAAA}{message}",
  "Permission": "chat.local", // Optional
  "Distance": 60, // Optional
  "CrossWorld": false, // Optional
  "Components": {}
}
```

### Available placeholders

- `{message}`
- `{player_username}`
- `{luckperms_prefix}`
- `{luckperms_suffix}`

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

![Discord_5k0eSX7xdW](https://github.com/user-attachments/assets/90adba8d-4dd7-486f-a9b0-597244329d0d)