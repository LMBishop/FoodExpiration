package com.leonardobishop.foodexpiration.listener;

import com.leonardobishop.foodexpiration.FoodExpirationPlugin;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class InventoryModificationListener implements Listener {

    private final FoodExpirationPlugin plugin;

    public InventoryModificationListener(FoodExpirationPlugin plugin) {
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
        if (!plugin.getConfiguration().getBooleanValue("allow-mixing")) return;

        if (event.getClickedInventory() != null
                && event.getClickedInventory().getType() == InventoryType.PLAYER
                && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR
                && event.getCursor() != null && event.getCursor().getType() != Material.AIR
                && event.getCursor().getType() == event.getCurrentItem().getType()) {
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();
            ItemStack over = event.getCursor();
            ItemStack held = event.getCurrentItem();

            if (!over.getItemMeta().getPersistentDataContainer().has(FoodExpirationPlugin.PRODUCTION_NAMESPACED_KEY, PersistentDataType.LONG)
                    || !held.getItemMeta().getPersistentDataContainer().has(FoodExpirationPlugin.PRODUCTION_NAMESPACED_KEY, PersistentDataType.LONG)) {
                return;
            }

            long overDate = over.getItemMeta().getPersistentDataContainer().get(FoodExpirationPlugin.PRODUCTION_NAMESPACED_KEY, PersistentDataType.LONG);
            long heldDate = held.getItemMeta().getPersistentDataContainer().get(FoodExpirationPlugin.PRODUCTION_NAMESPACED_KEY, PersistentDataType.LONG);

            if (overDate == heldDate) {
                return;
            }

            // comprehending this hurts my head but it works
            event.setCancelled(true);

            ItemStack transferringFrom;
            ItemStack transferringTo;

            if (overDate < heldDate) {
                transferringFrom = held;
                transferringTo = over;
            } else {
                transferringFrom = over;
                transferringTo = held;
            }

            int newAmount = transferringTo.getAmount() + transferringFrom.getAmount();
            if (newAmount > transferringTo.getMaxStackSize()) {
                int difference = newAmount - transferringTo.getMaxStackSize();
                transferringTo.setAmount(transferringTo.getMaxStackSize());
                transferringFrom.setAmount(difference);
            } else {
                transferringTo.setAmount(newAmount);
                transferringFrom.setAmount(0);
            }

            event.setCurrentItem(transferringTo);
            event.setCursor(transferringFrom.getAmount() == 0 ? null : transferringFrom);

            player.updateInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3, 2);
        }
    }

}
