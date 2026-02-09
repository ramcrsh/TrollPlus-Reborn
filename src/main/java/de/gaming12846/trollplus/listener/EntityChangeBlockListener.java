/*
 * This file is part of TrollPlus.
 * Copyright (C) 2024 Gaming12846
 */

package de.gaming12846.trollplus.listener;

import de.gaming12846.trollplus.TrollPlus;
import de.gaming12846.trollplus.constants.ConfigConstants;
import de.gaming12846.trollplus.constants.MetadataConstants;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.scheduler.BukkitRunnable;

// Listener for falling anvils to clean them up after landing
public class EntityChangeBlockListener implements Listener {
    private final TrollPlus plugin;

    public EntityChangeBlockListener(TrollPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof FallingBlock fallingBlock)) {
            return;
        }

        if (!fallingBlock.hasMetadata(MetadataConstants.TROLLPLUS_FALLING_ANVIL_ENTITY)) {
            return;
        }

        if (!isAnvilMaterial(event.getTo())) {
            return;
        }

        Location location = event.getBlock().getLocation();
        int delay = plugin.getConfigHelper().getInt(ConfigConstants.FALLING_ANVILS_REMOVE_DELAY);
        if (delay < 0) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Block block = location.getBlock();
                if (isAnvilMaterial(block.getType())) {
                    block.setType(Material.AIR);
                }
            }
        }.runTaskLater(plugin, delay);
    }

    private boolean isAnvilMaterial(Material material) {
        return material == Material.ANVIL
                || material == Material.CHIPPED_ANVIL
                || material == Material.DAMAGED_ANVIL;
    }
}
