package dev.ludwici.beyondthehorizons.neoforge.environment;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class EnvironmentCommands {

    private EnvironmentCommands() {
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("bth")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("environment")
                                .then(Commands.literal("debug")
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            boolean enabled = EnvironmentEvents.toggleDebug(player);
                                            context.getSource().sendSuccess(() -> Component.translatable(
                                                    enabled
                                                            ? "message.beyond_the_horizons.debug_enabled"
                                                            : "message.beyond_the_horizons.debug_disabled"
                                            ), false);
                                            return 1;
                                        }))
                                .then(Commands.literal("inspect")
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            context.getSource().sendSuccess(() -> EnvironmentEvents.inspect(player), false);
                                            return 1;
                                        })))
        );
    }
}
