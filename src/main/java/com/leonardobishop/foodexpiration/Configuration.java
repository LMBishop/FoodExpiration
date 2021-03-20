package com.leonardobishop.foodexpiration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {

    // for some reason the cache is more performant than accessing the config, even though
    // that is also cached
    private final Map<String, Boolean> booleanCache = new HashMap<>();
    private FileConfiguration config;

    /**
     * Sets the config to a given config, and invalidates the caches.
     *
     * @param config new configuration
     */
    public void loadConfig(FileConfiguration config) {
        this.config = config;
        this.invalidateCaches();
    }

    /**
     * Attempts to load the config from a file and invalidates the caches if successful.
     *
     * @param file the File to load from
     */
    public void loadConfig(File file) {
        this.config = YamlConfiguration.loadConfiguration(file);
        this.invalidateCaches();
    }

    /**
     * Get an integer from the config.
     *
     * @param path the path
     * @return integer
     */
    public int getIntValue(String path) {
        return config.getInt(path);
    }

    /**
     * Get an integer from the config, or a specified default.
     *
     * @param path the path
     * @param def default if it does not exist
     * @return integer
     */
    public int getIntValue(String path, int def) {
        return config.getInt(path, def);
    }

    /**
     * Get a string from the config.
     *
     * @param path the string
     * @return string
     */
    public String getStringValue(String path) {
        return config.getString(path);
    }

    /**
     * Get a string from the config, or a specified default.
     *
     * @param path the path
     * @param def default if it does not exist
     * @return string
     */
    public String getStringValue(String path, String def) {
        return config.getString(path, def);
    }

    /**
     * Get a boolean from the config (or the cache if previously accessed).
     *
     * @param path the path
     * @return boolean
     */
    public boolean getBooleanValue(String path) {
        return booleanCache.computeIfAbsent(path, s -> config.getBoolean(path));
    }

    /**
     * Get a boolean from the config (or the cache if previously accessed), or a specified default.
     * The default will be cached if it does not exist.
     *
     * @param path the path
     * @return boolean
     */
    public boolean getBooleanValue(String path, boolean def) {
        return booleanCache.computeIfAbsent(path, s -> config.getBoolean(path, def));
    }

    /**
     * Get a List<String> from the config.
     *
     * @param path the path
     * @return List<String>
     */
    public List<String> getStringListValue(String path) {
        return config.getStringList(path);
    }

    /**
     * Clear all caches.
     */
    public void invalidateCaches() {
        booleanCache.clear();
    }

}
