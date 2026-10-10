package igentuman.nr.client.mixin;

import igentuman.nr.client.IonizationGlowRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

/** Selects the ionization post chain for frames containing radioactive dropped items. */
@Mixin(LevelRenderer.class)
public abstract class IonizationOutlineChainMixin {
    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/Identifier;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;"))
    private PostChain nr$ionizationChain(ShaderManager shaderManager, Identifier id, Set<Identifier> targets) {
        return IonizationGlowRenderer.chainForWorldOutlines(shaderManager.getPostChain(id, targets));
    }
}
