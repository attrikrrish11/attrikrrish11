package com.ostrichmod;

import com.ostrichmod.entity.OstrichEntity;
import com.ostrichmod.registry.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OstrichEntityMod implements ModInitializer {
    public static final String MOD_ID = "ostrichmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Optional ostrich items
    public static final Item OSTRICH_FEATHER = new Item(new Item.Properties());
    public static final Item OSTRICH_EGG_ITEM = new Item(new Item.Properties());
    
    // Spawn egg item
    public static final Item OSTRICH_SPAWN_EGG = new SpawnEggItem(
        ModEntities.OSTRICH, 
        0x8B4513, // Brown color
        0xD2691E, // Light brown color  
        new Item.Properties()
    );

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Ostrich Entity Mod");
        
        // Register entities
        ModEntities.init();
        
        // Register entity attributes
        FabricDefaultAttributeRegistry.register(
            ModEntities.OSTRICH,
            OstrichEntity.createOstrichAttributes()
        );
        
        // Register spawn egg
        Registry.register(
            net.minecraft.core.registries.BuiltInRegistries.ITEM,
            ResourceLocation.tryBuild(MOD_ID, "ostrich_spawn_egg"),
            OSTRICH_SPAWN_EGG
        );
        
        // Register optional items
        Registry.register(
            net.minecraft.core.registries.BuiltInRegistries.ITEM,
            ResourceLocation.tryBuild(MOD_ID, "ostrich_feather"),
            OSTRICH_FEATHER
        );
        
        Registry.register(
            net.minecraft.core.registries.BuiltInRegistries.ITEM,
            ResourceLocation.tryBuild(MOD_ID, "ostrich_egg"),
            OSTRICH_EGG_ITEM
        );
        
        LOGGER.info("Ostrich Entity Mod loaded successfully!");
    }
}