package org.angler22.directionalblockappearance.listener;

import org.angler22.directionalblockappearance.manager.AppearanceManager;
import org.angler22.directionalblockappearance.model.BlockKey;
import org.angler22.directionalblockappearance.view.ViewService;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

public class ProtectionListener implements Listener {
    private final AppearanceManager manager;
    private final ViewService viewService;
    private final Plugin plugin;
    private final boolean protectBreak;
    private final boolean protectPlace;
    private final boolean protectPistons;
    private final boolean protectExplosions;
    private final boolean protectEntities;
    private final boolean protectFire;
    private final boolean protectLiquids;
    private final boolean protectNature;

    public ProtectionListener(AppearanceManager manager, ViewService viewService, Plugin plugin) {
        this.manager = manager;
        this.viewService = viewService;
        this.plugin = plugin;
        this.protectBreak = plugin.getConfig().getBoolean("protection.break", true);
        this.protectPlace = plugin.getConfig().getBoolean("protection.place", true);
        this.protectPistons = plugin.getConfig().getBoolean("protection.pistons", true);
        this.protectExplosions = plugin.getConfig().getBoolean("protection.explosions", true);
        this.protectEntities = plugin.getConfig().getBoolean("protection.entities", true);
        this.protectFire = plugin.getConfig().getBoolean("protection.fire", true);
        this.protectLiquids = plugin.getConfig().getBoolean("protection.liquids", true);
        this.protectNature = plugin.getConfig().getBoolean("protection.nature", true);
    }

    private boolean isDba(Block block) {
        return manager.getBlock(new BlockKey(block)) != null;
    }

    private void removeDba(Block block) {
        BlockKey key = new BlockKey(block);
        if (manager.getBlock(key) != null) {
            manager.removeBlock(key);
            viewService.forget(key);
        }
    }
    private void protect(Cancellable event, Block block, boolean enabled) {
        if (!isDba(block)) {
            return;
        }
        if (enabled) {
            event.setCancelled(true);
        } else {
            removeDba(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        BlockKey key = new BlockKey(event.getBlock());
        if (manager.getBlock(key) == null) {
            return;
        }
        Player player = event.getPlayer();
        if (protectBreak && !player.hasPermission("dba.break")) {
            event.setCancelled(true);
            player.sendMessage("This block is protected");
            Bukkit.getScheduler().runTask(plugin, () -> viewService.resync(player));
            return;
        }
        manager.removeBlock(key);
        viewService.forget(key);
        player.sendMessage("DBA block removed");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (!isDba(block)) {
                continue;
            }
            if(protectPistons){
                event.setCancelled(true);
                return;
            }
            removeDba(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (!isDba(block)) {
                continue;
            }
            if(protectPistons) {
                event.setCancelled(true);
                return;
            }
            removeDba(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if(protectExplosions){
            event.blockList().removeIf(this::isDba);
        } else {
            for(Block block : event.blockList()) {
                removeDba(block);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if(protectExplosions){
            event.blockList().removeIf(this::isDba);
        } else {
            for(Block block : event.blockList()) {
                removeDba(block);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        protect(event, event.getBlockPlaced(), protectPlace);

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
        protect(event, event.getBlock(), protectEntities);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBurn(BlockBurnEvent event) {
        protect(event, event.getBlock(), protectFire);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlow(BlockFromToEvent event) {
        protect(event, event.getToBlock(), protectLiquids);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        if (protectLiquids) {
            event.getBlocks().removeIf(state -> isDba(state.getBlock()));
        } else {
            for (BlockState state : event.getBlocks()) {
                removeDba(state.getBlock());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        protect(event, event.getBlock(), protectPlace);
        if (event.isCancelled()) {
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTask(plugin, () -> viewService.resync(player));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMultiPlace(BlockMultiPlaceEvent event) {
        for (BlockState state : event.getReplacedBlockStates()) {
            protect(event, state.getBlock(), protectPlace);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        protect(event, event.getBlock(), protectNature);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFade(BlockFadeEvent event) {
        protect(event, event.getBlock(), protectNature);
    }

    @EventHandler(ignoreCancelled = true)
    public void onForm(BlockFormEvent event) {
        protect(event, event.getBlock(), protectNature);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event) {
        protect(event, event.getBlock(), protectNature);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGrow(BlockGrowEvent event) {
        protect(event, event.getBlock(), protectNature);
    }

    @EventHandler(ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (protectNature) {
            event.getBlocks().removeIf(state -> isDba(state.getBlock()));
        } else {
            for (BlockState state : event.getBlocks()) {
                removeDba(state.getBlock());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        protect(event, event.getBlock(), protectEntities);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        protect(event, event.getBlock(), protectPlace);
        if (event.isCancelled()) {
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTask(plugin, () -> viewService.resync(player));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFluidChange(FluidLevelChangeEvent event) {
        protect(event, event.getBlock(), protectLiquids);
    }
}
