package me.twoj.client.mixin;

import me.twoj.client.modules.FakeLag;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void myclient$onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (FakeLag.shouldQueue(packet)) {
            FakeLag.queue(packet);
            ci.cancel();
        }
    }
}
