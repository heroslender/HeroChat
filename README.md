## HeroChat

A simple and customizable chat plugin for Hytale servers.

#### Screenshots

<img width="299" height="129" alt="{3EB12BE9-F0FE-4044-A973-B300883AD9BB}" src="https://github.com/user-attachments/assets/c5cac4a4-d4f3-403d-ad55-434a81e3168d" />

![Discord_5k0eSX7xdW](https://github.com/user-attachments/assets/90adba8d-4dd7-486f-a9b0-597244329d0d)

#### Config

You can create as many chat components as you want and need.

##### Global config

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

##### Channel config

```json
{
  "Name": "Local",
  "Format": "{#FFFF55}[L] {player_username}{#555555}{bold}> {#AAAAAA}{message}",
  "Permission": "chat.local", // Optional
  "Distance": 60, // Optional
  "CrossWorld": false // Optional
}
```

#### Available placeholders

- `{message}`
- `{player_username}`
- `{luckperms_prefix}`
- `{luckperms_suffix}`

#### Formatting placeholders

- `{#ffffff}` - Hex color
- `{bold}`
- `{italic}`
- `{monospaced}`

#### Plugin Compatibility

##### LuckPerms

To use this plugin with LuckPerms you must disable chat formatting on the LuckPerms config:

```yaml
chat-formatter:
  enabled: false  # <== Change this to false
  message-format: "<prefix><username><suffix>: <message>"
```