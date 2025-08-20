package com.reis.craftartaro.core.mixin;

import com.fruityspikes.whaleborne.server.entities.HullbackEntity;
import com.github.alexthe666.iceandfire.entity.EntitySeaSerpent;
import com.reis.craftartaro.core.entity.ia.WaterCreatureAIAvoidEntityGoal;
import net.minecraft.world.entity.PathfinderMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntitySeaSerpent.class)
public abstract class SeaSerpentEntityMixin {

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void addFearHullbackGoal(CallbackInfo ci) {
        PathfinderMob mob = (PathfinderMob) (Object) this;

        mob.goalSelector.addGoal(0, new WaterCreatureAIAvoidEntityGoal<>(
                mob,
                HullbackEntity.class,
                48.0F,
                1.2D,
                1.4D
        ));
        mob.goalSelector.addGoal(1, new WaterCreatureAIAvoidEntityGoal<>(
                mob,
                HullbackEntity.class,
                64.0F,
                1.4D,
                1.8D
        ));
    }
}