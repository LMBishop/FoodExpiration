package com.leonardobishop.foodexpiration;

import com.leonardobishop.foodexpiration.command.FoodExpirationCommand;
import com.leonardobishop.foodexpiration.expiration.ExpirationStage;
import com.leonardobishop.foodexpiration.expiration.ExpirationStageRegister;
import com.leonardobishop.foodexpiration.listener.FoodConsumeListener;
import com.leonardobishop.foodexpiration.listener.InventoryModificationListener;
import com.leonardobishop.foodexpiration.listener.JoinEventListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FoodExpirationPlugin extends JavaPlugin {

    public static NamespacedKey PRODUCTION_NAMESPACED_KEY;

    private ExpirationStageRegister expirationStageRegister;

    @Override
    public void onEnable() {
        PRODUCTION_NAMESPACED_KEY = new NamespacedKey(this, "production-date");

        this.expirationStageRegister = new ExpirationStageRegister();

        File directory = new File(String.valueOf(this.getDataFolder()));
        if (!directory.exists() && !directory.isDirectory()) {
            directory.mkdir();
        }

        File config = new File(this.getDataFolder() + File.separator + "config.yml");
        try {
            if (config.createNewFile()) {
                try (OutputStream out = new FileOutputStream(config); InputStream in = FoodExpirationPlugin.class.getClassLoader().getResourceAsStream("config.yml")) {
                    byte[] buffer = new byte[1024];
                    int length = in.read(buffer);
                    while (length != -1) {
                        out.write(buffer, 0, length);
                        length = in.read(buffer);
                    }
                }
            }
        } catch (IOException e) {
            super.getLogger().severe("Failed to create default config.");
            e.printStackTrace();
            super.getLogger().severe(ChatColor.RED + "...please delete the FoodExpiry directory and try RESTARTING (/not/ reloading).");
        }

        super.getServer().getPluginManager().registerEvents(new JoinEventListener(this), this);
        super.getServer().getPluginManager().registerEvents(new InventoryModificationListener(this), this);
        super.getServer().getPluginManager().registerEvents(new FoodConsumeListener(this), this);

        super.getServer().getPluginCommand("foodexpiration").setExecutor(new FoodExpirationCommand(this));

        super.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                applyFoodDescriptorsInventory(player);
                //TODO change
            }
        }, 1L, 1L);
        super.getServer().getScheduler().scheduleSyncDelayedTask(this, this::reloadPluginConfiguration);

    }

    /**
     * Get the {@link com.leonardobishop.foodexpiration.expiration.ExpirationStageRegister},
     * which contains all {@link com.leonardobishop.foodexpiration.expiration.ExpirationStage}.
     *
     * @return The expiration stage register
     */
    public ExpirationStageRegister getExpirationStages() {
        return expirationStageRegister;
    }

    /**
     * Apply food descriptors to every item in the Player's inventory. Internally calls
     * {@link FoodExpirationPlugin#applyFoodDescriptor(ItemStack)} for every item in their Inventory.
     *
     * @param player the player to apply food descriptors to
     */
    public void applyFoodDescriptorsInventory(Player player) {
        for (ItemStack is : player.getInventory()) {
            applyFoodDescriptor(is);
        }
    }

    /**
     * Apply food descriptors to the ItemStack. This assigns the value in the PersistentDataContainer if it
     * does not already exist to the current system time, and modifies the lore of
     * the ItemStack to reflect the {@link com.leonardobishop.foodexpiration.expiration.ExpirationStage} the food item is at.
     *
     * @param is the itemstack to apply to
     */
    public void applyFoodDescriptor(ItemStack is) {
        if (is == null) return;
        if (expirationStageRegister.isAcceptingRegistrations()) return;

        if (is.getType().isEdible()) {
            ItemMeta itemMeta = is.getItemMeta();
            PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
            long time;

            if (!persistentDataContainer.has(FoodExpirationPlugin.PRODUCTION_NAMESPACED_KEY, PersistentDataType.LONG)) {
                time = System.currentTimeMillis();
                persistentDataContainer.set(FoodExpirationPlugin.PRODUCTION_NAMESPACED_KEY, PersistentDataType.LONG, time);
            } else {
                time = persistentDataContainer.get(FoodExpirationPlugin.PRODUCTION_NAMESPACED_KEY, PersistentDataType.LONG);
            }

            ExpirationStage stage = expirationStageRegister.getStageOf(System.currentTimeMillis() - time);

            List<String> lore = new ArrayList<>(Collections.singletonList(ChatColor.GRAY + stage.getName()));

            itemMeta.setLore(lore);
            is.setItemMeta(itemMeta);
        }
    }

    public void reloadPluginConfiguration() {
        this.reloadConfig();
        expirationStageRegister.clearRegistrations();

        if (this.getConfig().contains("expiry-stages")) {
            List<Map> stages = (List<Map>) this.getConfig().getList("expiry-stages");
            for (Map stage : stages) {
                String name = (String) stage.get("name");
                if (name == null) {
                    this.getLogger().warning("An expiry stage in config.yml has no defined name. The stage will be ignored.");
                    continue;
                }

                Map after = (Map) stage.get("after");
                long time = (long) (int) after.get("time");
                String namedTimeUnit = (String) after.get("unit");
                TimeUnit timeUnit;
                try {
                    timeUnit = TimeUnit.valueOf(namedTimeUnit);
                } catch (IllegalArgumentException ignored) {
                    this.getLogger().warning("An expiry stage in config.yml has an invalid time unit. The stage will be ignored. The valid values are: " + Arrays.toString(TimeUnit.values()));
                    continue;
                }

                Object hungerModifier = stage.get("hunger-modifier");
                List<PotionEffect> effects = new ArrayList<>();

                if (stage.containsKey("effects")) {
                    for (Map effect : (List<Map>) stage.get("effects")) {
                        String type = (String) effect.get("type");
                        int duration = (int) effect.get("duration");
                        int amplifier = (int) effect.get("amplifier");

                        if (type == null) continue;

                        PotionEffectType potionEffectType = PotionEffectType.getByName(type);
                        if (potionEffectType == null) {
                            this.getLogger().warning("An expiry stage has an invalid potion effect type '" + type + "'.  The valid values are: " + Arrays.toString(PotionEffectType.values()));
                            continue;
                        }

                        PotionEffect potionEffect = new PotionEffect(potionEffectType, duration, amplifier);
                        effects.add(potionEffect);
                    }
                }

                ExpirationStage expirationStage = new ExpirationStage(name, timeUnit, time, effects);
                expirationStage.setHungerModifier(hungerModifier instanceof Double ? (double) hungerModifier : (int) hungerModifier);

                expirationStageRegister.register(expirationStage);
            }
        }

        expirationStageRegister.finaliseRegistrations();
    }

}
