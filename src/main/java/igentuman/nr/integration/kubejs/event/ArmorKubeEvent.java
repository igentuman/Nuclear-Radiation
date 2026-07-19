package igentuman.nr.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.KubeStartupEvent;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.integration.kubejs.NuclearRadiationKubeData;
import igentuman.nr.api.shielding.ArmorProtectionRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * {@code NRStartupEvents.armor} — assign per-item radiation protection factors (0..1) for each
 * channel to any wearable item. A full worn set stacks multiplicatively toward 1.0.
 */
public class ArmorKubeEvent implements KubeStartupEvent {

    /** Registers protection for an armor item: x-ray, alpha, beta, neutron (each 0..1). */
    public void add(String itemId, double xray, double alpha, double beta, double neutron) {
        add(itemId, xray, alpha, beta, neutron, false);
    }

    /** As above, plus gasProtection: when true and worn on the head, fully blocks radioactive gas inhalation. */
    public void add(String itemId, double xray, double alpha, double beta, double neutron, boolean gasProtection) {
        NuclearRadiationKubeData.ARMOR_OPS.add(() -> {
            Item item = resolve(itemId);
            if (item == null) return;
            ArmorProtectionRegistry.register(item,
                    new ArmorProtectionRegistry.Protection(xray, alpha, beta, neutron, gasProtection));
        });
    }

    public void remove(String itemId) {
        NuclearRadiationKubeData.ARMOR_OPS.add(() -> {
            Item item = resolve(itemId);
            if (item == null) return;
            ArmorProtectionRegistry.remove(item);
        });
    }

    private static Item resolve(String itemId) {
        ResourceLocation id = ResourceLocation.parse(itemId);
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            NuclearRadiation.LOGGER.warn("KubeJS armor protection: item not found '{}'", itemId);
            return null;
        }
        return item;
    }
}
