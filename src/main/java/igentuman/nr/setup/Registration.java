package igentuman.nr.setup;

import com.mojang.serialization.Codec;
import igentuman.nr.effect.RadiationResistance;
import igentuman.nr.setup.registration.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nr.setup.registration.Registries.*;

public class Registration {

    public static final RegistryObject<MobEffect> RADIATION_RESISTANCE = EFFECTS.register("radiation_resistance", () -> new RadiationResistance(MobEffectCategory.BENEFICIAL, 0xd4ffFF));

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Registries.init();
        NCBlocks.init();
        NCItems.init();
        NCFluids.init();
        WorldGeneration.register(bus);
        NCEnergyBlocks.init();
        CreativeTabs.init();
        NcParticleTypes.init();
        NCSounds.init();
    }



    private static <S extends Structure> StructureType<S> typeConvert(Codec<S> codec) {
        return () -> codec;
    }
}
