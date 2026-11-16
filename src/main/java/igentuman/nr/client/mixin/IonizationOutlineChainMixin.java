package igentuman.nr.client.mixin;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.GpuTextureView;
import igentuman.nr.client.IonizationGlowRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Runs bloom only when a radioactive dropped item actually reached the visible render states. */
@Mixin(LevelRenderer.class)
public abstract class IonizationOutlineChainMixin {
    @Shadow @Final private LevelRenderState levelRenderState;

    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostChain;addToFrame(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;IILnet/minecraft/client/renderer/PostChain$TargetBundle;)V", ordinal = 0))
    private void nr$ionizationChain(PostChain vanilla, FrameGraphBuilder frame, int width, int height,
                                    PostChain.TargetBundle targets) {
        IonizationGlowRenderer.renderOutlines(vanilla, frame, width, height, targets, levelRenderState);
    }

    @Redirect(method = "doEntityOutline", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitAndBlendToTexture(Lcom/mojang/blaze3d/textures/GpuTextureView;)V"))
    private void nr$compositeGlow(RenderTarget source, GpuTextureView output) {
        IonizationGlowRenderer.blitOutlines(source, output);
    }

    @Inject(method = {"onResourceManagerReload", "close"}, at = @At("HEAD"))
    private void nr$releaseGlow(CallbackInfo ci) {
        IonizationGlowRenderer.onResourceReload();
    }
}
