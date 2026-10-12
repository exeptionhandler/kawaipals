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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.Items;
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
    private static final String NBT_SITTING     = "IsSitting";

    private static final EntityDataAccessor<String> PAL_TYPE_SYNC =
        SynchedEntityData.defineId(PalEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Boolean> SITTING_SYNC =
        SynchedEntityData.defineId(PalEntity.class, EntityDataSerializers.BOOLEAN);

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

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PAL_TYPE_SYNC, "gotita");
        builder.define(SITTING_SYNC, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.35)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.25, 2.5f, 6.0f));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 6.0f, 1.2, 1.4, p -> ownerUUID == null && !isSitting()));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8) {
            @Override
            public boolean canUse() {
                return !PalEntity.this.isSitting() && super.canUse();
            }
        });
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
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack itemInHand = player.getItemInHand(hand);

        // Owner interactions
        if (ownerUUID != null && ownerUUID.equals(player.getUUID())) {

            // 1. Direct recall with empty PalOrb
            if (itemInHand.is(ModItems.PAL_ORB) && !PalOrbItem.isCaptured(itemInHand)) {
                PalOrbItem.setPalType(itemInHand, this.palType);
                PalOrbItem.setCaptured(itemInHand, true);
                PalOrbItem.setPalUUID(itemInHand, this.getUUID());
                itemInHand.set(DataComponents.CUSTOM_NAME, Component.literal("Orbe de " + this.palType.displayName).withStyle(this.palType.rarity.color));

                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.5, this.getZ(), 20, 0.3, 0.3, 0.3, 0.2);
                    sl.playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.5f);
                }

                player.sendSystemMessage(
                    Component.translatable("kawaipals.orb.recalled", this.palType.displayName).withStyle(ChatFormatting.GREEN)
                );
                this.discard();
                return InteractionResult.SUCCESS;
            }

            // 2. Feeding / Healing
            if (isFoodItem(itemInHand)) {
                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(4.0f);
                    if (!player.isCreative()) {
                        itemInHand.shrink(1);
                    }
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.6, this.getZ(), 6, 0.25, 0.25, 0.25, 0.05);
                        sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EAT.value(), SoundSource.NEUTRAL, 1.0f, 1.3f);
                    }
                    sendFeedback(player,
                        Component.literal("🍎 Alimentaste a " + this.palType.displayName + " (Vida: " + (int)this.getHealth() + "/" + (int)this.getMaxHealth() + ")")
                            .withStyle(ChatFormatting.GREEN)
                    );
                    return InteractionResult.SUCCESS;
                } else {
                    if (!player.isCreative()) {
                        itemInHand.shrink(1);
                    }
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.6, this.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
                        sl.playSound(null, this.blockPosition(), SoundEvents.ALLAY_AMBIENT_WITH_ITEM, SoundSource.NEUTRAL, 1.0f, 1.5f);
                    }
                    sendFeedback(player,
                        Component.literal("💖 ¡" + this.palType.displayName + " está muy feliz!").withStyle(this.palType.rarity.color)
                    );
                    return InteractionResult.SUCCESS;
                }
            }

            // 3. Petting (Sneak + Empty hand)
            if (player.isShiftKeyDown() && itemInHand.isEmpty()) {
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.6, this.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
                    sl.playSound(null, this.blockPosition(), SoundEvents.ALLAY_AMBIENT_WITH_ITEM, SoundSource.NEUTRAL, 1.0f, 1.4f);
                }
                sendFeedback(player,
                    Component.literal("💖 Acariciaste con cariño a " + this.palType.displayName + " ✨")
                        .withStyle(this.palType.rarity.color)
                );
                return InteractionResult.SUCCESS;
            }

            // 4. Pegasito mount
            if (palType == PalType.PEGASITO && !this.isVehicle() && itemInHand.isEmpty() && !player.isShiftKeyDown()) {
                if (!isSitting()) {
                    player.startRiding(this);
                    return InteractionResult.SUCCESS;
                }
            }

            // 5. Toggle Sitting / Stay mode (Empty hand, not sneaking)
            if (itemInHand.isEmpty() && !player.isShiftKeyDown()) {
                boolean newSitting = !isSitting();
                setSitting(newSitting);

                if (this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.7f, newSitting ? 0.9f : 1.4f);
                }

                if (newSitting) {
                    sendFeedback(player,
                        Component.literal("🐾 " + this.palType.displayName + " se queda esperando aquí.")
                            .withStyle(ChatFormatting.GOLD)
                    );
                } else {
                    sendFeedback(player,
                        Component.literal("🐾 " + this.palType.displayName + " te sigue de nuevo.")
                            .withStyle(ChatFormatting.GREEN)
                    );
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    private void sendFeedback(Player player, Component message) {
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(message, true);
        } else {
            player.sendSystemMessage(message);
        }
    }

    private static boolean isFoodItem(ItemStack stack) {
        return stack.is(Items.SWEET_BERRIES)
                || stack.is(Items.GLOW_BERRIES)
                || stack.is(Items.HONEY_BOTTLE)
                || stack.is(Items.COOKIE)
                || stack.is(Items.APPLE);
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
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // 1. Friendly fire immunity from owner
        if (source.getEntity() != null && ownerUUID != null && source.getEntity().getUUID().equals(ownerUUID)) {
            return false;
        }

        // 2. Immune to fall damage and suffocation
        if (source.is(DamageTypes.FALL) || source.is(DamageTypes.IN_WALL)) {
            return false;
        }

        return super.hurtServer(level, source, amount);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        ItemStack orbStack = new ItemStack(ModItems.PAL_ORB);
        PalOrbItem.setPalType(orbStack, this.palType);
        PalOrbItem.setCaptured(orbStack, true);
        orbStack.set(DataComponents.CUSTOM_NAME, Component.literal("Orbe de " + this.palType.displayName).withStyle(this.palType.rarity.color));

        // If owner is alive and nearby, return directly into owner inventory!
        if (ownerUUID != null) {
            Player owner = level.getPlayerByUUID(ownerUUID);
            if (owner != null && owner.isAlive()) {
                if (owner.getInventory().add(orbStack)) {
                    owner.sendSystemMessage(
                        Component.literal("🛡️ ¡" + this.palType.displayName + " regresó a tu inventario para protegerse!")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                    );
                    return;
                }
            }
        }

        ItemEntity drop = new ItemEntity(level, this.getX(), this.getY(), this.getZ(), orbStack);
        drop.setExtendedLifetime();
        level.addFreshEntity(drop);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString(NBT_PAL_TYPE, getPalType().id);
        output.putBoolean(NBT_SITTING, isSitting());
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
        if (parsed != null) {
            this.setPalType(parsed);
        }
        this.setSitting(input.getBooleanOr(NBT_SITTING, false));

        String ownerStr = input.getStringOr(NBT_OWNER_UUID, "");
        if (!ownerStr.isEmpty()) {
            try {
                ownerUUID = UUID.fromString(ownerStr);
            } catch (Exception ignored) {}
        }
        shieldCooldown = input.getIntOr(NBT_SHIELD_CD, 0);
        resurrectUsedToday = input.getBooleanOr(NBT_RESURRECT, false);
    }

    public boolean isSitting() {
        return this.entityData != null ? this.entityData.get(SITTING_SYNC) : false;
    }

    public void setSitting(boolean sitting) {
        if (this.entityData != null) {
            this.entityData.set(SITTING_SYNC, sitting);
        }
        if (sitting) {
            this.getNavigation().stop();
        }
    }

    public PalType getPalType() {
        if (this.entityData != null) {
            String id = this.entityData.get(PAL_TYPE_SYNC);
            PalType type = PalType.fromId(id);
            if (type != null) {
                this.palType = type;
                return type;
            }
        }
        return (this.palType != null) ? this.palType : PalType.GOTITA;
    }

    public void setPalType(PalType type) {
        this.palType = (type != null) ? type : PalType.GOTITA;
        if (this.entityData != null) {
            this.entityData.set(PAL_TYPE_SYNC, this.palType.id);
        }
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
            if (pal.ownerUUID == null || pal.isSitting()) return false;
            if (!(pal.level() instanceof ServerLevel sl)) return false;
            owner = sl.getPlayerByUUID(pal.ownerUUID);
            if (owner == null || !owner.isAlive()) return false;
            return pal.distanceToSqr(owner) > (maxDist * maxDist);
        }

        @Override
        public boolean canContinueToUse() {
            if (owner == null || !owner.isAlive() || pal.isSitting()) return false;
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
            if (owner == null) return;
            pal.getLookControl().setLookAt(owner, 10.0f, pal.getMaxHeadXRot());

            double distSqr = pal.distanceToSqr(owner);

            // Teleport if too far (> 16 blocks away) or lost
            if (distSqr > 256.0) {
                teleportToOwner();
                return;
            }

            if (--timeToRecalcPath <= 0) {
                timeToRecalcPath = 10;
                boolean pathFound = pal.getNavigation().moveTo(owner, speed);
                if (!pathFound && distSqr > 100.0) {
                    teleportToOwner();
                }
            }
        }

        private void teleportToOwner() {
            if (owner == null) return;
            Level level = pal.level();
            for (int i = 0; i < 10; i++) {
                int ox = pal.getRandom().nextIntBetweenInclusive(-2, 2);
                int oz = pal.getRandom().nextIntBetweenInclusive(-2, 2);
                int oy = pal.getRandom().nextIntBetweenInclusive(-1, 1);
                BlockPos target = owner.blockPosition().offset(ox, oy, oz);
                if (level.getBlockState(target).isAir() && level.getBlockState(target.above()).isAir()) {
                    pal.setPos(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
                    pal.getNavigation().stop();
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.PORTAL, pal.getX(), pal.getY() + 0.5, pal.getZ(), 16, 0.3, 0.3, 0.3, 0.1);
                        sl.sendParticles(ParticleTypes.END_ROD, pal.getX(), pal.getY() + 0.5, pal.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
                        sl.playSound(null, pal.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0f, 1.6f);
                    }
                    break;
                }
            }
        }
    }
}
