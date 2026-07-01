package igentuman.nr.util.persistence;

import igentuman.nr.NuclearRadiation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class NRAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, NuclearRadiation.MODID);

    public static final Supplier<AttachmentType<ChunkRadiationData>> CHUNK_RADIATION =
            ATTACHMENTS.register("chunk_radiation",
                    () -> AttachmentType.builder(ChunkRadiationData::new)
                            .serialize(ChunkRadiationData.CODEC)
                            .build());

    public static final Supplier<AttachmentType<EntityRadiationData>> ENTITY_RADIATION =
            ATTACHMENTS.register("entity_radiation",
                    () -> AttachmentType.builder(EntityRadiationData::new)
                            .serialize(EntityRadiationData.CODEC)
                            .copyOnDeath()
                            .build());

    public static final Supplier<AttachmentType<LevelRadiationData>> LEVEL_SOURCES =
            ATTACHMENTS.register("level_sources",
                    () -> AttachmentType.<LevelRadiationData>builder(() -> new LevelRadiationData())
                            .serialize(LevelRadiationData.CODEC)
                            .build());

    private NRAttachments() {}

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
