package igentuman.nr.medicine.items;

import igentuman.nr.medicine.NREffects;
import igentuman.nr.util.persistence.EntityRadiationData;
import igentuman.nr.util.persistence.NRAttachments;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class MedicineInstantItem extends Item {

    private final double instantSvRemoved;
    private final int durationTicks;
    private final int amplifier;
    private final Supplier<Holder<MobEffect>> isotopeEffect;

    public MedicineInstantItem(Properties props, double instantSvRemoved, int durationTicks, int amplifier) {
        this(props, instantSvRemoved, durationTicks, amplifier, null);
    }

    public MedicineInstantItem(Properties props, double instantSvRemoved, int durationTicks, int amplifier,
                               Supplier<Holder<MobEffect>> isotopeEffect) {
        super(props.stacksTo(16));
        this.instantSvRemoved = instantSvRemoved;
        this.durationTicks = durationTicks;
        this.amplifier = amplifier;
        this.isotopeEffect = isotopeEffect;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 16;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsing(player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            if (instantSvRemoved > 0.0) {
                EntityRadiationData data = entity.getData(NRAttachments.ENTITY_RADIATION.get());
                data.setSvTotalCareer(Math.max(0.0, data.svTotalCareer() - instantSvRemoved));
            }
            entity.addEffect(new MobEffectInstance(NREffects.PROTECTION, durationTicks, amplifier, true, true, true));
            entity.addEffect(new MobEffectInstance(NREffects.PURGE, durationTicks, amplifier, true, true, true));
            if (isotopeEffect != null) {
                entity.addEffect(new MobEffectInstance(isotopeEffect.get(), durationTicks, amplifier, true, true, true));
            }
        }
        if (entity instanceof Player p && !p.getAbilities().instabuild) stack.shrink(1);
        return stack;
    }
}
