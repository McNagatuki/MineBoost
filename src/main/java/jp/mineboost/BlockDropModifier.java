package jp.mineboost;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public final class BlockDropModifier extends LootModifier {
    /** Safety net for blocks that must never be multiplied, even if placement tracking misses them. */
    static final TagKey<Block> NO_BOOST = TagKey.create(Registries.BLOCK, new ResourceLocation(MineBoost.ID, "no_boost"));
    public static final Codec<BlockDropModifier> CODEC = RecordCodecBuilder.create(
            instance -> codecStart(instance).apply(instance, BlockDropModifier::new));

    public BlockDropModifier(LootItemCondition[] conditions) { super(conditions); }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!context.hasParam(LootContextParams.BLOCK_STATE)
                || !(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer player)
                || player.isCreative() || player.isSpectator() || !Settings.dropEnabled(player)) {
            return generatedLoot;
        }
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
        // Never boost: blocks in the safety-net tag, or player-placed blocks (except grown crops).
        if (state.is(NO_BOOST) || origin == null) return generatedLoot;
        if (PlacedBlocks.isPlaced(context.getLevel(), BlockPos.containing(origin))
                && !PlacedBlocks.isMatureCrop(state)) {
            return generatedLoot;
        }
        double multiplier = Settings.DROP.get();
        ObjectArrayList<ItemStack> result = new ObjectArrayList<>();
        for (ItemStack stack : generatedLoot) {
            if (stack.isEmpty()) continue;
            // Do not duplicate embedded block-entity inventory/NBT (e.g. filled shulker boxes).
            if (stack.getTagElement("BlockEntityTag") != null) {
                result.add(stack);
                continue;
            }
            double exact = stack.getCount() * multiplier;
            long count = (long) Math.floor(exact);
            double fraction = exact - count;
            if (fraction > 0 && context.getRandom().nextDouble() < fraction) count++;
            // Split large amounts according to item-specific maximum stack size.
            int max = Math.max(1, stack.getMaxStackSize());
            while (count > 0) {
                int part = (int) Math.min(count, max);
                ItemStack copy = stack.copy();
                copy.setCount(part);
                result.add(copy);
                count -= part;
            }
        }
        return result;
    }

    @Override
    public Codec<BlockDropModifier> codec() { return CODEC; }
}
