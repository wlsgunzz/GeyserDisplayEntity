# GeyserDisplayEntity

# About
This Geyser Extension provides Item Display support, allows players to see Nexo, CraftEngine and ItemAdder item displays
Please keep in mind i am actively trying to improve this/do more precice testing

# What's new in this fork
- Position offset support (`x-offset` / `y-offset` / `z-offset`) per mapping, rotated automatically to match the furniture's facing direction
- Fixed Bedrock players sitting in the wrong spot on armor-stand-based seats (Geyser's built-in seat positioning ignores the real Java-side seat position)
- Seat offset support (`seat-offset-x` / `seat-offset-y` / `seat-offset-z`), global default in `config.yml` and per-item override in individual mapping files
- Seat rotation support (`seat-rotation`), corrects the direction Bedrock players face while seated - same global/per-item setup as seat offset, works independently of it
- `/geyserdisplayentity reload` now actually reloads everything - re-reads your config and mappings from disk, and re-applies the fresh values to every piece of furniture already placed and visible, with no restart and no despawn/respawn flicker
- Minor internal optimization on the mount-detection check (see Performance section below) - no functional change, just cheaper to run

# How to use the new features

### Position offset
**Where it goes:** per-item only, inside a mapping's own `displayentityoptions`. No global fallback for this one.
```yaml
displayentityoptions:
  x-offset: 0.0
  y-offset: -0.5
  z-offset: 0.5
```
Rotates automatically with the item's placed direction, so the same values work correctly no matter which way that specific piece is facing.

### Seat offset
**Where it goes:** either global (`config.yml`) or per-item (a mapping's `displayentityoptions`) - per-item wins if both are set.

Global default, applies to every seat that doesn't set its own:
```yaml
general:
  seat-offset-x: 0.0
  seat-offset-y: -0.3
  seat-offset-z: 0.0
```
Per-item override, same keys, under that specific item's own `displayentityoptions`:
```yaml
displayentityoptions:
  seat-offset-x: 0.0
  seat-offset-y: -0.5
  seat-offset-z: 0.0
```
Applied flat (not rotation-aware, unlike position offset). Has no effect at all unless at least one of the three is actually set somewhere, global or per-item.

Furniture matching is proximity-based - when a player sits down, the plugin looks for the closest piece of *their own* placed furniture within 1 block to figure out which mapping's seat-offset to use. This has held up fine in testing so far, including two seats placed in the same block and multiple players sitting simultaneously, but it's worth knowing the mechanism isn't a guaranteed exact link - it's "whichever furniture is closest," not a real ID-based match (Geyser/Nexo don't expose one).

### Seat rotation
**Where it goes:** exactly the same as seat offset - global `config.yml` default, or per-item override, per-item wins.
```yaml
general:
  seat-rotation: 0.0
```
```yaml
displayentityoptions:
  seat-rotation: 90.0
```
Value is in degrees. Completely independent of seat-offset - set rotation alone with no position change, position alone with no rotation change, or both together. Same "does nothing unless set somewhere" rule applies.

### `/geyserdisplayentity reload`
No config changes needed - this just works differently now. Edit any mapping file or `config.yml`, run the command, and the new values apply immediately to furniture that's already placed and loaded, without needing a server restart or players needing to relog/walk away and back.

# Performance
Nothing added in this fork runs continuously or per-tick - everything is purely event-driven (only runs when a player actually mounts/dismounts a seat, or an admin runs `/reload`). The armor-stand detection check on mount was also changed to compare cached fields directly instead of building a temporary string every time, since it's the one check that runs on every single mount event on the server regardless of entity type. Tested via Spark with no measurable overhead from this extension's code.

# Known limitations
- Seat matching is proximity-based (see Seat offset section above) - in theory two pieces of the same player's seat-furniture placed extremely close together could match to the wrong one, though this hasn't been observed in testing
- Multi-seat furniture (e.g. a bed with a seat on each side) currently always resolves to the same configured seat-offset/rotation regardless of which side is sat on - no left/right distinction yet

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

# Before / After
before 
<img width="1385" height="878" alt="bed1" src="https://github.com/user-attachments/assets/b161a413-32a1-48a1-9a68-16d6b07d68f0" />

after 
<img width="1322" height="812" alt="bed2" src="https://github.com/user-attachments/assets/bebd0753-04cc-44d6-9a75-4ca8ef179598" />

after
<img width="1119" height="806" alt="chair" src="https://github.com/user-attachments/assets/4935a520-c193-4ced-a427-56c42a072d6b" />

after 
<img width="472" height="614" alt="image" src="https://github.com/user-attachments/assets/1423bb06-0263-48e5-9449-394dfb4ef887" />

before
<img width="544" height="450" alt="rotation1" src="https://github.com/user-attachments/assets/963cc677-6c79-40d4-98fb-1211754b1815" />

after
https://github.com/user-attachments/assets/7d4d0025-2c8c-428d-9597-2c57574e5a10

(will post more images soon)
