package igentuman.nr.handler.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class NCRadiationCommand {

    public static void register(CommandDispatcher<CommandSourceStack> command) {
        command.register(Commands.literal("nc_radiation")
                .then(Commands.argument("action", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("disable");
                            builder.suggest("enable");
                            builder.suggest("clear_all");
                            builder.suggest("clear_chunk");
                            return builder.buildFuture();
                        })
                        .executes(NCRadiationCommand::executeCommand)
                )
        );
    }

    private static int executeCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        String action = StringArgumentType.getString(context, "action");


        return 1; // Command succeeded
    }

}
