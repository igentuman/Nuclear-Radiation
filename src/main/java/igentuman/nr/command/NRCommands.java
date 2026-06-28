package igentuman.nr.command;

import com.mojang.brigadier.CommandDispatcher;
import igentuman.nr.persistence.EntityRadiationData;
import igentuman.nr.persistence.NRAttachments;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class NRCommands {

    private NRCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("nr")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("clear")
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                            EntityRadiationData data = player.getData(NRAttachments.ENTITY_RADIATION.get());
                            data.setSvTotalCareer(0.0);
                            data.setSvPerHour(0.0);
                            data.setProtectionFactor(0.0);
                            data.setDecayMultiplier(1.0);
                            data.internalContamination().clear();
                            ctx.getSource().sendSuccess(
                                () -> Component.literal("Cleared radiation dose for " + player.getName().getString()),
                                true
                            );
                            return 1;
                        })
                    )
                )
        );
    }
}
