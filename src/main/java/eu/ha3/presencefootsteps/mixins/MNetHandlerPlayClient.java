package eu.ha3.presencefootsteps.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import eu.ha3.presencefootsteps.main.PFHaddon;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketSoundEffect;

@Mixin(NetHandlerPlayClient.class)
public abstract class MNetHandlerPlayClient implements INetHandlerPlayClient {
	
	//NetHandlerPlayClient
	/*public void handleSoundEffect(SPacketSoundEffect packetIn) {*/
	@Inject(method = "handleSoundEffect(Lnet/minecraft/network/play/server/SPacketSoundEffect;)V", at = @At("HEAD"), cancellable = true)
	public void onHandleSoundEffect(SPacketSoundEffect packet, CallbackInfo info) {
		if (PFHaddon.INSTANCE.onSoundRecieved(packet.getSound(), packet.getCategory())) {
			info.cancel();
			/*return;*/
		}
	}
    /*  PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        this.gameController.theWorld.playSound(this.gameController.thePlayer, packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getSound(), packetIn.getCategory(), packetIn.getVolume(), packetIn.getPitch());
    }*/
}
