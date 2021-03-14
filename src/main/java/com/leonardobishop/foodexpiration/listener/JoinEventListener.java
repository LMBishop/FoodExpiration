package com.leonardobishop.foodexpiration.listener;

import com.leonardobishop.foodexpiration.FoodExpirationPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEventListener implements Listener {

    private final FoodExpirationPlugin plugin;

    public JoinEventListener(FoodExpirationPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.applyFoodDescriptorsInventory(event.getPlayer());
    }

}
