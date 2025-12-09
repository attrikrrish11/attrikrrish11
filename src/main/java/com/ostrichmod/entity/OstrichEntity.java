package com.ostrichmod.entity;

import com.ostrichmod.OstrichEntityMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class OstrichEntity extends Animal {
    private static final EntityDataAccessor<Boolean> PANICKING = SynchedEntityData.defineId(OstrichEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CORNERED = SynchedEntityData.defineId(OstrichEntity.class, EntityDataSerializers.BOOLEAN);
    
    private int eggLayTime;
    private static final int EGG_LAY_DELAY = 6000; // 5 minutes with 20 ticks per second
    
    public OstrichEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.eggLayTime = this.random.nextInt(EGG_LAY_DELAY);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PANICKING, false);
        this.entityData.define(CORNERED, false);
    }

    public boolean isPanicking() {
        return this.entityData.get(PANICKING);
    }

    public void setPanicking(boolean panicking) {
        this.entityData.set(PANICKING, panicking);
    }

    public boolean isCornered() {
        return this.entityData.get(CORNERED);
    }

    public void setCornered(boolean cornered) {
        this.entityData.set(CORNERED, cornered);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        
        // Panic goal (high priority)
        this.goalSelector.addGoal(1, new OstrichPanicGoal(this, 2.0));
        
        // Breeding and temping
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.BEETROOT_SEEDS), false));
        
        // Movement and attack when cornered
        this.goalSelector.addGoal(4, new OstrichCorneredAttackGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8) {
            @Override
            public boolean canUse() {
                return !OstrichEntity.this.isPanicking() && super.canUse();
            }
        });
        
        // Passive goals
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        
        // Target selector for panic triggers
        this.targetSelector.addGoal(1, new OstrichPanicTargetGoal(this));
    }

    public static AttributeSupplier.Builder createOstrichAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        
        // Check for panic
        if (!this.level().isClientSide && this.tickCount % 10 == 0) {
            checkForPanic();
        }
        
        // Handle egg laying
        if (!this.level().isClientSide && this.isAlive() && this.age % 100 == 0) {
            handleEggLaying();
        }
    }

    private void checkForPanic() {
        if (this.isBaby() || this.getHealth() <= 0) return;
        
        // Check for dangerous entities nearby
        double panicRange = 8.0;
        List<Entity> dangerousEntities = this.level().getEntities(
            this,
            this.getBoundingBox().inflate(panicRange),
            entity -> {
                if (entity instanceof Monster monster) {
                    return true;
                }
                if (entity instanceof Player player) {
                    return player.isSprinting();
                }
                return false;
            }
        );
        
        boolean shouldPanic = !dangerousEntities.isEmpty();
        this.setPanicking(shouldPanic);
        
        // Check if cornered
        if (shouldPanic) {
            setCornered(isCornered());
        }
        
        // Apply sprint speed when panicking
        if (shouldPanic) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.45); // Sprint speed
        } else {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.25); // Normal speed
        }
    }

    private boolean isCornered() {
        if (this.getLastHurtByMob() == null) return false;
        
        // Check if there are 4+ blocks within 3 blocks in all horizontal directions
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                
                BlockPos checkPos = this.blockPosition().offset(x, 0, z);
                if (this.level().isEmptyBlock(checkPos) || this.level().getBlockState(checkPos).getMaterial().isReplaceable()) {
                    return false; // Found an escape route
                }
            }
        }
        
        return true; // No escape routes found
    }

    private void handleEggLaying() {
        if (this.isBaby() || this.getHealth() <= 0) return;
        
        this.eggLayTime++;
        if (this.eggLayTime >= EGG_LAY_DELAY) {
            this.eggLayTime = this.random.nextInt(6000); // Reset with variation
            
            // Drop egg item
            if (!this.level().isClientSide) {
                this.spawnAtLocation(Items.EGG);
            }
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT_SEEDS) || 
               stack.is(Items.MELON_SEEDS) || 
               stack.is(Items.BEETROOT_SEEDS);
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return ModEntities.OSTRICH.create(serverLevel);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.eggLayTime = compoundTag.getInt("EggLayTime");
        this.setPanicking(compoundTag.getBoolean("Panicking"));
        this.setCornered(compoundTag.getBoolean("Cornered"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("EggLayTime", this.eggLayTime);
        compoundTag.putBoolean("Panicking", this.isPanicking());
        compoundTag.putBoolean("Cornered", this.isCornered());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CHICKEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.CHICKEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CHICKEN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.COW_STEP, 0.15F, 1.0F);
    }

    // Custom panic goal for ostrich
    public static class OstrichPanicGoal extends PanicGoal {
        private final OstrichEntity ostrich;

        public OstrichPanicGoal(OstrichEntity ostrich, double speedModifier) {
            super(ostrich, speedModifier);
            this.ostrich = ostrich;
        }

        @Override
        public boolean canUse() {
            return this.ostrich.isPanicking() && super.canUse();
        }

        @Override
        public void stop() {
            super.stop();
            this.ostrich.setPanicking(false);
        }
    }

    // Custom goal to attack when cornered
    public static class OstrichCorneredAttackGoal extends MeleeAttackGoal {
        private final OstrichEntity ostrich;

        public OstrichCorneredAttackGoal(OstrichEntity ostrich) {
            super(ostrich, 1.0, true);
            this.ostrich = ostrich;
        }

        @Override
        public boolean canUse() {
            return this.ostrich.isCornered() && super.canUse();
        }

        @Override
        protected double getAttackReachSqr(LivingEntity attackTarget) {
            return 2.0; // Short range kick
        }
    }

    // Target goal for panic behaviors
    public static class OstrichPanicTargetGoal extends Goal {
        private final OstrichEntity ostrich;
        private LivingEntity toAvoid;

        public OstrichPanicTargetGoal(OstrichEntity ostrich) {
            this.ostrich = ostrich;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!this.ostrich.isPanicking()) return false;
            
            // Find nearest dangerous entity
            this.toAvoid = getNearestDangerousEntity();
            return this.toAvoid != null;
        }

        @Override
        public void start() {
            if (this.toAvoid != null) {
                this.ostrich.setLastHurtByMob(this.toAvoid);
            }
        }

        private LivingEntity getNearestDangerousEntity() {
            return this.ostrich.level().getNearestEntity(
                Monster.class,
                TargetingConditions.forCombat().range(8.0),
                this.ostrich,
                this.ostrich.getX(),
                this.ostrich.getY(),
                this.ostrich.getZ()
            );
        }
    }

    public static boolean checkOstrichSpawnRules(EntityType<? extends Animal> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).isSolidRender(level, pos.below()) && 
               level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    public float getScale() {
        return this.isBaby() ? 0.5F : 1.0F;
    }
}