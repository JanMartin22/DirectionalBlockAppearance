package org.angler22.directionalblockappearance.listener;

import org.angler22.directionalblockappearance.config.Messages;
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

/**
 * Protects DBA blocks from being changed in the world.
 * <p>
 * Each kind of change has its own switch in the config (protection.*):
 * <ul>
 *   <li>switch on: the change is cancelled, the DBA block stays</li>
 *   <li>switch off: the change happens and the DBA entry is removed,
 *       because the real block is no longer the one that was set up</li>
 * </ul>
 * After a cancelled change by a player or a piston, the fake blocks are
 * sent again, because the client may already show the real block for a moment.
 */
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
    private final Messages messages;

    /**
     * Creates the protection listener and reads all protection switches from the config.
     *
     * @param manager     used to check and remove DBA blocks
     * @param viewService used to resend fake blocks after a cancelled change
     * @param plugin      the plugin, used for scheduling and the config
     * @param messages    used for the protected and removed messages
     */
    public ProtectionListener(AppearanceManager manager, ViewService viewService, Plugin plugin, Messages messages) {
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
        this.messages = messages;
    }

    /**
     * Checks if the block is a DBA block.
     *
     * @param block the block to check
     * @return true if there is a DBA block at this position
     */
    private boolean isDba(Block block) {
        return manager.getBlock(new BlockKey(block)) != null;
    }

    /**
     * Removes the DBA entry of the block, if there is one.
     * Sends nothing, because the client already gets the new real block from the server.
     *
     * @param block the block whose DBA entry is removed
     */
    private void removeDba(Block block) {
        BlockKey key = new BlockKey(block);
        if (manager.getBlock(key) != null) {
            manager.removeBlock(key);
            viewService.forget(key);
        }
    }

    /**
     * Cancels the event or removes the DBA entry, depending on the switch.
     * Does nothing if the block is not a DBA block.
     *
     * @param event   the event to cancel
     * @param block   the block that would change
     * @param enabled the protection switch for this kind of change
     */
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

    /**
     * Prevents players from breaking DBA blocks (protection.break).
     * If the switch is off or the player has dba.break, the block is broken
     * and the DBA entry is removed.
     *
     * @param event the break event
     */
    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        BlockKey key = new BlockKey(event.getBlock());
        if (manager.getBlock(key) == null) {
            return;
        }
        Player player = event.getPlayer();
        if (protectBreak && !player.hasPermission("dba.break")) {
            event.setCancelled(true);
            messages.send(player, "block-protected");
            Bukkit.getScheduler().runTask(plugin, () -> viewService.resync(player));
            return;
        }
        manager.removeBlock(key);
        viewService.forget(key);
        messages.send(player, "block-removed");
    }

    /**
     * Prevents pistons from pushing DBA blocks (protection.pistons).
     *
     * @param event the piston extend event
     */
    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (!isDba(block)) {
                continue;
            }
            if(protectPistons){
                event.setCancelled(true);
                Bukkit.getScheduler().runTaskLater(plugin, () -> viewService.resyncAll(), 2L);
                return;
            }
            removeDba(block);
        }
    }

    /**
     * Prevents sticky pistons from pulling DBA blocks (protection.pistons).
     *
     * @param event the piston retract event
     */
    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (!isDba(block)) {
                continue;
            }
            if(protectPistons) {
                event.setCancelled(true);
                Bukkit.getScheduler().runTaskLater(plugin, () -> viewService.resyncAll(), 2L);
                return;
            }
            removeDba(block);
        }
    }

    /**
     * Prevents explosions of entities, like TNT or creepers, from destroying DBA blocks
     * (protection.explosions). The rest of the explosion still happens.
     *
     * @param event the entity explode event
     */
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

    /**
     * Prevents explosions of blocks, like beds in the nether, from destroying DBA blocks
     * (protection.explosions). The rest of the explosion still happens.
     *
     * @param event the block explode event
     */
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

    /**
     * Prevents placing a block where a DBA block is, for example on grass (protection.place).
     * Also resends the fake blocks when a block is placed against a DBA block,
     * because the client may show the real block for a moment.
     *
     * @param event the place event
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        protect(event, event.getBlockPlaced(), protectPlace);
        if (event.isCancelled() || isDba(event.getBlockAgainst())) {
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTaskLater(plugin, () -> viewService.resync(player), 2L);
        }
    }

    /**
     * Resends the fake blocks when a player clicks a DBA block,
     * because the client may show the real block after the click.
     *
     * @param event the interact event
     */
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

    /**
     * Prevents entities, like endermen or falling blocks, from changing DBA blocks
     * (protection.entities).
     *
     * @param event the entity change block event
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        protect(event, event.getBlock(), protectEntities);
    }

    /**
     * Prevents fire from burning DBA blocks (protection.fire).
     *
     * @param event the burn event
     */
    @EventHandler(ignoreCancelled = true)
    public void onBurn(BlockBurnEvent event) {
        protect(event, event.getBlock(), protectFire);
    }

    /**
     * Prevents water and lava from flowing into DBA blocks (protection.liquids).
     *
     * @param event the flow event
     */
    @EventHandler(ignoreCancelled = true)
    public void onFlow(BlockFromToEvent event) {
        protect(event, event.getToBlock(), protectLiquids);
    }

    /**
     * Prevents sponges from changing DBA blocks (protection.liquids).
     *
     * @param event the sponge absorb event
     */
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

    /**
     * Prevents emptying a bucket into a DBA block (protection.place).
     *
     * @param event the bucket empty event
     */
    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        protect(event, event.getBlock(), protectPlace);
        if (event.isCancelled()) {
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTask(plugin, () -> viewService.resync(player));
        }
    }

    /**
     * Prevents placing multi-block blocks, like beds or doors, over DBA blocks
     * (protection.place).
     *
     * @param event the multi place event
     */
    @EventHandler(ignoreCancelled = true)
    public void onMultiPlace(BlockMultiPlaceEvent event) {
        for (BlockState state : event.getReplacedBlockStates()) {
            protect(event, state.getBlock(), protectPlace);
        }
        if (event.isCancelled()) {
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTaskLater(plugin, () -> viewService.resync(player), 2L);
        }
    }

    /**
     * Prevents DBA leaves from decaying (protection.nature).
     *
     * @param event the leaves decay event
     */
    @EventHandler(ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        protect(event, event.getBlock(), protectNature);
    }

    /**
     * Prevents DBA blocks from fading, like ice melting or coral dying (protection.nature).
     *
     * @param event the fade event
     */
    @EventHandler(ignoreCancelled = true)
    public void onFade(BlockFadeEvent event) {
        protect(event, event.getBlock(), protectNature);
    }

    /**
     * Prevents blocks from forming at DBA blocks, like snow or ice (protection.nature).
     *
     * @param event the form event
     */
    @EventHandler(ignoreCancelled = true)
    public void onForm(BlockFormEvent event) {
        protect(event, event.getBlock(), protectNature);
    }

    /**
     * Prevents blocks from spreading into DBA blocks, like grass or mushrooms (protection.nature).
     *
     * @param event the spread event
     */
    @EventHandler(ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event) {
        protect(event, event.getBlock(), protectNature);
    }

    /**
     * Prevents DBA plants from growing (protection.nature).
     *
     * @param event the grow event
     */
    @EventHandler(ignoreCancelled = true)
    public void onGrow(BlockGrowEvent event) {
        protect(event, event.getBlock(), protectNature);
    }

    /**
     * Prevents trees and huge mushrooms from growing into DBA blocks (protection.nature).
     *
     * @param event the structure grow event
     */
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

    /**
     * Prevents entities from forming blocks at DBA blocks, like snow golems
     * or frost walker ice (protection.entities).
     *
     * @param event the entity block form event
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        protect(event, event.getBlock(), protectEntities);
    }

    /**
     * Prevents filling a bucket from a DBA block (protection.place).
     *
     * @param event the bucket fill event
     */
    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        protect(event, event.getBlock(), protectPlace);
        if (event.isCancelled()) {
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTask(plugin, () -> viewService.resync(player));
        }
    }

    /**
     * Prevents the fluid level of DBA blocks from changing (protection.liquids).
     *
     * @param event the fluid level change event
     */
    @EventHandler(ignoreCancelled = true)
    public void onFluidChange(FluidLevelChangeEvent e) {
        protect(e, e.getBlock(), protectLiquids);
    }

    /**
     * Shows the protected message as soon as a player starts mining a protected
     * DBA block and stops the mining (protection.break).
     * Players with dba.break mine normally; the DBA entry is removed in onBreak.
     *
     * @param e the damage event
     */
    @EventHandler(ignoreCancelled = true)
    public void onDamage(BlockDamageEvent e){
        BlockKey key = new BlockKey(e.getBlock());
        if (manager.getBlock(key) == null) {
            return;
        }
        Player player = e.getPlayer();
        if (protectBreak && !player.hasPermission("dba.break")) {
            e.setCancelled(true);
            messages.send(player, "block-protected");
            Bukkit.getScheduler().runTask(plugin, () -> viewService.resync(player));
        }
    }
}
