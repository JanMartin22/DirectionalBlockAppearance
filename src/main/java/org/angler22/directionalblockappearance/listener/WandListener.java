package org.angler22.directionalblockappearance.listener;

import org.angler22.directionalblockappearance.config.Messages;
import org.angler22.directionalblockappearance.manager.SelectionManager;
import org.angler22.directionalblockappearance.model.Selection;
import org.angler22.directionalblockappearance.wand.Wand;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Handles clicks with the selection wand.
 * Left click on a block sets pos1, right click sets pos2.
 * Also removes the selection of a player when they leave.
 */
public class WandListener implements Listener {
    private final Wand wand;
    private final SelectionManager selectionManager;
    private final Messages messages;

    /**
     * Creates the wand listener.
     *
     * @param wand             used to check if an item is the wand
     * @param selectionManager stores the selections of the players
     * @param messages         used for the pos-set message
     */
    public WandListener(Wand wand, SelectionManager selectionManager, Messages messages) {
        this.wand = wand;
        this.selectionManager = selectionManager;
        this.messages = messages;
    }

    /**
     * Sets pos1 or pos2 when a player clicks a block with the wand.
     * The click is cancelled, so the block is not broken or used.
     * Players without dba.command.pos use the wand like a normal item.
     *
     * @param e the interact event
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        if(e.getHand() != EquipmentSlot.HAND){
            return;
        }
        if (!wand.isWand(e.getItem())){
            return;
        }
        if (!e.getPlayer().hasPermission("dba.command.pos")) {
            return;
        }
        if(e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }
        Block block = e.getClickedBlock();
        if(block == null){
            return;
        }
        e.setCancelled(true);
        Selection sel = selectionManager.getSelection(e.getPlayer());
        if(e.getAction() == Action.LEFT_CLICK_BLOCK){
            sel.setPos1(block);
            messages.send(e.getPlayer(), "pos-set", "pos", "pos1", "x", String.valueOf(block.getX()),
                    "y", String.valueOf(block.getY()), "z", String.valueOf(block.getZ()));
        } else {
            sel.setPos2(block);
            messages.send(e.getPlayer(), "pos-set", "pos", "pos2", "x", String.valueOf(block.getX()),
                    "y", String.valueOf(block.getY()), "z", String.valueOf(block.getZ()));
        }
    }

    /**
     * Removes the selection of the player who leaves.
     *
     * @param e the quit event
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        selectionManager.clear(e.getPlayer());
    }
}
