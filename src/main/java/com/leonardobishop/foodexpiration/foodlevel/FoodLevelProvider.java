package com.leonardobishop.foodexpiration.foodlevel;

import org.bukkit.inventory.ItemStack;

/**
 * The food level provider returns the nutrition value from an ItemStack.
 */
public interface FoodLevelProvider {

    int getFoodLevel(ItemStack itemStack);

}
