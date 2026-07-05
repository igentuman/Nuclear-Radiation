package igentuman.nr.binding;

import igentuman.nr.api.IsotopeStack;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.shielding.ArmorProtectionRegistry;
import igentuman.nr.shielding.ShieldingRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import static igentuman.nr.util.TextUtils.__;
import static igentuman.nr.util.TextUtils.numberFormat;
import static net.minecraft.ChatFormatting.DARK_GRAY;
import static net.minecraft.ChatFormatting.GRAY;

public class RadiationTooltip {

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        addShieldingTooltip(event);
        addArmorProtectionTooltip(event);

        RadiationProfile p = RadiationBindings.of(event.getItemStack());
        if (p.isEmpty()) return;

        double bq = p.totalActivityBq();
        double alpha = p.alphaActivityBq();
        double beta = p.betaActivityBq();
        double neutron = p.neutronActivityBq();
        double gamma = p.xRayActivityBq();

        event.getToolTip().add(Component.literal("☢ ")
                .append(Component.literal(formatActivity(bq)))
                .withStyle(colorForBq(bq)));
        if (event.getItemStack().getCount() > 1) {
            event.getToolTip().add(Component.literal("☢ Stack ")
                    .append(Component.literal(formatActivity(bq*event.getItemStack().getCount())))
                    .withStyle(colorForBq(bq*event.getItemStack().getCount())));
        }
        if (alpha > 0) {
            event.getToolTip().add(__("jei.nuclear_radiation.radioactive_items.alpha", numberFormat(alpha/bq) + "%").withStyle(DARK_GRAY));
        }
        if (beta > 0) {
            event.getToolTip().add(__("jei.nuclear_radiation.radioactive_items.beta", numberFormat(beta/bq) + "%").withStyle(DARK_GRAY));
        }
        if (gamma > 0) {
            event.getToolTip().add(__("jei.nuclear_radiation.radioactive_items.gamma", numberFormat(gamma/bq) + "%").withStyle(DARK_GRAY));
        }
        if (neutron > 0) {
            event.getToolTip().add(__("jei.nuclear_radiation.radioactive_items.neutron", numberFormat(neutron/bq) + "%").withStyle(DARK_GRAY));
        }
    }

    private static void addShieldingTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof BlockItem bi)) return;
        if (ShieldingRegistry.get(bi.getBlock().defaultBlockState()) == null) return;
        event.getToolTip().add(Component.translatable("tooltip.nuclear_radiation.block_shielding")
                .withStyle(ChatFormatting.AQUA));
    }

    private static void addArmorProtectionTooltip(ItemTooltipEvent event) {
        ArmorProtectionRegistry.Protection prot = ArmorProtectionRegistry.get(event.getItemStack());
        if (prot.xray() <= 0 && prot.alpha() <= 0 && prot.beta() <= 0 && prot.neutron() <= 0) return;

        event.getToolTip().add(Component.translatable("tooltip.nuclear_radiation.armor_protection")
                .withStyle(ChatFormatting.AQUA));
        event.getToolTip().add(Component.translatable("tooltip.nuclear_radiation.armor.xray", formatPercent(prot.xray()))
                .withStyle(GRAY));
        event.getToolTip().add(Component.translatable("tooltip.nuclear_radiation.armor.alpha", formatPercent(prot.alpha()))
                .withStyle(GRAY));
        event.getToolTip().add(Component.translatable("tooltip.nuclear_radiation.armor.beta", formatPercent(prot.beta()))
                .withStyle(GRAY));
        event.getToolTip().add(Component.translatable("tooltip.nuclear_radiation.armor.neutron", formatPercent(prot.neutron()))
                .withStyle(GRAY));
    }

    private static String formatPercent(double v) {
        return String.format("%.1f%%", v * 100.0);
    }

    private static ChatFormatting colorForBq(double bq) {
        if (bq >= 1.0e13) return ChatFormatting.DARK_RED;
        if (bq >= 1.0e11) return ChatFormatting.RED;
        if (bq >= 1.0e8) return ChatFormatting.GOLD;
        if (bq >= 1.0e6) return ChatFormatting.YELLOW;
        return ChatFormatting.GREEN;
    }

    private static String formatActivity(double bq) {
        if (bq < 1.0e3) return String.format("%.1f Bq", bq);
        if (bq < 1.0e6) return String.format("%.2f kBq", bq / 1.0e3);
        if (bq < 1.0e9) return String.format("%.2f MBq", bq / 1.0e6);
        if (bq < 1.0e12) return String.format("%.2f GBq", bq / 1.0e9);
        if (bq < 1.0e15) return String.format("%.2f TBq", bq / 1.0e12);
        return String.format("%.2f PBq", bq / 1.0e15);
    }

    private String formatTime(double seconds) {
        if (seconds < 60) return String.format("%.1f s", seconds);
        if (seconds < 3600) return String.format("%.1f min", seconds / 60.0);
        if (seconds < 86400) return String.format("%.1f h", seconds / 3600.0);
        double days = seconds / 86400.0;
        if (days < 365) return String.format("%.1f d", days);
        double years = days / 365.0;
        if (years < 1_000) return String.format("%.1f years", years);
        if (years < 1_000_000) return String.format("%.1f thousand years", years / 1_000.0);
        if (years < 1_000_000_000L) return String.format("%.1f million years", years / 1_000_000.0);
        return String.format("%.1f billion years", years / 1_000_000_000.0);
    }
}
