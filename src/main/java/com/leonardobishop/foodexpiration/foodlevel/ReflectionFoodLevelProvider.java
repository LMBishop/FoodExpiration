package com.leonardobishop.foodexpiration.foodlevel;

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class ReflectionFoodLevelProvider implements FoodLevelProvider {

    private Class craftItemStackClass;
    private Method asNMSCopyMethod;

    public ReflectionFoodLevelProvider() {
        try {
            craftItemStackClass = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
            asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);

            //TODO fix this
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getFoodLevel(ItemStack itemStack) {
        return 0;
    }
}
