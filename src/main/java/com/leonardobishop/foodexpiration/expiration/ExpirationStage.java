package com.leonardobishop.foodexpiration.expiration;

import org.bukkit.potion.PotionEffect;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExpirationStage implements Comparable<ExpirationStage> {

    private final String name;
    private final List<PotionEffect> potionEffects;
    private final TimeUnit timeUnit;
    private final long time;
    private  double hungerModifier;

    public ExpirationStage(String name, TimeUnit timeUnit, long time, List<PotionEffect> potionEffects) {
        this.name = name;
        this.timeUnit = timeUnit;
        this.time = time;
        this.potionEffects = potionEffects;

        this.hungerModifier = 1;
    }

    public String getName() {
        return name;
    }

    public List<PotionEffect> getPotionEffects() {
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

    public void setHungerModifier(double hungerModifier) {
        this.hungerModifier = hungerModifier;
    }

    public long inMillis() {
        if (timeUnit == TimeUnit.MILLISECONDS) return time;
        return TimeUnit.MILLISECONDS.convert(time, timeUnit);
    }

    @Override
    public int compareTo(ExpirationStage o) {
        return (int) (TimeUnit.MILLISECONDS.convert(time, timeUnit) - TimeUnit.MILLISECONDS.convert(o.time, o.timeUnit));
    }
}
