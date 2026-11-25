package jp.mineboost;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public final class BlockDropModifier extends LootModifier {
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
