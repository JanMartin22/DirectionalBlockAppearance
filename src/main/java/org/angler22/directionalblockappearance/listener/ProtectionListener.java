package org.angler22.directionalblockappearance.listener;

import org.angler22.directionalblockappearance.manager.AppearanceManager;
import org.angler22.directionalblockappearance.model.BlockKey;
import org.angler22.directionalblockappearance.view.ViewService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ProtectionListener implements Listener {
    private final AppearanceManager manager;
    private final ViewService viewService;

    public ProtectionListener(AppearanceManager manager, ViewService viewService) {
        this.manager = manager;
        this.viewService = viewService;
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
}
