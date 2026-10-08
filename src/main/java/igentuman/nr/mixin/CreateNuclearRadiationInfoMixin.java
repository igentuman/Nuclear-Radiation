package igentuman.nr.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import igentuman.nr.radiation.storage.EntityRadiationData;
import igentuman.nr.radiation.storage.NRAttachments;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.nuclearteam.createnuclear.infrastructure.command.RadiationInfoCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(value = RadiationInfoCommand.class, remap = false)
public class CreateNuclearRadiationInfoMixin {
    @Inject(method = "runInfos", at = @At("HEAD"), cancellable = true)
    private static void nr$showNuclearRadiationData(CommandContext<CommandSourceStack> context,
                                                     CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        for (Entity target : EntityArgument.getEntities(context, "targets")) {
            EntityRadiationData data = target.getData(NRAttachments.ENTITY_RADIATION.get());
            String info = String.format(Locale.ROOT, "%s: %.6g Sv total, %.6g Sv/h",
                    target.getDisplayName().getString(), data.svTotalCareer(), data.svPerHour());
            context.getSource().sendSuccess(() -> Component.literal(info), false);
        }
        cir.setReturnValue(1);
    }
}
