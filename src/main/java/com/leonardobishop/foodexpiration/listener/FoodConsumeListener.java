package com.leonardobishop.foodexpiration.listener;

import com.leonardobishop.foodexpiration.FoodExpirationPlugin;
import com.leonardobishop.foodexpiration.expiration.ExpirationStage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

public class FoodConsumeListener implements Listener {

    private final FoodExpirationPlugin plugin;

    public FoodConsumeListener(FoodExpirationPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodConsume(FoodLevelChangeEvent event) {
        if (event.getItem() == null) return;
        ItemFood
        ItemMeta itemMeta = event.getItem().getItemMeta();
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();

        if (!persistentDataContainer.has(FoodExpirationPlugin.PRODUCTION_NAMESPACED_KEY, PersistentDataType.LONG)) return;

        long time = persistentDataContainer.get(FoodExpirationPlugin.PRODUCTION_NAMESPACED_KEY, PersistentDataType.LONG);
        ExpirationStage stage = plugin.getExpirationStages().getStageOf(System.currentTimeMillis() - time);

        //TODO modify food

        for (PotionEffect potionEffect : stage.getPotionEffects()) {
            potionEffect.apply(event.getPlayer());
        }
    }
}
