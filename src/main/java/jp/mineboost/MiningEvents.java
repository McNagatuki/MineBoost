package jp.mineboost;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;

import java.util.ArrayList;
import java.util.List;
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

    /** Blocks the server insta-mined only because of the boost; effects are sent once confirmed. */
    private record InstaBreak(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state) {}
    private static final List<InstaBreak> INSTA_BREAKS = new ArrayList<>();

    /**
     * With a high multiplier the boosted progress reaches 1 on the very first tick, so vanilla
     * breaks the block while handling START ("insta mine"), but the unboosted client did not
     * predict that break. Vanilla then (1) skips the break sound/particles for the breaker and
     * (2) leaves destroyPos on an older block, so the client's later ABORT for this block logs
     * "Mismatch in destroy block pos".
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || player.isCreative()
                || !Settings.speedEnabled(player)) return;
        BlockPos pos = event.getPos().immutable();
        BlockState state = event.getState();
        ServerLevel level = player.serverLevel();
        float boosted = state.getDestroyProgress(player, level, pos);
        if (boosted >= 1.0F && boosted / Settings.SPEED.get() < 1.0) {
            player.gameMode.destroyPos = pos;
            INSTA_BREAKS.add(new InstaBreak(player, level, pos, state));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (InstaBreak b : INSTA_BREAKS) {
            // Only if the break actually happened (not cancelled after our listener).
            if (!b.player().isRemoved() && b.level().getBlockState(b.pos()) != b.state()) {
                sendBreakEffects(b.player(), b.pos(), b.state());
            }
        }
        INSTA_BREAKS.clear();
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
        if (mode.destroyBlock(pos)) sendBreakEffects(player, pos, state);
        // Correct the local client even if it was still showing the original progress.
        player.connection.send(new ClientboundBlockUpdatePacket(level, pos));
    }

    /**
     * Vanilla sends the break sound/particles to everyone except the breaker, whose client
     * normally predicts them. A boosted break happens before the client predicts it.
     */
    private static void sendBreakEffects(ServerPlayer player, BlockPos pos, BlockState state) {
        player.connection.send(new ClientboundLevelEventPacket(
                LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state), false));
    }

    private static void clearProgress(ServerPlayer player, ServerPlayerGameMode mode, ServerLevel level, BlockPos pos) {
        mode.isDestroyingBlock = false;
        mode.lastSentState = -1;
        level.destroyBlockProgress(player.getId(), pos, -1);
    }
}
