package com.reis.craftartaro.core.entity.ia;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

import javax.annotation.Nullable;

public class WaterCreatureAIAvoidEntityGoal<T extends LivingEntity> extends Goal {

    private final PathfinderMob creature;
    private final Class<T> classToAvoid;
    private final float avoidDistance;
    private final double walkSpeedModifier;
    private final double sprintSpeedModifier;

    @Nullable
    private T entityToAvoid;
    private final TargetingConditions targetingConditions;

    public WaterCreatureAIAvoidEntityGoal(PathfinderMob creature, Class<T> classToAvoid, float avoidDistance, double walkSpeedModifier, double sprintSpeedModifier) {
        this.creature = creature;
        this.classToAvoid = classToAvoid;
        this.avoidDistance = avoidDistance;
        this.walkSpeedModifier = walkSpeedModifier;
        this.sprintSpeedModifier = sprintSpeedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));

        this.targetingConditions = TargetingConditions.forCombat().range(avoidDistance).selector((entity) ->
                entity.isAlive() && this.creature.getSensing().hasLineOfSight(entity)
        );
    }

    @Override
    public boolean canUse() {
        if (!this.creature.isInWater()) {
            return false;
        }

        this.entityToAvoid = this.creature.level().getNearestEntity(
                this.creature.level().getEntitiesOfClass(this.classToAvoid, this.creature.getBoundingBox().inflate(this.avoidDistance), (e) -> true),
                this.targetingConditions,
                this.creature,
                this.creature.getX(),
                this.creature.getY(),
                this.creature.getZ()
        );

        return this.entityToAvoid != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.entityToAvoid != null && !this.creature.getNavigation().isDone() && this.creature.distanceToSqr(this.entityToAvoid) < (this.avoidDistance * this.avoidDistance);
    }

    @Override
    public void start() {
        Vec3 targetPos = findWaterPosAway();

        if (targetPos != null) {
            this.creature.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, this.walkSpeedModifier);
        }
    }

    @Nullable
    private Vec3 findWaterPosAway() {
        for (int i = 0; i < 10; i++) {
            Vec3 randomPos = DefaultRandomPos.getPosAway(this.creature, 16, 7, this.entityToAvoid.position());

            if (randomPos == null) {
                continue;
            }

            BlockPos.MutableBlockPos targetBlockPos = new BlockPos.MutableBlockPos(randomPos.x, randomPos.y, randomPos.z);

            while (targetBlockPos.getY() > this.creature.level().getMinBuildHeight() && !this.creature.level().getFluidState(targetBlockPos).is(FluidTags.WATER)) {
                targetBlockPos.move(0, -1, 0);
            }

            if (this.creature.level().getFluidState(targetBlockPos).is(FluidTags.WATER)) {
                if (this.entityToAvoid.distanceToSqr(targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ()) > this.entityToAvoid.distanceToSqr(this.creature)) {
                    return Vec3.atBottomCenterOf(targetBlockPos);
                }
            }
        }

        return null;
    }

    @Override
    public void stop() {
        this.entityToAvoid = null;
        this.creature.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.entityToAvoid == null) {
            return;
        }

        double speed = this.creature.distanceToSqr(this.entityToAvoid) < 49.0D ? this.sprintSpeedModifier : this.walkSpeedModifier;
        this.creature.getNavigation().setSpeedModifier(speed);

        if (this.creature.getNavigation().isDone()) {
            this.start();
        }
    }
}