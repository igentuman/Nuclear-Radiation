package igentuman.nr.integration.thewasteland;

import net.mcreator.thewastelandreworked.TheWastelandReworkedMod;
import net.mcreator.thewastelandreworked.configuration.TheWastelandConfigConfiguration;
import igentuman.nr.items.GeigerCounterItem;
import igentuman.nr.radiation.storage.NRAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/** Called only when The Wasteland Reworked is loaded. */
public final class TheWastelandGeiger {
    private static final ResourceLocation COUNTER = ResourceLocation.parse("the_wasteland_reworked:geiger_counter");
    private static final ResourceLocation CLICK = ResourceLocation.parse("the_wasteland_reworked:item.geiger_counter.click");

    private TheWastelandGeiger() {}

    public static boolean isEquipped(Player player) {
        IItemHandler curios = TheWastelandReworkedMod.CuriosApiHelper.getCuriosInventory(player);
        if (curios == null) return false;
        for (int slot = 0; slot < curios.getSlots(); slot++) {
            if (COUNTER.equals(BuiltInRegistries.ITEM.getKey(curios.getStackInSlot(slot).getItem()))) return true;
        }
        return false;
    }

    public static void click(Entity entity, ItemStack stack) {
        if (!(entity instanceof Player player) || !(entity.level() instanceof ServerLevel level)) return;
        if (!TheWastelandConfigConfiguration.GEIGER_COUNTER_SOUNDS.get()) return;
        if (player.getMainHandItem() != stack && player.getOffhandItem() != stack) return;
        double svh = player.getData(NRAttachments.ENTITY_RADIATION.get()).svPerHourAmbient();
        if (svh <= GeigerCounterItem.SILENT_SVH) return;
        double response = GeigerCounterItem.responseT(svh);
        int interval = Math.max(1, (int) Math.round(40.0 - 39.0 * response));
        if (level.getGameTime() % interval != 0) return;
        level.playSound(null, player.blockPosition(), BuiltInRegistries.SOUND_EVENT.get(CLICK),
                SoundSource.NEUTRAL, 0.2f, (float) (0.7 + response));
    }
}
