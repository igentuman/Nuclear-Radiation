package igentuman.nr.tools;

import igentuman.nr.persistence.EntityRadiationData;
import igentuman.nr.persistence.NRAttachments;
import igentuman.nr.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DosimeterItem extends Item {

    public DosimeterItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            EntityRadiationData d = player.getData(NRAttachments.ENTITY_RADIATION.get());
            player.displayClientMessage(Component.literal(
                    "Total: " + TextUtils.formatSv(d.svTotalCareer())
                            + " | Rate: " + TextUtils.formatSvPerHour(d.svPerHour())), true);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
