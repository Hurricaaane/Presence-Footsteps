package eu.ha3.presencefootsteps.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.hit.HitResult;

@Mixin(DebugHud.class)
public abstract class MDebugHud extends DrawableHelper {

    @Shadow
    private HitResult blockHit;

    @Shadow
    private HitResult fluidHit;

    @Inject(method = "getRightText", at = @At("RETURN"))
    protected void onGetRightText(CallbackInfoReturnable<List<String>> info) {
        PresenceFootsteps.getInstance().getDebugHud().render(blockHit, fluidHit, info.getReturnValue());
    }
}
