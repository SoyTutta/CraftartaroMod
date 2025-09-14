package com.reis.craftartaro.core.mixin;

import com.fruityspikes.whaleborne.server.entities.HullbackEntity;
import com.github.alexthe666.iceandfire.entity.EntitySeaSerpent;
import com.reis.craftartaro.core.entity.ia.UniversalAvoidEntityGoal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import com.google.common.base.Predicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntitySeaSerpent.class)
public abstract class SeaSerpentEntityMixin {

    @Accessor("NOT_SEA_SERPENT")
    public static Predicate<Entity> getNotSeaSerpentPredicate() {
        throw new AbstractMethodError();
    }

    @Accessor("NOT_SEA_SERPENT")
    @Mutable
    public static void setNotSeaSerpentPredicate(Predicate<Entity> value) {
        throw new AbstractMethodError();
    }

    @Accessor("NOT_SEA_SERPENT_IN_WATER")
    public static Predicate<Entity> getNotSeaSerpentInWaterPredicate() {
        throw new AbstractMethodError();
    }

    @Accessor("NOT_SEA_SERPENT_IN_WATER")
    @Mutable
    public static void setNotSeaSerpentInWaterPredicate(Predicate<Entity> value) {
        throw new AbstractMethodError();
    }


    @Inject(method = "registerGoals", at = @At("HEAD"))
    private void addFearHullbackGoal(CallbackInfo ci) {
        PathfinderMob mob = (PathfinderMob) (Object) this;

        mob.goalSelector.addGoal(0, new UniversalAvoidEntityGoal<>(
                mob,
                HullbackEntity.class,
                64.0F,
                1.4D,
                1.8D,
                true
        ));
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void modifyStaticPredicates(CallbackInfo ci) {
        Predicate<Entity> originalNotSeaSerpent = getNotSeaSerpentPredicate();
        setNotSeaSerpentPredicate(entity -> originalNotSeaSerpent.apply(entity) && !(entity instanceof HullbackEntity));

        Predicate<Entity> originalNotSeaSerpentInWater = getNotSeaSerpentInWaterPredicate();
        setNotSeaSerpentInWaterPredicate(entity -> originalNotSeaSerpentInWater.apply(entity) && !(entity instanceof HullbackEntity));
    }
}