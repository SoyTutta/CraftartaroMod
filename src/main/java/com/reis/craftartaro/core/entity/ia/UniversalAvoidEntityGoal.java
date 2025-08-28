package com.reis.craftartaro.core.entity.ia;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class UniversalAvoidEntityGoal<T extends LivingEntity> extends Goal {
    protected final PathfinderMob mob;
    private final double walkSpeedModifier;
    private final double sprintSpeedModifier;
    @Nullable protected T toAvoid;
    protected final float maxDist;
    @Nullable protected Path path;
    protected final Class<T> avoidClass;
    protected final Predicate<LivingEntity> avoidPredicate;
    protected final Predicate<LivingEntity> predicateOnAvoidEntity;
    private final TargetingConditions avoidEntityTargeting;
    private final boolean escapeOnlyInWater;

    public UniversalAvoidEntityGoal(PathfinderMob mob, Class<T> avoidClass, float maxDist, double walkSpeed, double sprintSpeed) {
        this(mob, avoidClass, (e) -> true, maxDist, walkSpeed, sprintSpeed, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test, false);
    }

    public UniversalAvoidEntityGoal(PathfinderMob mob, Class<T> avoidClass, float maxDist, double walkSpeed, double sprintSpeed, boolean escapeOnlyInWater) {
        this(mob, avoidClass, (e) -> true, maxDist, walkSpeed, sprintSpeed, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test, escapeOnlyInWater);
    }

    public UniversalAvoidEntityGoal(PathfinderMob mob, Class<T> avoidClass, Predicate<LivingEntity> extra, float maxDist, double walkSpeed, double sprintSpeed, Predicate<LivingEntity> onAvoid, boolean escapeOnlyInWater) {
        this.mob = mob;
        this.avoidClass = avoidClass;
        this.avoidPredicate = extra;
        this.maxDist = maxDist;
        this.walkSpeedModifier = walkSpeed;
        this.sprintSpeedModifier = sprintSpeed;
        this.predicateOnAvoidEntity = onAvoid;
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.avoidEntityTargeting = TargetingConditions.forCombat()
                .range(maxDist)
                .selector(onAvoid.and(extra));
        this.escapeOnlyInWater = escapeOnlyInWater;
    }

    @Override
    public boolean canUse() {
        this.toAvoid = this.mob.level().getNearestEntity(
                this.mob.level().getEntitiesOfClass(this.avoidClass,
                        this.mob.getBoundingBox().inflate(this.maxDist, 3.0D, this.maxDist), e -> true),
                this.avoidEntityTargeting, this.mob,
                this.mob.getX(), this.mob.getY(), this.mob.getZ());

        if (this.toAvoid == null) return false;

        PathNavigation nav = this.mob.getNavigation();
        final int maxTries = 10;
        double[] distanceFactors = {1.0, 0.75, 0.5, 0.25};
        double distToAvoid = this.mob.distanceTo(this.toAvoid);
        double urgencyFactor = Math.min(1.0, 1.0 - distToAvoid / this.maxDist);

        for (double factor : distanceFactors) {
            int horiz = (int)(this.maxDist * factor);

            for (int i = 0; i < maxTries; i++) {
                Vec3 dest;

                if (this.escapeOnlyInWater || this.mob.isInWaterOrBubble() || nav instanceof WaterBoundPathNavigation) {
                    dest = getWaterPosAway(this.mob, this.toAvoid.position(), horiz, 7);
                } else {
                    dest = DefaultRandomPos.getPosAway(this.mob, horiz, 7, this.toAvoid.position());
                }

                if (dest == null) continue;
                if (this.toAvoid.distanceToSqr(dest.x, dest.y, dest.z) <= this.toAvoid.distanceToSqr(this.mob)) continue;

                Path tryPath = nav.createPath(dest.x, dest.y, dest.z, 0);
                if (tryPath != null) {
                    this.path = tryPath;

                    double speed = this.walkSpeedModifier + (this.sprintSpeedModifier - this.walkSpeedModifier) * urgencyFactor;
                    nav.setSpeedModifier(speed);

                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.escapeOnlyInWater && !this.mob.isInWaterOrBubble()) {
            this.mob.getNavigation().stop();
            return false;
        }
        return !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        if (this.path != null) {
            this.mob.getNavigation().moveTo(this.path, this.walkSpeedModifier);
        }
    }

    @Override
    public void stop() {
        this.toAvoid = null;
        this.path = null;
    }

    @Override
    public void tick() {
        if (this.toAvoid == null) return;

        if (this.escapeOnlyInWater && !this.mob.isInWaterOrBubble()) {
            this.mob.getNavigation().stop();
            return;
        }

        double d = this.mob.distanceToSqr(this.toAvoid);
        this.mob.getNavigation().setSpeedModifier(d < 49.0D ? this.sprintSpeedModifier : this.walkSpeedModifier);
    }

    @Nullable
    protected static Vec3 getWaterPosAway(PathfinderMob mob, Vec3 threat, int horiz, int vert) {
        RandomSource rnd = mob.getRandom();
        Level level = mob.level();

        Vec3 away = new Vec3(mob.getX() - threat.x, 0.0D, mob.getZ() - threat.z);
        if (away.lengthSqr() < 1.0E-4) away = new Vec3(1, 0, 0); // evita NaN
        away = away.normalize();

        BlockPos origin = mob.blockPosition();

        for (int i = 0; i < 15; i++) {
            int dist = 4 + rnd.nextInt(Math.max(1, horiz - 3));
            int dx = Mth.floor(away.x * dist) + rnd.nextInt(3) - 1;
            int dz = Mth.floor(away.z * dist) + rnd.nextInt(3) - 1;
            int dy = rnd.nextInt(vert) - rnd.nextInt(vert);

            BlockPos p = origin.offset(dx, dy, dz);

            if (level.getFluidState(p).is(FluidTags.WATER)
                    && level.getBlockState(p).isPathfindable(level, p, PathComputationType.WATER)) {
                return Vec3.atBottomCenterOf(p);
            }
        }
        return null;
    }
}