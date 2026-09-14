# GeyserDisplayEntity

# About
This Geyser Extension provides Item Display support, allows players to see Nexo, CraftEngine and ItemAdder item displays.

This fork adds x/y/z position offset support (rotation-aware, so it works correctly regardless of which way furniture is facing) and a fix for the armor stand seat offset issue that caused Bedrock players to sit in the wrong spot on custom seats.

# FAQ
### How to use this extension?
You can use a converter to help you generate all of the required mappings and resources you need from [kafal](https://kafal.pogmc.net) or [campfire](https://builtbybit.com/resources/campfire-tools-converter-java2bedrock.108820/) (automatic mappings coming soon for campfire)
### What's different from the original?
- `x-offset` / `y-offset` / `z-offset` per item mapping options, rotated to match the furniture's facing
- `seat-offset-x` / `seat-offset-y` / `seat-offset-z` global config option, fixes Bedrock players sitting in the wrong position on armor-stand-based seats
### Where can I contact you?
You can contact us on our Discord: https://discord.gg/NNNaUdAbpP

# Credits
- Most of the code comes from [Kastle's branch](https://github.com/Kas-tle/Geyser/tree/feature/display-entities)
- Original extension by [GeyserExtensionists](https://github.com/GeyserExtensionists/GeyserDisplayEntity)
- Honestly this was made with help from Claude
# License
GeyserDisplayEntity is licensed under the [AGPL - 3.0 license](https://github.com/MxSGames/mc-Widgets-plugin/blob/main/LICENSE)
