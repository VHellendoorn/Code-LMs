package fr.arthurbambou.fdlink.mixin_1_16;

import fr.arthurbambou.fdlink.FDLink1_16;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    /**
     * This method handles message from the death of tamed entity, team chat, various commands and everything
     * broadcastChatMessage will processes
     * @param text
     * @param uUID
     * @param ci
     */
    @Inject(at = @At("HEAD"), method = "sendSystemMessage")
    public void sendMessage(Text text, UUID uUID, CallbackInfo ci) {
        FDLink1_16.handleText(text, uUID);
    }
}
