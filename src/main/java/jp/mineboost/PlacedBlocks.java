package jp.mineboost;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Remembers which block positions hold a block placed by a player/entity, so their drops
 * are not multiplied (place-and-break duplication). Stored per chunk, saved with the chunk.
 * When in doubt the position stays marked: a stale mark only costs a boost, never dupes items.
 */
public final class PlacedBlocks {
    private PlacedBlocks() {}

    public static final Capability<Store> CAP = CapabilityManager.get(new CapabilityToken<>() {});
    private static final ResourceLocation KEY = new ResourceLocation(MineBoost.ID, "placed_blocks");
    private static final String FALLING_FLAG = MineBoost.ID + ":placed";

    /** Marked positions to unmark once they turn into air (after the break/explosion finishes). */
    private static final Map<ResourceKey<Level>, LongOpenHashSet> PENDING = new HashMap<>();
    /** Piston moves computed in Pre, applied in Post only if the blocks actually moved. */
    private static final Map<PistonKey, PistonPlan> PISTON_PLANS = new HashMap<>();

    private record PistonKey(ResourceKey<Level> dimension, long pos) {}
    private record PistonPlan(Direction moveDir, List<BlockPos> markedSources) {}

    public static final class Store {
        final LongOpenHashSet positions = new LongOpenHashSet();
    }

    private static final class Provider implements ICapabilitySerializable<LongArrayTag> {
        private final Store store = new Store();
        private final LazyOptional<Store> optional = LazyOptional.of(() -> store);

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return CAP.orEmpty(cap, optional);
        }

        @Override
        public LongArrayTag serializeNBT() { return new LongArrayTag(store.positions.toLongArray()); }

        @Override
        public void deserializeNBT(LongArrayTag tag) {
            store.positions.clear();
            for (long pos : tag.getAsLongArray()) store.positions.add(pos);
        }
    }

    // ---- API ----

    public static boolean isPlaced(Level level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return false;
        return level.getChunkAt(pos).getCapability(CAP)
                .map(store -> store.positions.contains(pos.asLong())).orElse(false);
    }

    /** Fully grown crops are boosted even when planted by a player: growth takes time/bone meal. */
    public static boolean isMatureCrop(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock crop) return crop.isMaxAge(state);
        if (block instanceof NetherWartBlock) return state.getValue(NetherWartBlock.AGE) >= 3;
        if (block instanceof CocoaBlock) return state.getValue(CocoaBlock.AGE) >= 2;
        if (block instanceof SweetBerryBushBlock) return state.getValue(SweetBerryBushBlock.AGE) >= 3;
        return false;
    }

    private static void set(Level level, BlockPos pos, boolean placed) {
        if (level.isClientSide() || !level.hasChunkAt(pos)) return;
        LevelChunk chunk = level.getChunkAt(pos);
        chunk.getCapability(CAP).ifPresent(store -> {
            boolean changed = placed ? store.positions.add(pos.asLong()) : store.positions.remove(pos.asLong());
            if (changed) chunk.setUnsaved(true);
        });
    }

    private static void mark(Level level, BlockPos pos) { set(level, pos, true); }

    private static void queueCheck(Level level, BlockPos pos) {
        PENDING.computeIfAbsent(level.dimension(), k -> new LongOpenHashSet()).add(pos.asLong());
    }

    // ---- Events (mod bus) ----

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(Store.class);
    }

    // ---- Events (Forge bus) ----

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<LevelChunk> event) {
        if (!event.getObject().getLevel().isClientSide()) event.addCapability(KEY, new Provider());
    }

    /** Players, endermen, fake players etc. Also receives EntityMultiPlaceEvent (beds, doors). */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level)) return;
        if (event instanceof BlockEvent.EntityMultiPlaceEvent multi) {
            for (BlockSnapshot snapshot : multi.getReplacedBlockSnapshots()) mark(level, snapshot.getPos());
        }
        mark(level, event.getPos());
    }

    /** Loot is computed after BreakEvent, so only queue the unmark for the end of the tick. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof Level level && isPlaced(level, event.getPos())) {
            queueCheck(level, event.getPos());
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();
        for (BlockPos pos : event.getAffectedBlocks()) {
            if (isPlaced(level, pos)) queueCheck(level, pos);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPistonPre(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide()) return;
        PistonStructureResolver resolver = event.getStructureHelper();
        if (resolver == null || !resolver.resolve()) return;
        for (BlockPos pos : resolver.getToDestroy()) {
            if (isPlaced(level, pos)) queueCheck(level, pos);
        }
        List<BlockPos> marked = new ArrayList<>();
        for (BlockPos pos : resolver.getToPush()) {
            if (isPlaced(level, pos)) marked.add(pos.immutable());
        }
        if (marked.isEmpty()) return;
        Direction moveDir = event.getPistonMoveType().isExtend ? event.getDirection() : event.getDirection().getOpposite();
        PISTON_PLANS.put(new PistonKey(level.dimension(), event.getPos().asLong()), new PistonPlan(moveDir, marked));
    }

    @SubscribeEvent
    public static void onPistonPost(PistonEvent.Post event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide()) return;
        PistonPlan plan = PISTON_PLANS.remove(new PistonKey(level.dimension(), event.getPos().asLong()));
        if (plan == null) return;
        // Vanilla may still skip the move (e.g. non-sticky retract); only blocks now in
        // transit (moving_piston at the destination) actually moved.
        List<BlockPos> moved = new ArrayList<>();
        for (BlockPos src : plan.markedSources()) {
            if (level.getBlockState(src.relative(plan.moveDir())).is(Blocks.MOVING_PISTON)) moved.add(src);
        }
        for (BlockPos src : moved) set(level, src, false);
        for (BlockPos src : moved) mark(level, src.relative(plan.moveDir()));
    }

    /** A placed gravity block (sand, anvil...) starts falling: remember it on the entity. */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || event.getLevel().isClientSide()
                || !(event.getEntity() instanceof FallingBlockEntity falling)) return;
        BlockPos origin = falling.blockPosition();
        if (isPlaced(event.getLevel(), origin)) {
            falling.getPersistentData().putBoolean(FALLING_FLAG, true);
            queueCheck(event.getLevel(), origin);
        }
    }

    /** ...and mark where it lands. */
    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()
                || !(event.getEntity() instanceof FallingBlockEntity falling)
                || falling.getRemovalReason() != Entity.RemovalReason.DISCARDED
                || !falling.getPersistentData().getBoolean(FALLING_FLAG)) return;
        BlockPos pos = falling.blockPosition();
        if (!event.getLevel().getBlockState(pos).isAir()) mark(event.getLevel(), pos);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        PISTON_PLANS.clear(); // plans whose Post never fired (move failed)
        if (PENDING.isEmpty()) return;
        Map<ResourceKey<Level>, LongOpenHashSet> batch = new HashMap<>(PENDING);
        PENDING.clear();
        batch.forEach((dimension, positions) -> {
            ServerLevel level = event.getServer().getLevel(dimension);
            if (level == null) return;
            for (long packed : positions) {
                BlockPos pos = BlockPos.of(packed);
                if (!level.hasChunkAt(pos)) continue;
                BlockState state = level.getBlockState(pos);
                if (state.is(Blocks.MOVING_PISTON)) queueCheck(level, pos); // still moving; retry
                else if (state.isAir()) set(level, pos, false);
            }
        });
    }
}
