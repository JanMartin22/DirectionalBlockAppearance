package org.angler22.directionalblockappearance.listener;

import org.angler22.directionalblockappearance.view.ViewService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;

/**
 * Keeps the fake blocks of each player up to date.
 * <p>
 * Updates the view when a player moves to another block, and resends
 * all nearby fake blocks after a teleport, respawn or world change,
 * because the client may have lost them. The resend is delayed
 * (view.teleport-delay) so the client has loaded the new chunks first.
 */
public class ViewListener implements Listener {

    private final Plugin plugin;
    private final ViewService viewService;
    private final long teleportDelay;

    /**
     * Creates the view listener and reads view.teleport-delay from the config.
     *
     * @param plugin      the plugin, used for scheduling and the config
     * @param viewService the service that sends the fake blocks
     */
    public ViewListener(Plugin plugin, ViewService viewService) {
        this.plugin = plugin;
        this.viewService = viewService;
        this.teleportDelay = plugin.getConfig().getLong("view.teleport-delay", 10);
    }

    /**
     * Updates the view when the player's eyes move into another block.
     * Small movements inside the same block and only turning the head are ignored.
     *
     * @param e the move event
     */
    @EventHandler
    public void onMove(PlayerMoveEvent e){
        Location from = e.getFrom();
        Location to = e.getTo();

        if(to == null){
            return;
        }

        double eyeHeight = e.getPlayer().getEyeHeight();
        int fromEyeY = (int) Math.floor(from.getY() + eyeHeight);
        int toEyeY = (int) Math.floor(to.getY() + eyeHeight);

        if(to.getBlockX() == from.getBlockX() && toEyeY == fromEyeY && to.getBlockZ() == from.getBlockZ()){
            return;
        }
        viewService.update(e.getPlayer(), false);
    }

    /**
     * Sends the fake blocks to a player who joins.
     * Runs one tick later, when the player is fully in the world.
     *
     * @param e the join event
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> viewService.update(player, false));
    }

    /**
     * Forgets what was sent to the player who leaves.
     *
     * @param e the quit event
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        viewService.clear(e.getPlayer());
    }

    /**
     * Resends all nearby fake blocks after a teleport.
     *
     * @param event the teleport event
     */
    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> viewService.resync(player), teleportDelay);
    }

    /**
     * Resends all nearby fake blocks after a respawn.
     *
     * @param event the respawn event
     */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> viewService.resync(player), teleportDelay);
    }

    /**
     * Clears what was sent to the player and resends all nearby fake blocks
     * after a world change. The client drops the old world completely,
     * so nothing from there needs to be restored.
     *
     * @param event the world change event
     */
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        viewService.clear(player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> viewService.resync(player), teleportDelay);
    }
}

