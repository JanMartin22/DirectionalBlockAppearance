package org.angler22.directionalblockappearance.listener;

import org.angler22.directionalblockappearance.manager.AppearanceManager;
import org.angler22.directionalblockappearance.model.BlockKey;
import org.angler22.directionalblockappearance.view.ViewService;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

public class ProtectionListener implements Listener {
    private final AppearanceManager manager;
    private final ViewService viewService;
    private final Plugin plugin;

    public ProtectionListener(AppearanceManager manager, ViewService viewService, Plugin plugin) {
        this.manager = manager;
        this.viewService = viewService;
        this.plugin = plugin;
    }

    private boolean isDba(Block block) {
        return manager.getBlock(new BlockKey(block)) != null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        BlockKey key = new BlockKey(event.getBlock());
        if (manager.getBlock(key) == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasPermission("dba.admin")) {
            event.setCancelled(true);
            player.sendMessage("This block is protected");
            Bukkit.getScheduler().runTask(plugin, () -> viewService.resync(player));   // ← neu
            return;
        }
        manager.removeBlock(key);
        viewService.forget(key);
        player.sendMessage("DBA block removed");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (isDba(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (isDba(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(this::isDba);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(this::isDba);
    }
    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (isDba(event.getBlockPlaced())) {
            event.setCancelled(true);
            return;
        }

        if (isDba(event.getBlockAgainst())) {
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTask(plugin, () -> viewService.resync(player));
        }
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Block clicked = event.getClickedBlock();
        if (clicked == null || !isDba(clicked)) {
            return;
        }
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> viewService.resync(player));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (isDba(event.getBlock())) {
            event.setCancelled(true);
        }
    }
}
