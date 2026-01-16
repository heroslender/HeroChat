## HeroChat

A simple and customizable chat plugin for Hytale servers.

#### Config

You can create as many chat components as you want and need.

```json
{
  "ChatFormat": "{prefix}{admin_prefix} {#ff5555}{player_username}{#555555}{bold}> {#AAAAAA}{message}",
  "Components": {
    "prefix": {
      "Text": "{#00ffff}{bold}[HeroChatt]"
    },
    "admin_prefix": {
      "Text": "{#00AAAA}[Admin]",
      "Permission": "tag.admin"
    }
  }
}
```

#### Available placeholders

  - `{player_username}`
  - `{message}`

#### Formatting placeholders

  - `{#ffffff}` - Hex color
  - `{bold}`
  - `{italic}`
  - `{monospaced}`

#### Screenshots

<img width="299" height="129" alt="{3EB12BE9-F0FE-4044-A973-B300883AD9BB}" src="https://github.com/user-attachments/assets/c5cac4a4-d4f3-403d-ad55-434a81e3168d" />
