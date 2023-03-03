package com.example.mixin;

import com.example.ExampleMod;
import net.minecraft.network.play.server.SPacketChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SPacketChat.class)
public class MixinNetHandlerPlayClient {

//    @Inject(method = "handleChat", at = @At("HEAD"))
    @ModifyArg(method = "processPacket(Lnet/minecraft/network/play/INetHandlerPlayClient;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/INetHandlerPlayClient;handleChat(Lnet/minecraft/network/play/server/SPacketChat;)V"))
    public SPacketChat handleChat(SPacketChat var1) {
        return ExampleMod.handleChat(var1);
    }
}
