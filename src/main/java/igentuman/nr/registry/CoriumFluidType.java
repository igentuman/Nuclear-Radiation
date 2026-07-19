package igentuman.nr.registry;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.radiation.source.Corium;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * FluidType for molten corium. Carries the still/flow textures and the ARGB tint
 * used by the client fluid-render extension registered in {@code NuclearRadiationClient}.
 */
public class CoriumFluidType extends FluidType {

    private static final ResourceLocation STILL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "block/fluid/molten_still");
    private static final ResourceLocation FLOWING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "block/fluid/molten_flow");

    public CoriumFluidType(Properties properties) {
        super(properties);
    }

    public ResourceLocation getStillTexture() {
        return STILL_TEXTURE;
    }

    public ResourceLocation getFlowingTexture() {
        return FLOWING_TEXTURE;
    }

    public int getTintColor() {
        return Corium.COLOR;
    }
}
