package com.ostrichmod.registry;

import com.ostrichmod.entity.OstrichEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;

public class ModEntities {
    public static final EntityType<OstrichEntity> OSTRICH = register(
        "ostrich",
        FabricEntityTypeBuilder.create(MobCategory.CREATURE, OstrichEntity::new)
            .dimensions(EntityDimensions.scalable(1.0F, 2.5F))
            .trackRangeChunks(8)
            .spawnableFarFromPlayer()
            .build()
    );

    private static <T extends EntityType<?>> T register(String name, T entityType) {
        return Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.tryBuild("ostrichmod", name),
            entityType
        );
    }

    public static void init() {
        // Register spawn placements
        SpawnPlacements.register(
            OSTRICH,
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            OstrichEntity::checkOstrichSpawnRules
        );
        
        // Add spawn to biomes
        BiomeModifications.addSpawn(
            BiomeSelectors.includeByKey(Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA),
            MobCategory.CREATURE,
            OSTRICH,
            10,
            1,
            3
        );
        
        BiomeModifications.addSpawn(
            BiomeSelectors.includeByKey(Biomes.DESERT),
            MobCategory.CREATURE,
            OSTRICH,
            8,
            1,
            2
        );
        
        BiomeModifications.addSpawn(
            BiomeSelectors.includeByKey(Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS),
            MobCategory.CREATURE,
            OSTRICH,
            6,
            1,
            2
        );
    }
}