package com.leonardobishop.foodexpiry.command;

import com.leonardobishop.foodexpiry.FoodExpiryPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FoodExpiryCommand implements CommandExecutor {

    private final FoodExpiryPlugin plugin;

    public FoodExpiryCommand(FoodExpiryPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.reloadPluginConfiguration();
        sender.sendMessage(ChatColor.GRAY + "FoodExpiry reloaded.");
        return true;
    }
}
