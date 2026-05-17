package igentuman.nr.medicine.items;

import igentuman.nr.medicine.NREffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AntiRadInjectionItem extends Item {

    public AntiRadInjectionItem(Properties props) {
        super(props.stacksTo(8));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) { return 16; }

    @Override
    public net.minecraft.world.item.UseAnim getUseAnimation(ItemStack stack) {
        return net.minecraft.world.item.UseAnim.DRINK;
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                                      net.minecraft.world.InteractionHand hand) {
        return ItemUtils.startUsing(player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            entity.addEffect(new MobEffectInstance(NREffects.PURGE, 6000, 9, true, true, true));
        }
        if (entity instanceof Player p && !p.getAbilities().instabuild) stack.shrink(1);
        return stack;
    }
}
