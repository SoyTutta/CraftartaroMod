package com.reis.craftartaro.core.mixin;

import com.fruityspikes.whaleborne.server.entities.HullbackEntity;
import com.github.L_Ender.cataclysm.entity.Deepling.Coral_Golem_Entity;
import com.reis.craftartaro.core.entity.ia.UniversalAvoidEntityGoal;
import net.minecraft.world.entity.PathfinderMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Coral_Golem_Entity.class)
public abstract class CoralGolemEntityMixin {

    @Inject(method = "registerGoals", at = @At("HEAD"))
    private void addFearHullbackGoal(CallbackInfo ci) {
        PathfinderMob mob = (PathfinderMob) (Object) this;

        mob.goalSelector.addGoal(1, new UniversalAvoidEntityGoal<>(
                mob,
                HullbackEntity.class,
                32.0F,
                1.0D,
                1.2D
        ));
    }
}