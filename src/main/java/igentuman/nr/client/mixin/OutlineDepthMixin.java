package igentuman.nr.client.mixin;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Keep outline geometry depth so the ionization effect can reject hidden pixels. */
@Mixin(RenderPipelines.class)
public abstract class OutlineDepthMixin {
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderPipelines;register(Lcom/mojang/blaze3d/pipeline/RenderPipeline;)Lcom/mojang/blaze3d/pipeline/RenderPipeline;"))
    private static RenderPipeline nr$writeOutlineDepth(RenderPipeline pipeline) {
        String path = pipeline.getLocation().getPath();
        if (path.equals("pipeline/outline_cull") || path.equals("pipeline/outline_no_cull")) {
            return pipeline.toBuilder().withDepthStencilState(DepthStencilState.DEFAULT).build();
        }
        return pipeline;
    }
}
