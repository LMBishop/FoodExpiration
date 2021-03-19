package com.leonardobishop.foodexpiration.foodlevel;

import org.bukkit.inventory.ItemStack;

public interface FoodLevelProvider {

    int getFoodLevel(ItemStack itemStack);

}
