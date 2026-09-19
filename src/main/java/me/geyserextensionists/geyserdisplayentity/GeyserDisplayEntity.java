package me.geyserextensionists.geyserdisplayentity;

import me.geyserextensionists.geyserdisplayentity.entity.BlockDisplayEntity;
import me.geyserextensionists.geyserdisplayentity.entity.ItemDisplayEntity;
import me.geyserextensionists.geyserdisplayentity.entity.SlotDisplayEntity;
import me.geyserextensionists.geyserdisplayentity.managers.ConfigManager;
import me.geyserextensionists.geyserdisplayentity.util.EntityUtils;
import me.geyserextensionists.geyserdisplayentity.util.FileConfiguration;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.command.CommandSource;
import org.geysermc.geyser.api.entity.data.GeyserEntityDataTypes;
import org.geysermc.geyser.api.entity.property.GeyserEntityProperty;
import org.geysermc.geyser.api.entity.type.GeyserEntity;
import org.geysermc.geyser.api.event.java.ServerUpdateEntityPassengersEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineEntitiesEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineEntityPropertiesEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.util.Identifier;
import org.geysermc.geyser.entity.*;
import org.geysermc.geyser.entity.type.Entity;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataTypes;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;
import org.cloudburstmc.math.vector.Vector3f;

import java.util.Collection;

public class GeyserDisplayEntity implements Extension {

    private static GeyserDisplayEntity extension;

    private ConfigManager configManager;

    private static BedrockEntityDefinition ITEM_DISPLAY_BEDROCK;
    private static BedrockEntityDefinition BLOCK_DISPLAY_BEDROCK;

    private static VanillaEntityType<ItemDisplayEntity> ITEM_DISPLAY;
    private static VanillaEntityType<BlockDisplayEntity> BLOCK_DISPLAY;

    public static final Integer MAX_VALUE = 1000000;
    public static final Integer MIN_VALUE = -1000000;

    @Subscribe
    public void onLoad(GeyserPreInitializeEvent event) {
        extension = this;
        loadManagers();
    }

    @Subscribe
    public void onDefineEntities(GeyserDefineEntitiesEvent event) {
        //TODO loop through entity type hashmap

        ITEM_DISPLAY_BEDROCK = EntityUtils.findOrRegisterCustomDefinition(this, event, Identifier.of("geyser:item_display"));
        BLOCK_DISPLAY_BEDROCK = EntityUtils.findOrRegisterCustomDefinition(this, event, Identifier.of("geyser:block_display"));
    }

    @Subscribe
    public void onEntityPropertiesEvent(GeyserDefineEntityPropertiesEvent event) {
        try {
            registerDisplayProperties(event, Identifier.of("geyser:item_display"));
            registerDisplayProperties(event, Identifier.of("geyser:block_display"));

            EntityTypeBase<Entity> entityBase = EntityTypeDefinition.baseBuilder(Entity.class)
                    .addTranslator(MetadataTypes.BYTE, Entity::setFlags)
                    .addTranslator(MetadataTypes.INT, Entity::setAir) // Air/bubbles
                    .addTranslator(MetadataTypes.OPTIONAL_COMPONENT, Entity::setCustomName)
                    .addTranslator(MetadataTypes.BOOLEAN, Entity::setCustomNameVisible)
                    .addTranslator(MetadataTypes.BOOLEAN, Entity::setSilent)
                    .addTranslator(MetadataTypes.BOOLEAN, Entity::setGravity)
                    .addTranslator(MetadataTypes.POSE, (entity, entityMetadata) -> entity.setPose(entityMetadata.getValue()))
                    .addTranslator(MetadataTypes.INT, Entity::setFreezing)
                    .build();

            EntityTypeBase<SlotDisplayEntity> slotDisplayBase = EntityTypeBase.baseInherited(SlotDisplayEntity.class, entityBase)
                    .addTranslator(null) // Interpolation start ticks
                    .addTranslator(null) // Interpolation duration ID
                    .addTranslator(null) // Position/Rotation interpolation duration
                    .addTranslator(MetadataTypes.VECTOR3, SlotDisplayEntity::setTranslation) // Translation
                    .addTranslator(MetadataTypes.VECTOR3, SlotDisplayEntity::setScale) // Scale
                    .addTranslator(MetadataTypes.QUATERNION, SlotDisplayEntity::setLeftRotation) // Left rotation
                    .addTranslator(MetadataTypes.QUATERNION, SlotDisplayEntity::setRightRotation) // Right rotation
                    .addTranslator(null) // Billboard render constraints
                    .addTranslator(null) // Brightness override
                    .addTranslator(null) // View range
                    .addTranslator(null) // Shadow radius
                    .addTranslator(null) // Shadow strength
                    .addTranslator(null) // Width
                    .addTranslator(null) // Height
                    .addTranslator(null) // Glow color override
                    .build();

            BLOCK_DISPLAY = VanillaEntityType.inherited(BlockDisplayEntity::new, slotDisplayBase)
                    .type(EntityType.BLOCK_DISPLAY)
                    .height(configManager.getConfig().getFloat("general.height")).width(0.001f)
                    .bedrockDefinition(BLOCK_DISPLAY_BEDROCK)
                    .addTranslator(MetadataTypes.BLOCK_STATE, BlockDisplayEntity::setDisplayedBlockState)
                    .build();

            ITEM_DISPLAY = VanillaEntityType.inherited(ItemDisplayEntity::new, slotDisplayBase)
                    .type(EntityType.ITEM_DISPLAY)
                    .height(configManager.getConfig().getFloat("general.height")).width(0.001f)
                    .bedrockDefinition(ITEM_DISPLAY_BEDROCK)
                    .addTranslator(MetadataTypes.ITEM_STACK, ItemDisplayEntity::setDisplayedItem)
                    .addTranslator(MetadataTypes.BYTE, ItemDisplayEntity::setDisplayType)
                    .build();

            EntityUtils.replaceJavaDefinition(EntityType.BLOCK_DISPLAY, BLOCK_DISPLAY);
            EntityUtils.replaceJavaDefinition(EntityType.ITEM_DISPLAY, ITEM_DISPLAY);
        } catch (Throwable err) {
            logger().error("Error in load", err);
        }

        logger().info("Done");
    }

