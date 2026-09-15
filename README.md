# GeyserDisplayEntity

# About
This Geyser Extension provides Item Display support, allows players to see Nexo, CraftEngine and ItemAdder item displays
Please keep in mind i am actively trying to improve this/do more precice testing
# What's new in this fork
- Position offset support (`x-offset` / `y-offset` / `z-offset`) per mapping, rotated automatically to match the furniture's facing direction
- Fixed Bedrock players sitting in the wrong spot on armor-stand-based seats (Geyser's built-in seat positioning ignores the real Java-side seat position)
- Seat offset support (`seat-offset-x` / `seat-offset-y` / `seat-offset-z`), global default in `config.yml` and per-item override in individual mapping files

# How to use the new features

### Position offset (currently no global fallback)
Add to any mapping's `displayentityoptions`:
```yaml
displayentityoptions:
  x-offset: 0.0
  y-offset: -0.5
  z-offset: 0.5
```
Rotates automatically with the item's placed direction, so the same values work correctly no matter which way that specific piece is facing.

### Seat offset (has global fallback)
For furniture with a seat (chairs, couches, etc). Set a global default in `config.yml`:
```yaml
general:
  seat-offset-x: 0.0
  seat-offset-y: -0.3
  seat-offset-z: 0.0
```
Or override it for a specific item, same as position offset, under that item's own `displayentityoptions`:
```yaml
displayentityoptions:
  seat-offset-x: 0.0
  seat-offset-y: -0.5
  seat-offset-z: 0.0
```
Per-item values take priority over the global default when both are set. Neither has any effect unless at least one of the three is actually set somewhere.

# FAQ
### How to use this extension?
You can use a converter to help you generate all of the required mappings and resoucres you need from [kafal](https://kafal.pogmc.net) or [campfire](https://builtbybit.com/resources/campfire-tools-converter-java2bedrock.108820/) (automatic mappings in campfire coming soon)
### Where can I contact you?
You can contact us on our Discord: https://discord.gg/NNNaUdAbpP

# Credits
- Most of the code comes from [Kastle's branch](https://github.com/Kas-tle/Geyser/tree/feature/display-entities)
- Main Geyser Extensionists developers [Main Geyser Display Plugin](https://github.com/GeyserExtensionists/GeyserDisplayEntity)
- Honestly i used [Claude](https://claude.ai) for help

# License
GeyserDisplayEntity is licensed under the [AGPL - 3.0 license](https://github.com/MxSGames/mc-Widgets-plugin/blob/main/LICENSE)

before 
<img width="1385" height="878" alt="bed1" src="https://github.com/user-attachments/assets/b161a413-32a1-48a1-9a68-16d6b07d68f0" />
after 
<img width="1322" height="812" alt="bed2" src="https://github.com/user-attachments/assets/bebd0753-04cc-44d6-9a75-4ca8ef179598" />
after
<img width="1119" height="806" alt="chair" src="https://github.com/user-attachments/assets/4935a520-c193-4ced-a427-56c42a072d6b" />
after 
<img width="472" height="614" alt="image" src="https://github.com/user-attachments/assets/1423bb06-0263-48e5-9449-394dfb4ef887" />
(will post more images soon)
