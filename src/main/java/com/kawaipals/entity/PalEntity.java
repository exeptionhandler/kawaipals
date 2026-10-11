package com.kawaipals.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import com.kawaipals.core.PalType;
import com.kawaipals.item.PalOrbItem;
import com.kawaipals.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

/**
 * PalEntity – the animated companion entity for all 12 Pal types.
 * Squishy kawaii spirit companion in the style of Fortnite Sprites!
 */
public class PalEntity extends PathfinderMob implements GeoEntity {

    private static final String NBT_PAL_TYPE   = "PalType";
    private static final String NBT_OWNER_UUID  = "OwnerUUID";
    private static final String NBT_SHIELD_CD   = "ShieldCooldown";
    private static final String NBT_RESURRECT   = "ResurrectUsed";

    private PalType palType = PalType.GOTITA;

    @Nullable
    private UUID ownerUUID;

    private int shieldCooldown = 0;
    private boolean resurrectUsedToday = false;
    private int buffTickCounter = 0;

    private int flightEnergy = 300;
    private static final int MAX_FLIGHT_ENERGY = 300;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.pal_entity.idle_float");

    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache(this);

    public PalEntity(EntityType<? extends PalEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.35)
            .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.2, 3.0f, 16.0f));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 6.0f, 1.2, 1.4, p -> ownerUUID == null));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) return;

        if (shieldCooldown > 0) shieldCooldown--;

        if (this.onGround() && flightEnergy < MAX_FLIGHT_ENERGY) {
            flightEnergy++;
        }

        buffTickCounter++;
        if (buffTickCounter >= 20) {
            buffTickCounter = 0;
            applyOwnerBuff();
        }
    }

    private void applyOwnerBuff() {
        if (ownerUUID == null) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Player owner = serverLevel.getPlayerByUUID(ownerUUID);
        if (owner == null || !owner.isAlive()) return;

        final int DURATION = 600;
        final boolean AMBIENT = true;
        final boolean SHOW_PARTICLES = false;

        switch (palType) {
            case GOTITA ->
                owner.addEffect(new MobEffectInstance(MobEffects.SPEED, DURATION, 0, AMBIENT, SHOW_PARTICLES));
            case BRILLIN ->
                owner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION, 0, AMBIENT, SHOW_PARTICLES));
            case CARBONITO ->
                owner.addEffect(new MobEffectInstance(MobEffects.HASTE, DURATION, 0, AMBIENT, SHOW_PARTICLES));
            case ZEPHYRA ->
                owner.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, DURATION, 0, AMBIENT, SHOW_PARTICLES));
            case FENIXITO ->
                owner.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, DURATION, 0, AMBIENT, SHOW_PARTICLES));
            case MUSGOSIN ->
                owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, DURATION, 0, AMBIENT, SHOW_PARTICLES));
            case TEMPESIN ->
                owner.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, DURATION, 1, AMBIENT, SHOW_PARTICLES));
            case LUMINARIX -> {
                owner.addEffect(new MobEffectInstance(MobEffects.STRENGTH, DURATION, 1, AMBIENT, SHOW_PARTICLES));
                owner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, DURATION, 0, AMBIENT, SHOW_PARTICLES));
            }
            case GUARDIAN_ASTRAL -> {
                if (shieldCooldown == 0) {
                    shieldCooldown = 600;
                    owner.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 3, false, true));
                }
            }
            case PEGASITO ->
                owner.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, DURATION, 1, AMBIENT, SHOW_PARTICLES));
            case CELESTIA -> {
                owner.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, DURATION, 3, AMBIENT, SHOW_PARTICLES));
                owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, DURATION, 1, AMBIENT, SHOW_PARTICLES));
            }
            case XOLOTL_REY ->
                owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, DURATION, 0, AMBIENT, SHOW_PARTICLES));
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (palType == PalType.PEGASITO
                && ownerUUID != null
                && ownerUUID.equals(player.getUUID())
                && !this.isVehicle()) {
            player.startRiding(this);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isAlive() && this.isVehicle() && this.getFirstPassenger() instanceof Player player) {
            this.setYRot(player.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(player.getXRot() * 0.5f);
            this.setRot(this.getYRot(), this.getXRot());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.yBodyRot;

            float forward = player.zza;
            float strafe = player.xxa;

            if (this.palType == PalType.PEGASITO && flightEnergy > 0) {
                Vec3 motion = this.getDeltaMovement();
                double upward = (player.getXRot() < -10.0f) ? 0.3 : -0.05;
                flightEnergy--;
                this.setDeltaMovement(motion.x, upward, motion.z);
            }

            super.travel(new Vec3(strafe, travelVector.y, forward));
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ALLAY_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.ALLAY_DEATH;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        ItemStack orbStack = new ItemStack(ModItems.PAL_ORB);
        PalOrbItem.setPalType(orbStack, this.palType);
        PalOrbItem.setCaptured(orbStack, true);

        ItemEntity drop = new ItemEntity(level, this.getX(), this.getY(), this.getZ(), orbStack);
        level.addFreshEntity(drop);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString(NBT_PAL_TYPE, palType.id);
        if (ownerUUID != null) {
            output.putString(NBT_OWNER_UUID, ownerUUID.toString());
        }
        output.putInt(NBT_SHIELD_CD, shieldCooldown);
        output.putBoolean(NBT_RESURRECT, resurrectUsedToday);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String typeId = input.getStringOr(NBT_PAL_TYPE, "gotita");
        PalType parsed = PalType.fromId(typeId);
        if (parsed != null) palType = parsed;

        String ownerStr = input.getStringOr(NBT_OWNER_UUID, "");
        if (!ownerStr.isEmpty()) {
            try {
                ownerUUID = UUID.fromString(ownerStr);
            } catch (Exception ignored) {}
        }
        shieldCooldown = input.getIntOr(NBT_SHIELD_CD, 0);
        resurrectUsedToday = input.getBooleanOr(NBT_RESURRECT, false);
    }

    public PalType getPalType() { return palType; }

    public void setPalType(PalType type) {
        this.palType = (type != null) ? type : PalType.GOTITA;
    }

    public @Nullable UUID getOwnerUUID() { return ownerUUID; }

    public void setOwnerUUID(@Nullable UUID uuid) { this.ownerUUID = uuid; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("main_controller", 5, state -> state.setAndContinue(IDLE_ANIM)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geckoCache;
    }

    private static class FollowOwnerGoal extends Goal {
        private final PalEntity pal;
        private final double speed;
        private final float minDist;
        private final float maxDist;
        private Player owner;
        private int timeToRecalcPath;

        FollowOwnerGoal(PalEntity pal, double speed, float minDist, float maxDist) {
            this.pal     = pal;
            this.speed   = speed;
            this.minDist = minDist;
            this.maxDist = maxDist;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (pal.ownerUUID == null) return false;
            if (!(pal.level() instanceof ServerLevel sl)) return false;
            owner = sl.getPlayerByUUID(pal.ownerUUID);
            if (owner == null || !owner.isAlive()) return false;
            return pal.distanceToSqr(owner) > (maxDist * maxDist);
        }

        @Override
        public boolean canContinueToUse() {
            if (owner == null || !owner.isAlive()) return false;
            return pal.distanceToSqr(owner) > (minDist * minDist);
        }

        @Override
        public void start() {
            timeToRecalcPath = 0;
        }

        @Override
        public void stop() {
            pal.getNavigation().stop();
        }

        @Override
        public void tick() {
            pal.getLookControl().setLookAt(owner, 10.0f, pal.getMaxHeadXRot());
            if (--timeToRecalcPath <= 0) {
                timeToRecalcPath = 10;
                pal.getNavigation().moveTo(owner, speed);
            }
        }
    }
}