    @Subscribe
    public void onDefineCommand(GeyserDefineCommandsEvent event) {
        event.register(Command.builder(this)
                .name("reload")
                .source(CommandSource.class)
                .playerOnly(false)
                .description("GeyserDisplayEntity Reload Command")
                .permission("geyserdisplayentity.commands.reload")
                .executor((source, command, args) -> {
                    configManager.load();

                    // re-apply the fresh config to every currently-spawned furniture entity in
                    // place (a metadata update), instead of a full despawn/respawn - much lighter
                    // on the network for servers with a lot of furniture placed, since this
                    // doesn't send full add/remove entity packets for every single piece
                    for (ItemDisplayEntity entity : ItemDisplayEntity.ACTIVE_ENTITIES) {
                        entity.reapplyMappingConfig();
                    }

                    source.sendMessage(configManager.getLang().getString("commands.geyserdisplayentity.reload.successfully-reloaded"));
                })
                .build());
    }

    // geyser's armor stand mount offset ignores the real java seat position, fixed here.
    // per-item wins over global. only matches furniture on the same player's connection,
    // since ACTIVE_ENTITIES is shared across everyone on the proxy
    @Subscribe
    public void onPassengerMount(ServerUpdateEntityPassengersEvent.Mount event) {
        GeyserEntity vehicle = event.vehicle();
        if (vehicle.definition() == null) return;
        // path()/vanilla() avoid the string allocation toString() would do - this runs on
        // every mount on the server, of any entity type, so it's worth the cheaper check
        Identifier vehicleId = vehicle.definition().identifier();
        if (!vehicleId.vanilla() || !"armor_stand".equals(vehicleId.path())) return;

        FileConfiguration generalConfig = configManager.getConfig().getConfigurationSection("general");

        Vector3f vehiclePos = vehicle.position();
        ItemDisplayEntity nearest = null;
        if (vehiclePos != null) {
            float nearestDist = 1.0f; // ignore anything further than this, likely unrelated
            for (ItemDisplayEntity candidate : ItemDisplayEntity.ACTIVE_ENTITIES) {
                if (candidate.getSession() != event.connection()) continue;
                Vector3f candidatePos = candidate.getPosition();
                if (candidatePos == null) continue;
                float dist = candidatePos.distance(vehiclePos);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = candidate;
                }
            }
        }

        FileConfiguration itemConfig = nearest != null ? nearest.getMappingConfig() : null;

        boolean hasPerItemSeatOffset = itemConfig != null && (itemConfig.contains("seat-offset-x") || itemConfig.contains("seat-offset-y") || itemConfig.contains("seat-offset-z"));
        boolean hasGlobalSeatOffset = generalConfig != null && (generalConfig.contains("seat-offset-x") || generalConfig.contains("seat-offset-y") || generalConfig.contains("seat-offset-z"));
        boolean hasPerItemSeatRotation = itemConfig != null && itemConfig.contains("seat-rotation");
        boolean hasGlobalSeatRotation = generalConfig != null && generalConfig.contains("seat-rotation");
        if (!hasPerItemSeatOffset && !hasGlobalSeatOffset && !hasPerItemSeatRotation && !hasGlobalSeatRotation) return;

