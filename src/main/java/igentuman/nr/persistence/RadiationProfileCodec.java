package igentuman.nr.persistence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nr.api.Isotope;
import igentuman.nr.core.IsotopeStack;
import igentuman.nr.core.RadiationProfile;
import igentuman.nr.registry.IsotopeRegistry;

import java.util.List;
import java.util.stream.Collectors;

public final class RadiationProfileCodec {

    private record StackEntry(String isotope, double atoms, long timestamp) {}

    private static final Codec<StackEntry> STACK_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("isotope").forGetter(StackEntry::isotope),
            Codec.DOUBLE.fieldOf("atoms").forGetter(StackEntry::atoms),
            Codec.LONG.optionalFieldOf("t", 0L).forGetter(StackEntry::timestamp)
    ).apply(inst, StackEntry::new));

    public static final Codec<RadiationProfile> CODEC = STACK_CODEC.listOf().xmap(
            list -> {
                RadiationProfile p = new RadiationProfile();
                for (StackEntry e : list) {
                    Isotope iso = IsotopeRegistry.get(e.isotope());
                    if (iso == null) continue;
                    p.put(new IsotopeStack(iso, e.atoms(), e.timestamp()));
                }
                return p;
            },
            profile -> profile.stacks().stream()
                    .map(s -> new StackEntry(s.isotope().id(), s.atoms(), s.timestamp()))
                    .collect(Collectors.toList())
    );

    private RadiationProfileCodec() {}

    public static List<?> dummy() { return List.of(); }
}
