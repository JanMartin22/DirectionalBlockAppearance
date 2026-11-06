package org.angler22.directionalblockappearance.listener;

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

public class WandListener implements Listener {
    private final Wand wand;
    private final SelectionManager selectionManager;

    public WandListener(Wand wand, SelectionManager selectionManager) {
        this.wand = wand;
        this.selectionManager = selectionManager;
    }

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
            e.getPlayer().sendMessage("pos1 set (" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ")");
        } else {
            sel.setPos2(block);
            e.getPlayer().sendMessage("pos2 set (" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ")");
        }
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        selectionManager.clear(e.getPlayer());
    }
}
