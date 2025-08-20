package com.reis.craftartaro.core.mixin;

import com.fruityspikes.whaleborne.server.entities.HullbackEntity;
import com.reis.craftartaro.core.entity.ia.WaterCreatureAIAvoidEntityGoal;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Guardian.class)
public abstract class GuardianEntityMixin {

    @Inject(method = "registerGoals", at = @At("HEAD"))
    private void addFearHullbackGoal(CallbackInfo ci) {
        PathfinderMob mob = (PathfinderMob) (Object) this;

        mob.goalSelector.addGoal(0, new WaterCreatureAIAvoidEntityGoal<>(
                mob,
                HullbackEntity.class,
                32.0F,
                1.0D,
                1.2D
        ));
    }
}