package com.enderio.core.common.handlers;

import java.util.Map;

import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.enchant.EnchantAutoSmelt;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Handler
public class AutoSmeltHandler {

  @SubscribeEvent
  public void handleBlockBreak(BlockEvent.HarvestDropsEvent event) {
    if (!event.world.isRemote && event.harvester != null) {
      ItemStack held = event.harvester.getCurrentEquippedItem();
      if (held != null) {
        int level = getAutoSmeltLevel(held);
        int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, held);

        if (level >= 0) {
          for (int i = 0; i < event.drops.size(); i++) {
            {
              ItemStack stack = event.drops.get(i);
              if (stack != null && !event.isSilkTouching) {
                if (FurnaceRecipes.instance().getSmeltingResult(stack) != null) {
                  ItemStack furnaceStack = FurnaceRecipes.instance().getSmeltingResult(stack).copy();
                  //Fortune stuffs
                  if (fortune > 0 && ConfigHandler.allowAutoSmeltWithFortune)
                    furnaceStack.stackSize *= (event.world.rand.nextInt(fortune + 1) + 1);

                  event.drops.set(i, furnaceStack);

                  //XP (adapted vanilla code)
                  int xp = furnaceStack.stackSize;
                  float f = FurnaceRecipes.instance().getSmeltingExperience(furnaceStack);
                  int j;

                  if (f == 0.0F) {
                    xp = 0;
                  } else if (f < 1.0F) {
                    j = MathHelper.floor_float((float) xp * f);

                    if (j < MathHelper.ceiling_float_int((float) xp * f) && (float) Math.random() < (float) xp * f - (float) j) {
                      ++j;
                    }

                    xp = j;
                  }

                  while (xp > 0) {
                    j = EntityXPOrb.getXPSplit(xp);
                    xp -= j;
                    event.world.spawnEntityInWorld(new EntityXPOrb(event.world, event.pos.getX(), event.pos.getY() + 0.5, event.pos.getZ(), j));
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private int getAutoSmeltLevel(ItemStack tool) {
    if (tool == null) {
      return -1;
    }

    Map<Integer, Integer> enchants = EnchantmentHelper.getEnchantments(tool);
    for (int i : enchants.keySet()) {
      Enchantment enchant = Enchantment.getEnchantmentById(i);
      if (enchant == EnchantAutoSmelt.instance()) {
        return enchants.get(i);
      }
    }
    return -1;
  }
}
