package com.leonardobishop.foodexpiration.expiration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.bukkit.potion.PotionEffect;

public class PotionEffectWrapper {

    private final PotionEffect potionEffect;
    private final double chance;

    public PotionEffectWrapper(PotionEffect potionEffect, double chance) {
        this.potionEffect = potionEffect;
        this.chance = chance;
    }

    public PotionEffect getPotionEffect() {
        return potionEffect;
    }

    public double getChance() {
        return chance;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
