package jp.mineboost;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class BoostCommands {
    private BoostCommands() {}

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("mineboost")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                .then(branch("speed", Settings.SPEED_TAG, Settings.SPEED))
                .then(branch("drop", Settings.DROP_TAG, Settings.DROP)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> branch(String type,
            ForgeConfigSpec.ConfigValue<String> tag, ForgeConfigSpec.DoubleValue multiplier) {
        return Commands.literal(type)
                .then(Commands.literal("tag")
                        .then(Commands.argument("name", StringArgumentType.string()).executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            if (!Settings.validTag(name)) {
                                ctx.getSource().sendFailure(Component.literal("Invalid tag: use 1-128 letters, digits or _ . + : -"));
                                return 0;
                            }
                            tag.set(name);
                            tag.save();
                            ctx.getSource().sendSuccess(() -> Component.literal(type + " tag = " + name), true);
                            return 1;
                        })))
                .then(Commands.literal("multiplier")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(1.0, Settings.MAX)).executes(ctx -> {
                            double value = DoubleArgumentType.getDouble(ctx, "value");
                            multiplier.set(value);
                            multiplier.save();
                            ctx.getSource().sendSuccess(() -> Component.literal(type + " multiplier = " + value), true);
                            return 1;
                        })));
    }

    private static int status(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("MineBoost: speed=" + Settings.SPEED.get()
                + "x [" + Settings.SPEED_TAG.get() + "], drop=" + Settings.DROP.get()
                + "x [" + Settings.DROP_TAG.get() + "]"), false);
        return 1;
    }
}
