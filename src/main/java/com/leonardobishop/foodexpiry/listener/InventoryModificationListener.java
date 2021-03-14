package com.leonardobishop.foodexpiry.listener;

import com.leonardobishop.foodexpiry.FoodExpiryPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryModificationListener implements Listener {

    private final FoodExpiryPlugin plugin;

    public InventoryModificationListener(FoodExpiryPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            plugin.applyFoodDescriptor(event.getItem().getItemStack());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClickItem(InventoryClickEvent event) {
        if (event.getClickedInventory() != null
                && event.getClickedInventory().getType() == InventoryType.PLAYER
                && event.getCurrentItem() != null) {
            plugin.applyFoodDescriptor(event.getCurrentItem());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMergeItem(InventoryClickEvent event) {
        if (event.getClickedInventory() != null
                && event.getClickedInventory().getType() == InventoryType.PLAYER
                && event.getCurrentItem() != null
                && event.getCursor() != null
                && event.getCursor().getType() == event.getCurrentItem().getType()) {

            plugin.applyFoodDescriptor(event.getCurrentItem());
        }
    }

}
