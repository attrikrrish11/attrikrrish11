package com.ostrichmod.client.renderer;

import com.ostrichmod.OstrichEntityMod;
import com.ostrichmod.entity.OstrichEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

@Environment(EnvType.CLIENT)
public class OstrichRenderer extends MobRenderer<OstrichEntity, OstrichModel> {
    private static final ResourceLocation OSTRICH_TEXTURE = ResourceLocation.tryBuild("ostrichmod", "textures/entity/ostrich.png");

    public OstrichRenderer(EntityRendererProvider.Context context) {
        super(context, new OstrichModel(context.bakeLayer(OstrichModel.LAYER_LOCATION)), 0.7f);
        
        // Add item layer for when ostrich is tempted/breeding
        this.addLayer(new RenderLayer<OstrichEntity, OstrichModel>(this) {
            @Override
            public void render(net.minecraft.client.renderer.MultiBufferSource bufferSource, 
                             net.minecraft.client.renderer.entity.RenderLayerParent<OstrichEntity, OstrichModel> parent, 
                             int packedLight, OstrichEntity ostrich, float limbSwing, float limbSwingAmount, 
                             float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                // Render held item when tempted
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(OstrichEntity entity) {
        return OSTRICH_TEXTURE;
    }

    @Override
    protected void scale(OstrichEntity ostrich, net.minecraft.client.renderer.PoseStack poseStack, float partialTickTime) {
        float scale = ostrich.isBaby() ? 0.5f : 1.0f;
        poseStack.scale(scale, scale, scale);
        super.scale(ostrich, poseStack, partialTickTime);
    }
}