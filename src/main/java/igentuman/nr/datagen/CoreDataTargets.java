package igentuman.nr.datagen;

import igentuman.nr.NuclearRadiation;
import net.minecraft.resources.Identifier;

/** Generate direct IDs only for core content and verified matching optional builds. */
final class CoreDataTargets {
    private CoreDataTargets() {}

    static boolean includes(Identifier target) {
        return switch (target.getNamespace()) {
            // MEGATON publishes a matching 26.1.2 NeoForge jar. Missing direct IDs
            // are ignored by the binding registries when the optional mod is absent.
            case "minecraft", "c", "nuclearcraft", NuclearRadiation.MODID -> true;
            default -> false;
        };
    }
}
