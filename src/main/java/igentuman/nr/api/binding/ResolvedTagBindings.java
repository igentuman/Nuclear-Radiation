package igentuman.nr.api.binding;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.HashMap;
import java.util.Map;

/** Expands loaded registry tags once, preserving the first matching tag and direct-ID priority. */
public final class ResolvedTagBindings {
    private ResolvedTagBindings() {}

    public static <T, V> Map<T, V> expand(Registry<T> registry, Map<TagKey<T>, V> tags) {
        Map<T, V> resolved = new HashMap<>();
        for (Map.Entry<TagKey<T>, V> entry : tags.entrySet()) {
            registry.getTag(entry.getKey()).ifPresent(holders ->
                    holders.forEach(holder -> resolved.putIfAbsent(holder.value(), entry.getValue())));
        }
        return resolved;
    }

    public static <T, V> Map<T, V> compile(Registry<T> registry, Map<TagKey<T>, V> tags,
                                            Map<ResourceLocation, V> direct) {
        Map<T, V> resolved = expand(registry, tags);
        for (Map.Entry<ResourceLocation, V> entry : direct.entrySet()) {
            registry.getOptional(entry.getKey()).ifPresent(value -> resolved.put(value, entry.getValue()));
        }
        return Map.copyOf(resolved);
    }
}
