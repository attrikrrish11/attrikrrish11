package com.ostrichmod.client.renderer;

import com.ostrichmod.OstrichEntityMod;
import com.ostrichmod.entity.OstrichEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class OstrichModel extends HierarchicalModel<OstrichEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.tryBuild("ostrichmod", "ostrich"), "main"
    );
    
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart tail;

    public OstrichModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.leftLeg = this.body.getChild("left_leg");
        this.rightLeg = this.body.getChild("right_leg");
        this.tail = this.body.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Body (main torso)
        PartDefinition body = partDefinition.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-6.0F, -16.0F, -4.0F, 12.0F, 16.0F, 8.0F), 
                PartPose.offset(0.0F, 16.0F, 0.0F));

        // Neck (long and curved)
        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(0, 24)
                .addBox(-2.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, -16.0F, 0.0F));

        // Head (small with beak)
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(32, 0)
                .addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, -12.0F, 0.0F));

        // Add beak
        head.addOrReplaceChild("beak", CubeListBuilder.create()
                .texOffs(32, 12)
                .addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 3.0F),
                PartPose.ZERO);

        // Left leg (long and thin)
        PartDefinition leftLeg = body.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(48, 0)
                .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offset(-4.0F, 0.0F, 0.0F));

        // Left foot
        leftLeg.addOrReplaceChild("left_foot", CubeListBuilder.create()
                .texOffs(56, 18)
                .addBox(-2.0F, 0.0F, -3.0F, 4.0F, 1.0F, 5.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));

        // Right leg
        PartDefinition rightLeg = body.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(48, 0)
                .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offset(4.0F, 0.0F, 0.0F));

        // Right foot
        rightLeg.addOrReplaceChild("right_foot", CubeListBuilder.create()
                .texOffs(56, 18)
                .addBox(-2.0F, 0.0F, -3.0F, 4.0F, 1.0F, 5.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));

        // Tail (small feathered)
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 40)
                .addBox(-3.0F, -2.0F, 0.0F, 6.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, -8.0F, 4.0F));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(OstrichEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float speed = entity.isPanicking() ? 3.0f : 1.0f;
        float degree = 1.0f;

        // Animate legs when walking/running
        this.leftLeg.xRot = Mth.cos(limbAngle * speed * 0.6662F) * 1.4F * limbDistance * degree;
        this.rightLeg.xRot = Mth.cos(limbAngle * speed * 0.6662F + (float)Math.PI) * 1.4F * limbDistance * degree;

        // Animate body bobbing
        this.body.y = Mth.cos(limbAngle * speed * 0.6662F) * 2.0F * limbDistance * degree;

        // Head/neck animation
        this.neck.xRot = headPitch * ((float)Math.PI / 180F);
        this.neck.yRot = headYaw * ((float)Math.PI / 180F);

        // Tail animation
        this.tail.yRot = Mth.cos(limbAngle * speed * 0.3331F) * 0.2F * limbDistance;

        // Panic animation (more frantic movement)
        if (entity.isPanicking()) {
            this.head.xRot += this.random.nextFloat() * 0.3F - 0.15F;
            this.neck.yRot += this.random.nextFloat() * 0.6F - 0.3F;
            this.tail.yRot += this.random.nextFloat() * 0.4F - 0.2F;
        }
    }
}