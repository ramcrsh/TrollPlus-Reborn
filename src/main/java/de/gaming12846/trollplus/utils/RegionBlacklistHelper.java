/*
 * This file is part of TrollPlus.
 * Copyright (C) 2024 Gaming12846
 */

package de.gaming12846.trollplus.utils;

import de.gaming12846.trollplus.TrollPlus;
import de.gaming12846.trollplus.constants.ConfigConstants;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RegionBlacklistHelper {
    private final TrollPlus plugin;
    private Boolean worldGuardAvailable;

    public RegionBlacklistHelper(TrollPlus plugin) {
        this.plugin = plugin;
    }

    public boolean isTargetInBlacklistedRegion(Player target) {
        List<String> blacklist = plugin.getConfigHelper().getStringList(ConfigConstants.REGION_BLACKLIST);
        if (blacklist.isEmpty()) {
            return false;
        }

        if (!isWorldGuardAvailable()) {
            return false;
        }

        Set<String> normalized = new HashSet<>();
        for (String region : blacklist) {
            if (region != null && !region.isBlank()) {
                normalized.add(region.toLowerCase(Locale.ROOT));
            }
        }

        if (normalized.isEmpty()) {
            return false;
        }

        return isInWorldGuardRegion(target, normalized);
    }

    private boolean isWorldGuardAvailable() {
        if (worldGuardAvailable != null) {
            return worldGuardAvailable;
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            worldGuardAvailable = false;
            return false;
        }

        try {
            Class.forName("com.sk89q.worldguard.WorldGuard");
            worldGuardAvailable = true;
        } catch (ClassNotFoundException e) {
            worldGuardAvailable = false;
        }

        return worldGuardAvailable;
    }

    private boolean isInWorldGuardRegion(Player target, Set<String> normalized) {
        try {
            Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object worldGuard = worldGuardClass.getMethod("getInstance").invoke(null);
            Object platform = worldGuardClass.getMethod("getPlatform").invoke(worldGuard);
            Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);

            World world = target.getWorld();
            Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldguard.bukkit.BukkitAdapter");
            Object adaptedWorld = bukkitAdapterClass.getMethod("adapt", World.class).invoke(null, world);

            Class<?> worldEditWorldClass = Class.forName("com.sk89q.worldedit.world.World");
            Object regionManager = regionContainer.getClass().getMethod("get", worldEditWorldClass).invoke(regionContainer, adaptedWorld);
            if (regionManager == null) {
                return false;
            }

            Location location = target.getLocation();
            Object applicableRegions = getApplicableRegions(regionManager, location);
            Set<?> regions = (Set<?>) applicableRegions.getClass().getMethod("getRegions").invoke(applicableRegions);

            for (Object region : regions) {
                String id = (String) region.getClass().getMethod("getId").invoke(region);
                if (id != null && normalized.contains(id.toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
        } catch (ReflectiveOperationException e) {
            plugin.getLoggingHelper().debug("Unable to check WorldGuard regions: " + e.getMessage());
        }

        return false;
    }

    private Object getApplicableRegions(Object regionManager, Location location) throws ReflectiveOperationException {
        Class<?> blockVectorClass = Class.forName("com.sk89q.worldedit.math.BlockVector3");
        Object blockVector = createBlockVector(blockVectorClass, location);

        try {
            return regionManager.getClass().getMethod("getApplicableRegions", blockVectorClass).invoke(regionManager, blockVector);
        } catch (NoSuchMethodException e) {
            return getApplicableRegionsByLocation(regionManager, location);
        }
    }

    private Object createBlockVector(Class<?> blockVectorClass, Location location) throws ReflectiveOperationException {
        try {
            return blockVectorClass.getMethod("at", int.class, int.class, int.class)
                    .invoke(null, location.getBlockX(), location.getBlockY(), location.getBlockZ());
        } catch (NoSuchMethodException e) {
            return blockVectorClass.getMethod("at", double.class, double.class, double.class)
                    .invoke(null, location.getX(), location.getY(), location.getZ());
        }
    }

    private Object getApplicableRegionsByLocation(Object regionManager, Location location) throws ReflectiveOperationException {
        Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldguard.bukkit.BukkitAdapter");
        Object adaptedLocation = bukkitAdapterClass.getMethod("adapt", Location.class).invoke(null, location);
        Class<?> worldEditLocationClass = Class.forName("com.sk89q.worldedit.util.Location");
        return regionManager.getClass().getMethod("getApplicableRegions", worldEditLocationClass).invoke(regionManager, adaptedLocation);
    }
}
