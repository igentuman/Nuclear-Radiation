package igentuman.nr.compat.ntm;

import com.hbm.blocks.generic.BlockHazard;
import com.hbm.blocks.generic.BlockHazardFalling;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.config.NtmCompatConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Pinned-NTM placed hazard blocks as NR block sources. Only reached through NtmCompatibility.enabled().
 * Native emission per 20 ticks is {@code 0.1 x level}, where the level is the {@code hbm:radiation_emission}
 * value (or registered radMax) for BlockHazard and the block item's hazard level for BlockHazardFalling;
 * both equal the carried block item's level, so the block uses the same composition and conversion as the item.
 */
final class NtmBlockBridge {
    private NtmBlockBridge() {}

    static boolean hazardBlock(Block block) {
        return block instanceof BlockHazard || block instanceof BlockHazardFalling;
    }

    /** Live native level: value maps and hazard data reload without an NR-side cache. */
    static double nativeLevel(Block block) {
        double level;
        if (block instanceof BlockHazard hazard) level = hazard.getRadMax();
        else if (block instanceof BlockHazardFalling) level = NtmItemBridge.nativeLevel(new ItemStack(block));
        else return 0.0;
        return Double.isFinite(level) ? level : 0.0;
    }

    /** Native area exposure (ContaminationUtil.radiate within 32 blocks). Not imported; stays native. */
    static float areaRadiation(Block block) {
        return block instanceof BlockHazard hazard ? hazard.getRad3d() : 0.0F;
    }

    static RadiationProfile profile(BlockState state) {
        Block block = state.getBlock();
        if (!hazardBlock(block)) return RadiationProfile.empty();
        double level = nativeLevel(block);
        if (!(level > 1.0e-5)) return RadiationProfile.empty();
        return NtmItemProfiles.profile(level, NtmItemBridge.composition(block.asItem()),
                NtmCompatConfig.SV_PER_ITEM_RADIATION_UNIT.get());
    }
}
