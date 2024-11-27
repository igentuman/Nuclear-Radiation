package igentuman.nr.handler.event.client;

import igentuman.nr.radiation.ItemRadiation;
import igentuman.nr.radiation.ItemShielding;
import igentuman.nr.radiation.RadiationCleaningItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Locale;

import static igentuman.nr.NuclearRadiation.MODID;
import static net.minecraft.world.item.Items.FILLED_MAP;
import static net.minecraft.world.item.Items.LIGHTNING_ROD;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class TooltipHandler {
    private static ItemTooltipEvent processedEvent;
    public static void register(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(TooltipHandler::handle);
    }
    @SubscribeEvent
    public static void handle(ItemTooltipEvent event) {
        if(event.equals(processedEvent)) return;
        processedEvent = event;
        Item item = event.getItemStack().getItem();
        miscTooltips(event, event.getItemStack());
        addRadiationLevelTooltip(event, item);
        addShieldingTooltip(event, event.getItemStack());
        addRadiationCleaningEffect(event, event.getItemStack());
    }


    private static void miscTooltips(ItemTooltipEvent event, ItemStack itemStack) {


    }

    private static void addRadiationCleaningEffect(ItemTooltipEvent event, ItemStack itemStack) {
        long radiation = RadiationCleaningItems.byItem(itemStack.getItem());
        if(radiation == 0) return;
        ChatFormatting color = ChatFormatting.GREEN;
        event.getToolTip().add(Component.translatable("tooltip.nr.radiation_removal", format(((double)radiation)/1000000000)+"Rad").withStyle(color));
    }

    private static void addShieldingTooltip(ItemTooltipEvent event, ItemStack item) {
        int shielding = ItemShielding.byItem(item.getItem());
        if((!item.hasTag() || !item.getTag().contains("rad_shielding")) &&  shielding == 0) return;
        ChatFormatting color = ChatFormatting.GOLD;
        if(item.hasTag() && item.getTag().contains("rad_shielding")) {
            shielding += item.getTag().getInt("rad_shielding");
        }
        event.getToolTip().add(Component.translatable("tooltip.nr.rad_shielding", shielding).withStyle(color));
    }

    private static void addRadiationLevelTooltip(ItemTooltipEvent event, Item item) {
        double radiation = ItemRadiation.byItem(item);
        ChatFormatting color = ChatFormatting.GRAY;
        if(radiation > 0) {
            if(radiation > 0.0001) {
                color = ChatFormatting.YELLOW;
            }
            if(radiation > 0.001) {
                color = ChatFormatting.GOLD;
            }
            if(radiation > 0.1) {
                color = ChatFormatting.RED;
            }
            event.getToolTip().add(Component.translatable("tooltip.nr.radiation", format(radiation)+"Rad/s").withStyle(color));
        }
    }

    private static String format(Double radiation) {
        if(radiation >= 1) {
            return String.format(Locale.US,"%.0f", radiation)+" ";
        }
        if(radiation >= 0.000999) {
            return String.format(Locale.US,"%.0f", radiation*1000)+" m";
        }
        if(radiation >= 0.000000999) {
            return String.format(Locale.US,"%.0f", radiation*1000000)+" u";
        }
        return String.format(Locale.US,"%.0f", radiation*1000000000)+" p";
    }
}
