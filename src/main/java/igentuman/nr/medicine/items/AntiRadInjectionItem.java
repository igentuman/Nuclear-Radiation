package igentuman.nr.medicine.items;

import igentuman.nr.medicine.NREffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class AntiRadInjectionItem extends Item {

    public AntiRadInjectionItem(Properties props) {
        super(props.stacksTo(8));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) { return 16; }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsing(player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            entity.addEffect(new MobEffectInstance(NREffects.PURGE, 6000, 21, true, true, true));
        }
        if (entity instanceof Player p && !p.getAbilities().instabuild) stack.shrink(1);
        return stack;
    }
}
