package com.leonardobishop.foodexpiration.expiration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExpirationStage implements Comparable<ExpirationStage> {

    private final String name;
    private final List<PotionEffectWrapper> potionEffects;
    private final TimeUnit timeUnit;
    private final long time;
    private String description;
    private List<String> extendedDescription;
    private double hungerModifier;

    private List<String> constructedLore;

    public ExpirationStage(String name, TimeUnit timeUnit, long time, List<PotionEffectWrapper> potionEffects) {
        this.name = name;
        this.timeUnit = timeUnit;
        this.time = time;
        this.potionEffects = potionEffects;

        this.description = null;
        this.extendedDescription = null;
        this.hungerModifier = 1;
    }

    public String getName() {
        return name;
    }

    public List<PotionEffectWrapper> getPotionEffects() {
        return Collections.unmodifiableList(potionEffects);
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public long getTime() {
        return time;
    }

    public double getHungerModifier() {
        return hungerModifier;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getExtendedDescription() {
        return extendedDescription;
    }

    public List<String> asItemLore() {
        if (constructedLore == null) {
            List<String> lore = new ArrayList<>();
            if (description != null) {
                lore.add(ChatColor.translateAlternateColorCodes('&', description.replace("%name%", name)));
            }

            if (extendedDescription != null) {
                for (String line : extendedDescription) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', line.replace("%name%", name)));
                }
            }
            constructedLore = lore;
            return lore;
        }
        return constructedLore;
    }

    public void setHungerModifier(double hungerModifier) {
        this.hungerModifier = hungerModifier;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExtendedDescription(List<String> extendedDescription) {
        this.extendedDescription = extendedDescription;
    }

    public long inMillis() {
        if (timeUnit == TimeUnit.MILLISECONDS) return time;
        return TimeUnit.MILLISECONDS.convert(time, timeUnit);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int compareTo(ExpirationStage o) {
        return (int) (TimeUnit.MILLISECONDS.convert(time, timeUnit) - TimeUnit.MILLISECONDS.convert(o.time, o.timeUnit));
    }
}
