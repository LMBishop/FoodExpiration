package com.leonardobishop.foodexpiration.command;

import com.leonardobishop.foodexpiration.FoodExpirationPlugin;
import com.leonardobishop.foodexpiration.expiration.ExpirationStage;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FoodExpirationCommand implements TabExecutor {

    private final FoodExpirationPlugin plugin;
    private final List<String> options = Arrays.asList("dump");

    public FoodExpirationCommand(FoodExpirationPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.reloadPluginConfiguration();
        if (args.length > 0 && args[0].equalsIgnoreCase("dump")) {
            sender.sendMessage(ChatColor.GRAY + "Expiration stages:");
            int i = 0;
            for (ExpirationStage expirationStage : plugin.getExpirationStages().getStages()) {
                sender.sendMessage(ChatColor.RED.toString() + ChatColor.UNDERLINE + i + ChatColor.GRAY + " " + expirationStage.toString());
                i++;
            }
            return true;
        }
        sender.sendMessage(ChatColor.GRAY + "FoodExpiration was reloaded.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], options, completions);
            Collections.sort(completions);
            return completions;
        }
        return null;
    }
}
