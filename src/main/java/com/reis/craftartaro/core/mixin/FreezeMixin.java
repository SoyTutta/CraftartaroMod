package com.reis.craftartaro.core.mixin;

import com.obscuria.aquamirae.Aquamirae;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Aquamirae.class, remap = false)
public class FreezeMixin {
    @Inject(method = "onPlayerTick", at = @At("HEAD"), cancellable = true)
    private void cancelFreeze(TickEvent.PlayerTickEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}