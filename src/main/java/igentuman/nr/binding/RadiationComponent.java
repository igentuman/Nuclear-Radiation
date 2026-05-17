package igentuman.nr.binding;

import com.mojang.serialization.Codec;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.Isotope;
import igentuman.nr.core.IsotopeStack;
import igentuman.nr.core.RadiationProfile;
import igentuman.nr.registry.IsotopeRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class RadiationComponent {
    private final Map<String, Double> atomsByIsotope;
    private final long lastTick;

    public static final Codec<RadiationComponent> CODEC = Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
            .xmap(m -> new RadiationComponent(new LinkedHashMap<>(m), 0L),
                  c -> c.atomsByIsotope);

    public static final StreamCodec<RegistryFriendlyByteBuf, RadiationComponent> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.DOUBLE),
                    c -> c.atomsByIsotope,
                    m -> new RadiationComponent(m, 0L)
            );

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(NuclearRadiation.MODID);

    public static final Supplier<DataComponentType<RadiationComponent>> TYPE =
            COMPONENTS.registerComponentType("radiation",
                    builder -> builder.persistent(CODEC).networkSynchronized(STREAM_CODEC));

    public RadiationComponent(Map<String, Double> atomsByIsotope, long lastTick) {
        this.atomsByIsotope = atomsByIsotope;
        this.lastTick = lastTick;
    }

    public static RadiationComponent empty() {
        return new RadiationComponent(Collections.emptyMap(), 0L);
    }

    public Map<String, Double> atomsByIsotope() {
        return Collections.unmodifiableMap(atomsByIsotope);
    }

    public long lastTick() { return lastTick; }

    public boolean isEmpty() { return atomsByIsotope.isEmpty(); }

    public RadiationProfile toProfile(long timestamp) {
        RadiationProfile p = new RadiationProfile();
        for (Map.Entry<String, Double> e : atomsByIsotope.entrySet()) {
            Isotope iso = IsotopeRegistry.get(e.getKey());
            if (iso == null) continue;
            p.put(new IsotopeStack(iso, e.getValue(), timestamp));
        }
        return p;
    }

    public static RadiationComponent fromProfile(RadiationProfile profile, long currentTick) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (IsotopeStack s : profile.stacks()) {
            map.put(s.isotope().id(), s.atoms());
        }
        return new RadiationComponent(map, currentTick);
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RadiationComponent c)) return false;
        return lastTick == c.lastTick && atomsByIsotope.equals(c.atomsByIsotope);
    }

    @Override
    public int hashCode() {
        return atomsByIsotope.hashCode() * 31 + Long.hashCode(lastTick);
    }

    public static ResourceLocation key() {
        return ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "radiation");
    }
}
