package igentuman.nr.items;

import igentuman.nr.radiation.storage.EntityRadiationData;
import igentuman.nr.radiation.storage.NRAttachments;
import igentuman.nr.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DosimeterItem extends Item {

    public DosimeterItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            EntityRadiationData d = player.getData(NRAttachments.ENTITY_RADIATION.get());
            player.sendOverlayMessage(Component.literal(
                    "Total: " + TextUtils.formatSv(d.svTotalCareer())
                            + " | Rate: " + TextUtils.formatSvPerHour(d.svPerHour())));
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
}
