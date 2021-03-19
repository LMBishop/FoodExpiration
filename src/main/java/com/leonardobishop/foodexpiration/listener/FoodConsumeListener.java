package com.leonardobishop.foodexpiration.listener;

import com.leonardobishop.foodexpiration.FoodExpirationPlugin;
import com.leonardobishop.foodexpiration.expiration.ExpirationStage;
import com.leonardobishop.foodexpiration.expiration.PotionEffectWrapper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

import java.util.concurrent.ThreadLocalRandom;

public class FoodConsumeListener implements Listener {

    private final FoodExpirationPlugin plugin;

    public FoodConsumeListener(FoodExpirationPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodConsume(FoodLevelChangeEvent event) {
        if (event.getItem() == null || !event.getItem().getType().isEdible()) return;

        ItemMeta itemMeta = event.getItem().getItemMeta();
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();

        if (!persistentDataContainer.has(FoodExpirationPlugin.PRODUCTION_NAMESPACED_KEY, PersistentDataType.LONG)) return;

        long time = persistentDataContainer.get(FoodExpirationPlugin.PRODUCTION_NAMESPACED_KEY, PersistentDataType.LONG);
        ExpirationStage stage = plugin.getExpirationStages().getStageOf(System.currentTimeMillis() - time);

        int modifiedFoodLevel = (int) ((double) plugin.getFoodLevelProvider().getFoodLevel(event.getItem()) * stage.getHungerModifier());
        int newPlayerFoodLevel = event.getEntity().getFoodLevel() + modifiedFoodLevel;
        event.setFoodLevel(Math.min(newPlayerFoodLevel, 20));

        for (PotionEffectWrapper potionEffect : stage.getPotionEffects()) {
            if (potionEffect.getChance() >= 1 || ThreadLocalRandom.current().nextDouble() < potionEffect.getChance()) {
                potionEffect.getPotionEffect().apply(event.getEntity());
            }
        }
    }
}
