package com.leonardobishop.foodexpiration;

import com.leonardobishop.foodexpiration.command.FoodExpirationCommand;
import com.leonardobishop.foodexpiration.expiration.ExpirationStage;
import com.leonardobishop.foodexpiration.expiration.ExpirationStageRegister;
import com.leonardobishop.foodexpiration.expiration.PotionEffectWrapper;
import com.leonardobishop.foodexpiration.foodlevel.FoodLevelProvider;
import com.leonardobishop.foodexpiration.foodlevel.ReflectionFoodLevelProvider;
import com.leonardobishop.foodexpiration.listener.FoodConsumeListener;
import com.leonardobishop.foodexpiration.listener.InventoryModificationListener;
import com.leonardobishop.foodexpiration.listener.JoinEventListener;
import org.apache.commons.lang.time.DateUtils;
import org.bstats.bukkit.MetricsLite;
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
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FoodExpirationPlugin extends JavaPlugin {

    public static NamespacedKey PRODUCTION_NAMESPACED_KEY;

    private ExpirationStageRegister expirationStageRegister;
    private FoodLevelProvider foodLevelProvider;
    private Configuration mainConfiguration;
    private int timeResolution;
    private BukkitTask refreshTask;

    @Override
    public void onEnable() {
        PRODUCTION_NAMESPACED_KEY = new NamespacedKey(this, "production-date");

        this.expirationStageRegister = new ExpirationStageRegister();
        this.foodLevelProvider = new ReflectionFoodLevelProvider();
        this.mainConfiguration = new Configuration();

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

        MetricsLite metrics = new MetricsLite(this, 10748);
        if (metrics.isEnabled()) {
            super.getLogger().info("Metrics started. This can be disabled at /plugins/bStats/config.yml.");
        }

        super.getServer().getPluginManager().registerEvents(new JoinEventListener(this), this);
        super.getServer().getPluginManager().registerEvents(new InventoryModificationListener(this), this);
        super.getServer().getPluginManager().registerEvents(new FoodConsumeListener(this), this);

        super.getServer().getPluginCommand("foodexpiration").setExecutor(new FoodExpirationCommand(this));

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
     * Get the {@link FoodLevelProvider} which is currently enabled. This will be an instance of
     * {@link ReflectionFoodLevelProvider}, unless I have added new ones with direct access and
     * forgot to update this javadoc.
     *
     * @return the active food level provider
     */
    public FoodLevelProvider getFoodLevelProvider() {
        return foodLevelProvider;
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
                if (mainConfiguration.getBooleanValue("rounding.enabled", true)) {
                    time = DateUtils.round(new Date(time), timeResolution).getTime();
                }
                persistentDataContainer.set(FoodExpirationPlugin.PRODUCTION_NAMESPACED_KEY, PersistentDataType.LONG, time);
            } else {
                time = persistentDataContainer.get(FoodExpirationPlugin.PRODUCTION_NAMESPACED_KEY, PersistentDataType.LONG);
            }

            ExpirationStage stage = expirationStageRegister.getStageOf(System.currentTimeMillis() - time);

            itemMeta.setLore(stage.asItemLore());
            is.setItemMeta(itemMeta);
        }
    }

    public void reloadPluginConfiguration() {
        this.reloadConfig();
        expirationStageRegister.clearRegistrations();
        mainConfiguration.loadConfig(this.getConfig());

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

                Object hungerModifier = stage.getOrDefault("hunger-modifier", 1);
                List<PotionEffectWrapper> effects = new ArrayList<>();

                Object description = stage.get("description");
                Object extendedDescription = stage.get("extended-description");

                if (stage.containsKey("effects")) {
                    for (Map effect : (List<Map>) stage.get("effects")) {
                        String type = (String) effect.get("type");
                        int duration = (int) effect.getOrDefault("duration", 0);
                        int amplifier = (int) effect.getOrDefault("amplifier", 0);
                        Object namedChance = effect.getOrDefault("chance", 1);
                        double chance = namedChance instanceof Integer ? (double) (int) namedChance : (double) namedChance;

                        if (type == null) continue;

                        PotionEffectType potionEffectType = PotionEffectType.getByName(type);
                        if (potionEffectType == null) {
                            this.getLogger().warning("An expiry stage has an invalid potion effect type '" + type + "'.  The valid values are: " + Arrays.toString(PotionEffectType.values()));
                            continue;
                        }

                        PotionEffect potionEffect = new PotionEffect(potionEffectType, duration, amplifier);
                        effects.add(new PotionEffectWrapper(potionEffect, chance));
                    }
                }

                ExpirationStage expirationStage = new ExpirationStage(name, timeUnit, time, effects);
                expirationStage.setHungerModifier(hungerModifier instanceof Double ? (double) hungerModifier : (int) hungerModifier);
                expirationStage.setDescription(description instanceof String ? (String) description : null);
                expirationStage.setExtendedDescription(extendedDescription instanceof List ? (List<String>) extendedDescription : null);

                expirationStageRegister.register(expirationStage);
            }
        }

        if (mainConfiguration.getBooleanValue("rounding.enabled", true)) {
            switch (mainConfiguration.getStringValue("rounding.time-resolution").toLowerCase()) {
                case "second": case "seconds":
                    timeResolution = Calendar.SECOND;
                    break;
                case "minute": case "minutes":
                    timeResolution = Calendar.MINUTE;
                    break;
                case "hour": case "hours":
                    timeResolution = Calendar.HOUR;
                    break;
                case "day": case "days":
                    timeResolution = Calendar.DAY_OF_MONTH;
                    break;
            }
        }

        if (refreshTask != null && !refreshTask.isCancelled()) refreshTask.cancel();
        if (mainConfiguration.getBooleanValue("item-refresh.auto-refresh", true)) {
            int interval = mainConfiguration.getIntValue("item-refresh.auto-refresh-time", 1);
            refreshTask = Bukkit.getServer().getScheduler().runTaskTimer(this, new ItemRefreshRunnable(), interval, interval);
        }

        expirationStageRegister.finaliseRegistrations();
    }

    public Configuration getConfiguration() {
        return mainConfiguration;
    }

    public class ItemRefreshRunnable implements Runnable {

        LinkedList<Player> queue = new LinkedList<>();

        @Override
        public void run() {
            Player player = queue.poll();
            if (player == null) {
                queue.addAll(Bukkit.getOnlinePlayers());
                return;
            }
            if (!player.isOnline()) {
                return;
            }
            applyFoodDescriptorsInventory(player);
        }
    }
}
