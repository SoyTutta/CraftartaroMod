package com.reis.craftartaro.core.mixin;

import com.fruityspikes.whaleborne.server.entities.HullbackEntity;
import com.reis.craftartaro.core.entity.ia.UniversalAvoidEntityGoal;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Drowned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Drowned.class)
public abstract class DrownedEntityMixin {

    @Inject(method = "addBehaviourGoals", at = @At("TAIL"))
    private void addFearHullbackGoal(CallbackInfo ci) {
        PathfinderMob mob = (PathfinderMob) (Object) this;

        mob.goalSelector.addGoal(0, new UniversalAvoidEntityGoal<>(
                mob,
                HullbackEntity.class,
                48.0F,
                1.0D,
                1.2D
        ));
    }
}