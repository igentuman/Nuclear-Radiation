package igentuman.nr.items;

import igentuman.nr.registry.NRGuide;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GuideBookItem extends Item {

    public GuideBookItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide && NRGuide.isLoaded()) {
            guideme.internal.GuideMEProxy.instance().openGuide(player, NRGuide.GUIDE_ID);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
