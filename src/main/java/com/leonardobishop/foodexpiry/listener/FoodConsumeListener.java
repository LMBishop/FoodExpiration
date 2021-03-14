package com.leonardobishop.foodexpiry.listener;

import com.leonardobishop.foodexpiry.FoodExpiryPlugin;
import com.leonardobishop.foodexpiry.expiration.ExpirationStage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

public class FoodConsumeListener implements Listener {

    private final FoodExpiryPlugin plugin;

    public FoodConsumeListener(FoodExpiryPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodConsume(PlayerItemConsumeEvent event) {
        ItemMeta itemMeta = event.getItem().getItemMeta();
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();

        if (!persistentDataContainer.has(FoodExpiryPlugin.PRODUCTION_NAMESPACED_KEY, PersistentDataType.LONG)) return;

        long time = persistentDataContainer.get(FoodExpiryPlugin.PRODUCTION_NAMESPACED_KEY, PersistentDataType.LONG);
        ExpirationStage stage = plugin.getExpirationStages().getStageOf(System.currentTimeMillis() - time);

        //TODO modify food

        for (PotionEffect potionEffect : stage.getPotionEffects()) {
            potionEffect.apply(event.getPlayer());
        }
    }
}
