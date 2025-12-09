package com.ostrichmod;

import com.ostrichmod.client.renderer.OstrichModel;
import com.ostrichmod.client.renderer.OstrichRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class OstrichModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register entity renderer
        EntityRendererRegistry.register(ModEntities.OSTRICH, OstrichRenderer::new);
        
        // Register entity model layer
        EntityModelLayerRegistry.registerModelLayer(
            OstrichModel.LAYER_LOCATION, 
            OstrichModel::createBodyLayer
        );
    }
}