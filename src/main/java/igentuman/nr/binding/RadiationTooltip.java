package igentuman.nr.binding;

import igentuman.nr.core.IsotopeStack;
import igentuman.nr.core.RadiationProfile;
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
        event.getToolTip().add(Component.literal(String.format("☢ %.2e Bq", bq))
                .withStyle(ChatFormatting.GREEN));
        IsotopeStack top = null;
        for (IsotopeStack s : p.stacks()) {
            if (top == null || s.currentActivityBq() > top.currentActivityBq()) top = s;
        }
        if (top != null) {
            double halfLifeSeconds = top.isotope().halfLifeTicks() / 20.0;
            event.getToolTip().add(Component.literal(
                    String.format("%s · t½ %s", top.isotope().id(), formatTime(halfLifeSeconds)))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private String formatTime(double seconds) {
        if (seconds < 60) return String.format("%.1fs", seconds);
        if (seconds < 3600) return String.format("%.1fm", seconds / 60);
        if (seconds < 86400) return String.format("%.1fh", seconds / 3600);
        if (seconds < 365.0 * 86400) return String.format("%.1fd", seconds / 86400);
        return String.format("%.2gy", seconds / (365.0 * 86400));
    }
}
