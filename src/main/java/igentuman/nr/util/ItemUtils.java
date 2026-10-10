package igentuman.nr.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public final class ItemUtils {
    private ItemUtils() {}

    public static InteractionResult startUsing(Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }
}
