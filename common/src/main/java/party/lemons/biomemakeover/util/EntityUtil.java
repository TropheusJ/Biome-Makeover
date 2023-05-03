package party.lemons.biomemakeover.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.apache.commons.lang3.mutable.MutableInt;
import party.lemons.biomemakeover.init.BMEntities;

import java.util.Collection;

public final class EntityUtil
{
    public static void applyProjectileResistance(Iterable<ItemStack> equipment, MutableInt resistance)
    {
        MutableInt slotIndex = new MutableInt(0);
        equipment.forEach(e->{
            if(!e.isEmpty())
            {
                EquipmentSlot slot = EquipmentSlot.values()[2 + slotIndex.getValue()];
                if(e.getAttributeModifiers(slot).containsKey(BMEntities.ATT_PROJECTILE_RESISTANCE.get()))
                {
                    Collection<AttributeModifier> modifiers = e.getAttributeModifiers(slot).get(BMEntities.ATT_PROJECTILE_RESISTANCE.get());
                    for(AttributeModifier mod : modifiers)
                    {
                        resistance.add(mod.getAmount());
                    }
                }
            }
            slotIndex.add(1);
        });
    }

    public static double getProjectileResistance(LivingEntity e)
    {
        double res = 0;
        for(EquipmentSlot slot : EquipmentSlot.values())
        {
            ItemStack st = e.getItemBySlot(slot);
            if(!st.isEmpty() && st.getAttributeModifiers(slot).containsKey(BMEntities.ATT_PROJECTILE_RESISTANCE.get()))
            {
                Collection<AttributeModifier> modifiers = st.getAttributeModifiers(slot).get(BMEntities.ATT_PROJECTILE_RESISTANCE.get());
                for(AttributeModifier mod : modifiers)
                {
                    res += mod.getAmount();
                }
            }
        }
        return res;
    }

    private EntityUtil()
    {
    }

    public static boolean attemptProjectileResistanceBlock(LivingEntity entity, DamageSource source)
    {
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            double protection = EntityUtil.getProjectileResistance(entity);
            if (protection > 0D && (entity.getRandom().nextDouble() * 30D) < protection) {
                entity.playSound(SoundEvents.SHIELD_BLOCK, 1F, 0.8F + entity.getRandom().nextFloat() * 0.4F);
                return true;
            }
        }

        return false;
    }

    public static void scatterItemStack(Entity e, ItemStack stack)
    {
        Containers.dropItemStack(e.level, e.getX(), e.getY(), e.getZ(), stack);
    }

    public static void dropFromLootTable(LivingEntity living, ResourceLocation table, LootContext.Builder context) {
        LootTable lootTable = living.getLevel().getServer().getLootTables().get(table);
        lootTable.getRandomItems(context.create(LootContextParamSets.ENTITY), living::spawnAtLocation);
    }

    public static void dropFromLootTable(LivingEntity living, ResourceLocation table)
    {
        LootContext.Builder context = new LootContext.Builder((ServerLevel)living.level)
                .withRandom(living.getRandom())
                .withParameter(LootContextParams.THIS_ENTITY, living)
                .withParameter(LootContextParams.ORIGIN, living.position());

        if (living.getLastAttacker() != null && living.getLastAttacker() instanceof Player attackingPlayer) {
            context = context.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, attackingPlayer).withLuck(attackingPlayer.getLuck());
        }

        dropFromLootTable(living, table, context);
    }
}
