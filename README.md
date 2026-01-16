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

  - `{#ffffff}` - Hex color
  - `{bold}`
  - `{italic}`
  - `{monospaced}`