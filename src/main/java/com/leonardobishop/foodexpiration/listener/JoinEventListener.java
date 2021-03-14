package com.leonardobishop.foodexpiry.listener;

import com.leonardobishop.foodexpiry.FoodExpiryPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEventListener implements Listener {

    private final FoodExpiryPlugin plugin;

    public JoinEventListener(FoodExpiryPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.applyFoodDescriptorsInventory(event.getPlayer());
    }

}
