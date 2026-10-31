package org.angler22.directionalblockappearance.listener;

import org.angler22.directionalblockappearance.manager.AppearanceManager;
import org.angler22.directionalblockappearance.model.BlockKey;
import org.angler22.directionalblockappearance.view.ViewService;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ProtectionListener implements Listener {
    private final AppearanceManager manager;
    private final ViewService viewService;

    public ProtectionListener(AppearanceManager manager, ViewService viewService) {
        this.manager = manager;
        this.viewService = viewService;
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
            viewService.update(player);
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
}
