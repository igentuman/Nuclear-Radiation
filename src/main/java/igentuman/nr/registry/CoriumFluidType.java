package igentuman.nr.registry;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.radiation.source.Corium;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * FluidType for molten corium. Its textures and tint are used by the fluid model
 * registered in {@code NuclearRadiationClient}.
 */
public class CoriumFluidType extends FluidType {

    private static final Identifier STILL_TEXTURE =
            Identifier.fromNamespaceAndPath(NuclearRadiation.MODID, "block/fluid/molten_still");
    private static final Identifier FLOWING_TEXTURE =
            Identifier.fromNamespaceAndPath(NuclearRadiation.MODID, "block/fluid/molten_flow");

    public CoriumFluidType(Properties properties) {
        super(properties);
    }

    public Identifier getStillTexture() {
        return STILL_TEXTURE;
    }

    public Identifier getFlowingTexture() {
        return FLOWING_TEXTURE;
    }

    public int getTintColor() {
        return Corium.COLOR;
    }
}
