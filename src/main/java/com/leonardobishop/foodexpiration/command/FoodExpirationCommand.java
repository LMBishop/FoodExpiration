package com.leonardobishop.foodexpiration.command;

import com.leonardobishop.foodexpiration.FoodExpirationPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FoodExpirationCommand implements CommandExecutor {

    private final FoodExpirationPlugin plugin;

    public FoodExpirationCommand(FoodExpirationPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.reloadPluginConfiguration();
        sender.sendMessage(ChatColor.GRAY + "FoodExpiration reloaded.");
        return true;
    }
}
