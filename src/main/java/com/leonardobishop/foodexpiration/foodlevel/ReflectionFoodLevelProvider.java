package com.leonardobishop.foodexpiration.foodlevel;

import com.leonardobishop.foodexpiration.FoodExpirationPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ReflectionFoodLevelProvider implements FoodLevelProvider {

    private HashMap<Material, Integer> nutritionValueCache = new HashMap<>();

    private FoodExpirationPlugin plugin;
    private String version;
    private boolean enabled;
    private Method asNMSCopyMethod;
    private Method getItemMethod;
    private Method getFoodInfoMethod;
    private Method getNutritionMethod;

    public ReflectionFoodLevelProvider(FoodExpirationPlugin plugin) {
        this.plugin = plugin;
        enabled = true;
        try {
            // this shit actually works lol
            version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            plugin.getLogger().info("Your server is running version " + version + ".");
            Class craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
            asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Class nmsItemStackClass = Class.forName("net.minecraft.server." + version + ".ItemStack");
            getItemMethod = nmsItemStackClass.getMethod("getItem");
            Class nmsItemClass = Class.forName("net.minecraft.server." + version + ".Item");
            getFoodInfoMethod = nmsItemClass.getMethod("getFoodInfo");
            Class nmsFoodInfoClass = Class.forName("net.minecraft.server." + version + ".FoodInfo");
            getNutritionMethod = nmsFoodInfoClass.getMethod("getNutrition");
            plugin.getLogger().info("ReflectionFoodLevelProvider initialised with version specific mappings.");
        } catch (ClassNotFoundException e) {
            try {
                Class craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
                asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
                Class nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
                getItemMethod = nmsItemStackClass.getMethod("getItem");
                Class nmsItemClass = Class.forName("net.minecraft.world.item.Item");
                getFoodInfoMethod = nmsItemClass.getMethod("getFoodInfo");
                Class nmsFoodInfoClass = Class.forName("net.minecraft.world.food.FoodInfo");
                getNutritionMethod = nmsFoodInfoClass.getMethod("getNutrition");
                plugin.getLogger().info("ReflectionFoodLevelProvider initialised with new mappings.");
            } catch (NoSuchMethodException | ClassNotFoundException | ArrayIndexOutOfBoundsException ex) {
                plugin.getLogger().severe("Failed to initialise food level provider!");
                enabled = false;
                ex.printStackTrace();
            }
        } catch (NoSuchMethodException | ArrayIndexOutOfBoundsException e) {
            plugin.getLogger().severe("Failed to initialise food level provider!");
            enabled = false;
            e.printStackTrace();
        }
    }

    @Override
    public int getFoodLevel(ItemStack itemStack) {
        if (!enabled) return 0;
        if (!itemStack.getType().isEdible()) throw new RuntimeException("Cannot get food level of inedible item!");

        return nutritionValueCache.computeIfAbsent(itemStack.getType(), type -> {
            try {
                Object nmsItemStack = asNMSCopyMethod.invoke(null, itemStack);
                Object nmsItem = getItemMethod.invoke(nmsItemStack);
                Object nmsFoodInfo = getFoodInfoMethod.invoke(nmsItem);
                if (nmsFoodInfo != null) {
                    return (int) getNutritionMethod.invoke(nmsFoodInfo);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                plugin.getLogger().severe("Failed to obtain food level from item!");
                e.printStackTrace();
            }
            return 0;
        });
    }
}
