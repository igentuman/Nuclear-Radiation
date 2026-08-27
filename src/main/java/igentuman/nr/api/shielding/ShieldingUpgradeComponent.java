package igentuman.nr.api.shielding;

import com.mojang.serialization.Codec;
import igentuman.nr.NuclearRadiation;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ShieldingUpgradeComponent {

    public static final Codec<Double> CODEC = Codec.DOUBLE;

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(NuclearRadiation.MODID);

    public static final Supplier<DataComponentType<Double>> TYPE =
            COMPONENTS.registerComponentType("shielding_upgrade",
                    builder -> builder.persistent(CODEC).networkSynchronized(ByteBufCodecs.DOUBLE));

    private ShieldingUpgradeComponent() {}

    public static void register(IEventBus modBus) { COMPONENTS.register(modBus); }
}
