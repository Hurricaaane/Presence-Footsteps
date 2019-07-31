package eu.ha3.presencefootsteps.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.PlaySoundS2CPacket;
import net.minecraft.network.listener.ClientPlayPacketListener;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MClientPlayNetworkHandler implements ClientPlayPacketListener {

    @Inject(method = "onPlaySound(Lnet/minecraft/client/network/packet/PlaySoundS2CPacket;)V", at = @At("HEAD"), cancellable = true)
    public void onHandleSoundEffect(PlaySoundS2CPacket packet, CallbackInfo info) {
        if (PresenceFootsteps.INSTANCE.getEngine().onSoundRecieved(packet.getSound(), packet.getCategory())) {
            info.cancel();
        }
    }
}