        GeyserEntity passenger = event.addedPassenger();

        if (hasPerItemSeatOffset || hasGlobalSeatOffset) {
            float seatX = readSeatOffsetComponent(itemConfig, generalConfig, "seat-offset-x");
            float seatY = readSeatOffsetComponent(itemConfig, generalConfig, "seat-offset-y");
            float seatZ = readSeatOffsetComponent(itemConfig, generalConfig, "seat-offset-z");
            passenger.override(GeyserEntityDataTypes.SEAT_OFFSET, Vector3f.from(seatX, seatY, seatZ));
        }

        // corrects the direction the rider faces while seated, independent of position offset
        if (hasPerItemSeatRotation || hasGlobalSeatRotation) {
            float rotationDegrees = readSeatOffsetComponent(itemConfig, generalConfig, "seat-rotation");
            // corrects for the furniture's placed yaw so one configured value works at any
            // facing. confirmed against real yaw readings at all 4 cardinal directions -
            // north/south use the base correction, east/west need an extra 180 flip on top
            if (nearest != null) {
                float yaw = nearest.getYaw();
                float contribution = -(yaw + 180f);
                if (Math.abs(Math.abs(yaw) - 90f) < 45f) {
                    contribution += 180f;
                }
                rotationDegrees += contribution;
            }
            passenger.override(GeyserEntityDataTypes.ROTATE_RIDER_DEGREES, rotationDegrees);
            passenger.override(GeyserEntityDataTypes.SEAT_HAS_ROTATION, true);

            // seat-rotation-locked: true actually locks the rider's rotation (same mechanism
            // as boats/happy ghasts). false/unset just sets a starting angle they can still
            // look away from - useful for custom mount setups
            boolean rotationLocked = itemConfig != null && itemConfig.contains("seat-rotation-locked")
                    ? itemConfig.getBoolean("seat-rotation-locked")
                    : generalConfig != null && generalConfig.getBoolean("seat-rotation-locked");
            if (rotationLocked) {
                passenger.override(GeyserEntityDataTypes.ROTATION_LOCKED_TO_VEHICLE, true);
            }
        }
    }

    // null clears the override entirely - a zero vector would just be a different override
    // that persists into the next mount instead of resetting to geyser's own default
    @Subscribe
    public void onPassengerDismount(ServerUpdateEntityPassengersEvent.Dismount event) {
        GeyserEntity vehicle = event.vehicle();
        if (vehicle.definition() == null) return;
        Identifier vehicleId = vehicle.definition().identifier();
        if (!vehicleId.vanilla() || !"armor_stand".equals(vehicleId.path())) return;

        GeyserEntity passenger = event.removedPassenger();
        passenger.override(GeyserEntityDataTypes.SEAT_OFFSET, null);
        passenger.override(GeyserEntityDataTypes.ROTATE_RIDER_DEGREES, null);
        passenger.override(GeyserEntityDataTypes.SEAT_HAS_ROTATION, null);
        passenger.override(GeyserEntityDataTypes.ROTATION_LOCKED_TO_VEHICLE, null);
    }

    // per-item value wins if set; otherwise falls back to global config default; otherwise 0
    private static float readSeatOffsetComponent(FileConfiguration perItem, FileConfiguration global, String key) {
        if (perItem != null && perItem.contains(key)) return (float) perItem.getDouble(key);
        if (global != null && global.contains(key)) return (float) global.getDouble(key);
        return 0f;
    }

    private void registerDisplayProperties(GeyserDefineEntityPropertiesEvent event, Identifier entityIdentifier) {
        Collection<GeyserEntityProperty<?>> existing = event.properties(entityIdentifier);

        FileConfiguration entityConfig = configManager.getEntityTypesCache().get(entityIdentifier);
        for (Object entityKey : entityConfig.getConfigurationSection("properties").getRootNode().childrenMap().keySet()) {
            String entityString = entityKey.toString();
            FileConfiguration propertyConfig = entityConfig.getConfigurationSection("properties." + entityString);

            String propertyType = propertyConfig.getString("property-type");

            if (propertyType.equals("integer")) {
                EntityUtils.registerInteger(event, existing, entityIdentifier, propertyConfig.getString("id"), propertyConfig.getInt("min-value"), propertyConfig.getInt("max-value"), propertyConfig.getInt("default-value"));
            } else if (propertyType.equals("float")) {
                EntityUtils.registerFloat(event, existing, entityIdentifier, propertyConfig.getString("id"), propertyConfig.getInt("min-value"), propertyConfig.getInt("max-value"), propertyConfig.getFloat("default-value"));
            }
        }
    }

    private void loadManagers() {
        this.configManager = new ConfigManager(this);
    }

    public static GeyserDisplayEntity getExtension() {
        return extension;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
