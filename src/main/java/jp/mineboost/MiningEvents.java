package jp.mineboost;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class MiningEvents {
    private MiningEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity() instanceof ServerPlayer player && Settings.speedEnabled(player)) {
            float speed = event.getNewSpeed();
            if (speed > 0 && Float.isFinite(speed)) {
                event.setNewSpeed((float) Math.min(Float.MAX_VALUE, speed * Settings.SPEED.get()));
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            finishAcceleratedMining(player);
        }
    }

    private static void finishAcceleratedMining(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || player.isCreative() || !Settings.speedEnabled(player)) return;
        ServerPlayerGameMode mode = player.gameMode;
        // Only act on a START accepted by vanilla. Vanilla handles delayed STOP itself.
        if (!mode.isDestroyingBlock || mode.hasDelayedDestroy) return;
        BlockPos pos = mode.destroyPos;
        ServerLevel level = player.serverLevel();
        if (!level.hasChunkAt(pos) || !player.canReach(pos, 1.0) || !level.mayInteract(player, pos)
                || player.blockActionRestricted(level, pos, mode.getGameModeForPlayer())) {
            // Stop stale or invalid targets rather than force-breaking them.
            clearProgress(player, mode, level, pos);
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            clearProgress(player, mode, level, pos);
            return;
        }
        int ticks = mode.gameTicks - mode.destroyProgressStart + 1;
        // getDestroyProgress calls Forge BreakSpeed; do not multiply again.
        float perTick = state.getDestroyProgress(player, level, pos);
        if (!(perTick > 0) || ticks <= 0 || (double) perTick * ticks < 1.0) return;
        clearProgress(player, mode, level, pos);
        // Use vanilla/Forge path (BreakEvent, harvest checks, tool wear, XP, loot).
        mode.destroyBlock(pos);
        // Correct the local client even if it was still showing the original progress.
        player.connection.send(new ClientboundBlockUpdatePacket(level, pos));
    }

    private static void clearProgress(ServerPlayer player, ServerPlayerGameMode mode, ServerLevel level, BlockPos pos) {
        mode.isDestroyingBlock = false;
        mode.lastSentState = -1;
        level.destroyBlockProgress(player.getId(), pos, -1);
    }
}
