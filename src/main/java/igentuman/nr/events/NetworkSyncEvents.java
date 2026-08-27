package igentuman.nr.events;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.DecayEdge;
import igentuman.nr.api.DecayGraph;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.binding.Bindings;
import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.api.isotope.IsotopeImpl;
import igentuman.nr.api.isotope.IsotopeRegistry;
import igentuman.nr.api.isotope.IsotopeStack;
import igentuman.nr.api.shielding.ArmorProtectionRegistry;
import igentuman.nr.api.shielding.ShieldingTier;
import igentuman.nr.network.DatapackRegistrySyncPayload;
import igentuman.nr.network.DatapackRegistrySyncPayload.*;
import igentuman.nr.radiation.shielding.world.ShieldingBindings;
import igentuman.nr.radiation.shielding.world.ShieldingRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class NetworkSyncEvents {

    @SubscribeEvent
    public void onDatapackSync(OnDatapackSyncEvent event) {
        // 1. Compilation of Isotopes and Decay Trees
        List<IsotopeImpl> isotopes = new ArrayList<>();
        Map<String, List<DecayEdge>> decayGraph = new LinkedHashMap<>();
        for (Isotope iso : IsotopeRegistry.all()) {
            isotopes.add((IsotopeImpl) iso);
            List<DecayEdge> edges = DecayGraph.outputs(iso.id());
            if (!edges.isEmpty()) {
                decayGraph.put(iso.id(), new ArrayList<>(edges));
            }
        }

        // 2. Compilation of Radiation Relationships (Bindings)
        List<BindingDTO> bindings = new ArrayList<>();
        packBindings(bindings, Bindings.items(), TargetTypes.ITEM);
        packBindings(bindings, Bindings.blocks(), TargetTypes.BLOCK);
        packBindings(bindings, Bindings.fluids(), TargetTypes.FLUID);
        packTagBindings(bindings, Bindings.itemTags(), TargetTypes.ITEM_TAG);
        packTagBindings(bindings, Bindings.blockTags(), TargetTypes.BLOCK_TAG);
        packTagBindings(bindings, Bindings.fluidTags(), TargetTypes.FLUID_TAG);

        // 3. Collecting Armor
        List<ArmorDTO> armors = new ArrayList<>();
        for (Map.Entry<Item, ArmorProtectionRegistry.Protection> entry : ArmorProtectionRegistry.all().entrySet()) {
            armors.add(new ArmorDTO(false, BuiltInRegistries.ITEM.getKey(entry.getKey()),
                    entry.getValue().xray(), entry.getValue().alpha(), entry.getValue().beta(), entry.getValue().neutron(), entry.getValue().protectsFromGas()));
        }
        for (Map.Entry<TagKey<Item>, ArmorProtectionRegistry.Protection> entry : ArmorProtectionRegistry.allTags().entrySet()) {
            armors.add(new ArmorDTO(true, entry.getKey().location(),
                    entry.getValue().xray(), entry.getValue().alpha(), entry.getValue().beta(), entry.getValue().neutron(), entry.getValue().protectsFromGas()));
        }

        // 4. Collecting Panels (Presets and Blocks)
        List<ShieldingPresetDTO> presets = new ArrayList<>();
        for (Map.Entry<ShieldingTier, ShieldingRegistry.Coeffs> entry : ShieldingBindings.tierPresets().entrySet()) {
            presets.add(new ShieldingPresetDTO(entry.getKey().id(), entry.getValue().xray(), entry.getValue().neutron()));
        }

        List<ShieldingBlockDTO> shieldBlocks = new ArrayList<>();
        for (Map.Entry<ResourceLocation, ShieldingBindings.ShieldEntry> entry : ShieldingBindings.blocks().entrySet()) {
            packShieldEntry(shieldBlocks, false, entry.getKey(), entry.getValue());
        }
        for (Map.Entry<TagKey<Block>, ShieldingBindings.ShieldEntry> entry : ShieldingBindings.blockTags().entrySet()) {
            packShieldEntry(shieldBlocks, true, entry.getKey().location(), entry.getValue());
        }

        DatapackRegistrySyncPayload payload = new DatapackRegistrySyncPayload(isotopes, decayGraph, bindings, armors, presets, shieldBlocks);

        // 5. Sending and Logging
        if (event.getPlayer() != null) {
            NuclearRadiation.LOGGER.debug("Sending NR Datapack Sync to player {}. Packing {} isotopes, {} decay edges, {} bindings, {} armors, {} shields.",
                    event.getPlayer().getName().getString(), isotopes.size(), decayGraph.size(), bindings.size(), armors.size(), shieldBlocks.size());
            PacketDistributor.sendToPlayer(event.getPlayer(), payload);
        } else {
            NuclearRadiation.LOGGER.info("Sending NR Datapack Sync globally (reload). Packing {} isotopes, {} decay edges, {} bindings, {} armors, {} shields.",
                    isotopes.size(), decayGraph.size(), bindings.size(), armors.size(), shieldBlocks.size());
            for (ServerPlayer player : event.getPlayerList().getPlayers()) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }

    private static void packBindings(List<BindingDTO> list, Map<ResourceLocation, Supplier<RadiationProfile>> map, byte type) {
        for (Map.Entry<ResourceLocation, Supplier<RadiationProfile>> entry : map.entrySet()) {
            RadiationProfile profile = entry.getValue().get();
            if (profile.isEmpty()) continue;
            Map<String, Double> atoms = new HashMap<>();
            for (IsotopeStack stack : profile.stacks()) {
                atoms.put(stack.isotope().id(), stack.atoms());
            }
            list.add(new BindingDTO(type, entry.getKey(), atoms));
        }
    }

    private static <T> void packTagBindings(List<BindingDTO> list, Map<TagKey<T>, Supplier<RadiationProfile>> map, byte type) {
        for (Map.Entry<TagKey<T>, Supplier<RadiationProfile>> entry : map.entrySet()) {
            RadiationProfile profile = entry.getValue().get();
            if (profile.isEmpty()) continue;
            Map<String, Double> atoms = new HashMap<>();
            for (IsotopeStack stack : profile.stacks()) {
                atoms.put(stack.isotope().id(), stack.atoms());
            }
            list.add(new BindingDTO(type, entry.getKey().location(), atoms));
        }
    }

    private static void packShieldEntry(List<ShieldingBlockDTO> list, boolean isTag, ResourceLocation id, ShieldingBindings.ShieldEntry entry) {
        if (entry.tier() != null) {
            list.add(new ShieldingBlockDTO(isTag, id, true, entry.tier().id(), 0, 0));
        } else if (entry.raw() != null) {
            list.add(new ShieldingBlockDTO(isTag, id, false, null, entry.raw().xray(), entry.raw().neutron()));
        }
    }
}