/*
 * This file is part of TrollPlus.
 * Copyright (C) 2024 Gaming12846
 */

package de.gaming12846.trollplus.utils;

import org.bukkit.potion.PotionEffectType;

public final class PotionEffectHelper {
    private PotionEffectHelper() {
    }

    public static PotionEffectType getSlownessEffectType() {
        PotionEffectType effectType = PotionEffectType.getByName("SLOWNESS");
        if (effectType == null) {
            effectType = PotionEffectType.getByName("SLOW");
        }
        return effectType;
    }
}
