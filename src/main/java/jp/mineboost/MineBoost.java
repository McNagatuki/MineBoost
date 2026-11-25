package jp.mineboost;

import com.mojang.serialization.Codec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(MineBoost.ID)
public final class MineBoost {
    public static final String ID = "mineboost";
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ID);
    static {
        LOOT.register("multiply_block_drops", () -> BlockDropModifier.CODEC);
    }

    // Legacy no-arg constructor + get(): the server runs Forge 47.3.0, which has neither
    // constructor injection nor FMLJavaModLoadingContext#registerConfig.
    @SuppressWarnings("removal")
    public MineBoost() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        LOOT.register(modBus);
        modBus.addListener(PlacedBlocks::registerCapabilities);
        MinecraftForge.EVENT_BUS.register(PlacedBlocks.class);
        // COMMON is local to the server, not sent to clients via Forge config sync.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Settings.SPEC, "tagboost.toml");
        MinecraftForge.EVENT_BUS.register(MiningEvents.class);
        MinecraftForge.EVENT_BUS.register(BoostCommands.class);
    }
}
