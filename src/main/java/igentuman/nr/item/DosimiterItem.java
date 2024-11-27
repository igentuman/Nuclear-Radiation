package igentuman.nr.item;

import igentuman.nr.radiation.data.PlayerRadiation;
import igentuman.nr.radiation.data.PlayerRadiationProvider;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class DosimiterItem extends Item
{
	public DosimiterItem(Properties props)
	{
		super(props);
	}


	@NotNull
	@Override
	public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!world.isClientSide()) {
			PlayerRadiation radiationCap = player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).orElse(null);
			if(radiationCap == null) return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
			long radiation = radiationCap.getRadiation();
			player.sendSystemMessage(Component.translatable("message.nr.player_radiation_contamination", format(radiation)));
			CriteriaTriggers.USING_ITEM.trigger((ServerPlayer) player, stack);
		}
		return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
	}

	private static String format(long radiation) {
		if(radiation >= 1000000) {
			return String.format(Locale.US,"%.2f", (float)radiation/1000000)+" Rad";
		}
		if(radiation >= 1000) {
			return String.format(Locale.US,"%.2f", (float)radiation/1000)+" mRad";
		}
		return String.format(Locale.US,"%.2f", (float)radiation)+" uRad";
	}
}
