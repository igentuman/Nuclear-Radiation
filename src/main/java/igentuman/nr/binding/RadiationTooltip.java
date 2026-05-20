package igentuman.nr.binding;

import igentuman.nr.api.IsotopeStack;
import igentuman.nr.api.RadiationProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public class RadiationTooltip {

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        RadiationProfile p = RadiationBindings.of(event.getItemStack());
        if (p.isEmpty()) return;

        double bq = p.totalActivityBq();
        event.getToolTip().add(Component.literal("☢ ")
                .append(Component.literal(formatActivity(bq)))
                .withStyle(colorForBq(bq)));

        IsotopeStack top = null;
        for (IsotopeStack s : p.stacks()) {
            if (top == null || s.currentActivityBq() > top.currentActivityBq()) top = s;
        }
        if (top != null) {
            double halfLifeSeconds = top.isotope().halfLifeTicks() / 20.0;
            event.getToolTip().add(Component.translatable("tooltip.nuclear_radiation.isotope",
                    Component.translatable(isotopeTranslationKey(top.isotope().id())))
                    .withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.translatable("tooltip.nuclear_radiation.half_life",
                    formatTime(halfLifeSeconds))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static String isotopeTranslationKey(String id) {
        String shortId = id.startsWith("nr:") ? id.substring(3) : id;
        return "isotope.nuclear_radiation." + shortId;
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
        return String.format("%.2f TBq", bq / 1.0e12);
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
