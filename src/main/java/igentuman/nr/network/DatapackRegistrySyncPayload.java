package igentuman.nr.network;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.DecayEdge;
import igentuman.nr.api.DecayGraph;
import igentuman.nr.api.RadiationProfileBuilder;
import igentuman.nr.api.RadiationQuality;
import igentuman.nr.api.binding.Bindings;
import igentuman.nr.api.isotope.IsotopeImpl;
import igentuman.nr.api.isotope.IsotopeRegistry;
import igentuman.nr.api.shielding.ArmorProtectionRegistry;
import igentuman.nr.api.shielding.ShieldingTier;
import igentuman.nr.radiation.shielding.world.ShieldingBindings;
import igentuman.nr.radiation.shielding.world.ShieldingRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record DatapackRegistrySyncPayload(
        List<IsotopeImpl> isotopes,
        Map<String, List<DecayEdge>> decayGraph,
        List<BindingDTO> bindings,
        List<ArmorDTO> armors,
        List<ShieldingPresetDTO> shieldingPresets,
        List<ShieldingBlockDTO> shieldingBlocks
) implements CustomPacketPayload {

    public static final Type<DatapackRegistrySyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "datapack_registry_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DatapackRegistrySyncPayload> STREAM_CODEC = StreamCodec.of(
            DatapackRegistrySyncPayload::encode,
            DatapackRegistrySyncPayload::decode
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     Executed on the client when a packet is received from the server
     */
    public void handleData(IPayloadContext context) {
        context.enqueueWork(() -> {
            // Clearing Old Client Caches
            IsotopeRegistry.clear();
            DecayGraph.clear();
            Bindings.clear();
            ArmorProtectionRegistry.clear();
            ShieldingBindings.clear();

            // 1. Isotope Recovery
            for (IsotopeImpl iso : isotopes()) {
                IsotopeRegistry.register(iso);
            }

            // 1b. Decay Graph Recovery
            for (Map.Entry<String, List<DecayEdge>> entry : decayGraph().entrySet()) {
                for (DecayEdge edge : entry.getValue()) {
                    DecayGraph.addEdge(entry.getKey(), edge);
                }
            }

            // 2. Restoring Ties (Bindings)
            for (BindingDTO dto : bindings()) {
                java.util.function.Supplier<igentuman.nr.api.RadiationProfile> profileSupplier = () -> {
                    RadiationProfileBuilder builder = RadiationProfileBuilder.create();
                    dto.atoms().forEach(builder::isotope);
                    return builder.build();
                };

                switch (dto.targetType()) {
                    case TargetTypes.ITEM -> Bindings.putItem(dto.targetId(), profileSupplier);
                    case TargetTypes.BLOCK -> Bindings.putBlock(dto.targetId(), profileSupplier);
                    case TargetTypes.FLUID -> Bindings.putFluid(dto.targetId(), profileSupplier);
                    case TargetTypes.ITEM_TAG -> Bindings.putItemTag(TagKey.create(Registries.ITEM, dto.targetId()), profileSupplier);
                    case TargetTypes.BLOCK_TAG -> Bindings.putBlockTag(TagKey.create(Registries.BLOCK, dto.targetId()), profileSupplier);
                    case TargetTypes.FLUID_TAG -> Bindings.putFluidTag(TagKey.create(Registries.FLUID, dto.targetId()), profileSupplier);
                }
            }

            // 3. Armor Regeneration
            for (ArmorDTO dto : armors()) {
                ArmorProtectionRegistry.Protection p = new ArmorProtectionRegistry.Protection(
                        dto.xray(), dto.alpha(), dto.beta(), dto.neutron(), dto.gasProtection()
                );
                if (dto.isTag()) {
                    ArmorProtectionRegistry.registerTag(TagKey.create(Registries.ITEM, dto.targetId()), p);
                } else {
                    Item item = BuiltInRegistries.ITEM.get(dto.targetId());
                    if (item != null && item != Items.AIR) {
                        ArmorProtectionRegistry.register(item, p);
                    }
                }
            }

            // 4. Restoring Panel Presets
            for (ShieldingPresetDTO dto : shieldingPresets()) {
                ShieldingTier tier = ShieldingTier.byId(dto.tierId());
                if (tier != null) {
                    ShieldingBindings.setTierPreset(tier, new ShieldingRegistry.Coeffs(dto.xray(), dto.neutron()));
                }
            }

            // 5. Restoration of Switchgear Panels
            for (ShieldingBlockDTO dto : shieldingBlocks()) {
                ShieldingBindings.ShieldEntry entry;
                if (dto.usesTier()) {
                    ShieldingTier tier = ShieldingTier.byId(dto.tierId());
                    if (tier == null) continue;
                    entry = ShieldingBindings.ShieldEntry.ofTier(tier);
                } else {
                    entry = ShieldingBindings.ShieldEntry.ofRaw(new ShieldingRegistry.Coeffs(dto.xray(), dto.neutron()));
                }

                if (dto.isTag()) {
                    ShieldingBindings.putBlockTag(TagKey.create(Registries.BLOCK, dto.targetId()), entry);
                } else {
                    ShieldingBindings.putBlock(dto.targetId(), entry);
                }
            }

            NuclearRadiation.LOGGER.debug("Successfully synced NR Datapacks from server. Loaded {} isotopes, {} decay edges, {} bindings, {} armors, {} shields.",
                    isotopes().size(), decayGraph().size(), bindings().size(), armors().size(), shieldingBlocks().size());
        });
    }

    private static void encode(RegistryFriendlyByteBuf buf, DatapackRegistrySyncPayload payload) {
        buf.writeVarInt(payload.isotopes().size());
        for (IsotopeImpl iso : payload.isotopes()) {
            buf.writeUtf(iso.id());
            buf.writeFloat(iso.xRayStrength());
            buf.writeFloat(iso.alphaStrength());
            buf.writeFloat(iso.betaStrength());
            buf.writeFloat(iso.neutronStrength());
            buf.writeLong(iso.halfLifeTicks());

            buf.writeBoolean(iso.decaysToId() != null);
            if (iso.decaysToId() != null) {
                buf.writeUtf(iso.decaysToId());
            }

            buf.writeFloat(iso.quality().qXRay);
            buf.writeFloat(iso.quality().qBeta);
            buf.writeFloat(iso.quality().qAlpha);
            buf.writeFloat(iso.quality().qNeutron);
        }

        buf.writeVarInt(payload.decayGraph().size());
        for (Map.Entry<String, List<DecayEdge>> entry : payload.decayGraph().entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeVarInt(entry.getValue().size());
            for (DecayEdge edge : entry.getValue()) {
                buf.writeUtf(edge.targetIsotopeId());
                buf.writeDouble(edge.probability());
            }
        }

        buf.writeVarInt(payload.bindings().size());
        for (BindingDTO bind : payload.bindings()) {
            buf.writeByte(bind.targetType());
            buf.writeResourceLocation(bind.targetId());

            buf.writeVarInt(bind.atoms().size());
            for (Map.Entry<String, Double> entry : bind.atoms().entrySet()) {
                buf.writeUtf(entry.getKey());
                buf.writeDouble(entry.getValue());
            }
        }

        buf.writeVarInt(payload.armors().size());
        for (ArmorDTO armor : payload.armors()) {
            buf.writeBoolean(armor.isTag());
            buf.writeResourceLocation(armor.targetId());
            buf.writeDouble(armor.xray());
            buf.writeDouble(armor.alpha());
            buf.writeDouble(armor.beta());
            buf.writeDouble(armor.neutron());
            buf.writeBoolean(armor.gasProtection());
        }

        buf.writeVarInt(payload.shieldingPresets().size());
        for (ShieldingPresetDTO preset : payload.shieldingPresets()) {
            buf.writeUtf(preset.tierId());
            buf.writeDouble(preset.xray());
            buf.writeDouble(preset.neutron());
        }

        buf.writeVarInt(payload.shieldingBlocks().size());
        for (ShieldingBlockDTO shieldBlock : payload.shieldingBlocks()) {
            buf.writeBoolean(shieldBlock.isTag());
            buf.writeResourceLocation(shieldBlock.targetId());
            buf.writeBoolean(shieldBlock.usesTier());

            if (shieldBlock.usesTier()) {
                buf.writeUtf(shieldBlock.tierId());
            } else {
                buf.writeDouble(shieldBlock.xray());
                buf.writeDouble(shieldBlock.neutron());
            }
        }
    }

    private static DatapackRegistrySyncPayload decode(RegistryFriendlyByteBuf buf) {
        int isoSize = buf.readVarInt();
        List<IsotopeImpl> isotopes = new ArrayList<>(isoSize);
        for (int i = 0; i < isoSize; i++) {
            String id = buf.readUtf();
            float xray = buf.readFloat();
            float alpha = buf.readFloat();
            float beta = buf.readFloat();
            float neutron = buf.readFloat();
            long halfLife = buf.readLong();

            String decaysTo = null;
            if (buf.readBoolean()) {
                decaysTo = buf.readUtf();
            }

            float qXray = buf.readFloat();
            float qBeta = buf.readFloat();
            float qAlpha = buf.readFloat();
            float qNeutron = buf.readFloat();

            isotopes.add(new IsotopeImpl(id, xray, alpha, beta, neutron, halfLife, decaysTo,
                    new RadiationQuality(qXray, qBeta, qAlpha, qNeutron)));
        }

        int graphSize = buf.readVarInt();
        Map<String, List<DecayEdge>> decayGraph = new LinkedHashMap<>(graphSize);
        for (int i = 0; i < graphSize; i++) {
            String fromId = buf.readUtf();
            int edgeSize = buf.readVarInt();
            List<DecayEdge> edges = new ArrayList<>(edgeSize);
            for (int j = 0; j < edgeSize; j++) {
                edges.add(new DecayEdge(buf.readUtf(), buf.readDouble()));
            }
            decayGraph.put(fromId, edges);
        }

        int bindSize = buf.readVarInt();
        List<BindingDTO> bindings = new ArrayList<>(bindSize);
        for (int i = 0; i < bindSize; i++) {
            byte targetType = buf.readByte();
            ResourceLocation targetId = buf.readResourceLocation();

            int atomSize = buf.readVarInt();
            Map<String, Double> atoms = new LinkedHashMap<>(atomSize);
            for (int j = 0; j < atomSize; j++) {
                atoms.put(buf.readUtf(), buf.readDouble());
            }

            bindings.add(new BindingDTO(targetType, targetId, atoms));
        }

        int armorSize = buf.readVarInt();
        List<ArmorDTO> armors = new ArrayList<>(armorSize);
        for (int i = 0; i < armorSize; i++) {
            boolean isTag = buf.readBoolean();
            ResourceLocation targetId = buf.readResourceLocation();
            double xray = buf.readDouble();
            double alpha = buf.readDouble();
            double beta = buf.readDouble();
            double neutron = buf.readDouble();
            boolean gasProtection = buf.readBoolean();
            armors.add(new ArmorDTO(isTag, targetId, xray, alpha, beta, neutron, gasProtection));
        }

        int presetSize = buf.readVarInt();
        List<ShieldingPresetDTO> shieldingPresets = new ArrayList<>(presetSize);
        for (int i = 0; i < presetSize; i++) {
            shieldingPresets.add(new ShieldingPresetDTO(buf.readUtf(), buf.readDouble(), buf.readDouble()));
        }

        int shieldSize = buf.readVarInt();
        List<ShieldingBlockDTO> shieldingBlocks = new ArrayList<>(shieldSize);
        for (int i = 0; i < shieldSize; i++) {
            boolean isTag = buf.readBoolean();
            ResourceLocation targetId = buf.readResourceLocation();
            boolean usesTier = buf.readBoolean();

            String tierId = null;
            double xray = 0;
            double neutron = 0;

            if (usesTier) {
                tierId = buf.readUtf();
            } else {
                xray = buf.readDouble();
                neutron = buf.readDouble();
            }
            shieldingBlocks.add(new ShieldingBlockDTO(isTag, targetId, usesTier, tierId, xray, neutron));
        }

        return new DatapackRegistrySyncPayload(isotopes, decayGraph, bindings, armors, shieldingPresets, shieldingBlocks);
    }

    public static final class TargetTypes {
        public static final byte ITEM = 0;
        public static final byte BLOCK = 1;
        public static final byte FLUID = 2;
        public static final byte ITEM_TAG = 3;
        public static final byte BLOCK_TAG = 4;
        public static final byte FLUID_TAG = 5;
    }

    public record BindingDTO(byte targetType, ResourceLocation targetId, Map<String, Double> atoms) {}

    public record ArmorDTO(
            boolean isTag, ResourceLocation targetId,
            double xray, double alpha, double beta, double neutron, boolean gasProtection
    ) {}

    public record ShieldingPresetDTO(String tierId, double xray, double neutron) {}

    public record ShieldingBlockDTO(
            boolean isTag, ResourceLocation targetId, boolean usesTier,
            String tierId, double xray, double neutron
    ) {}
}
