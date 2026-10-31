package org.angler22.directionalblockappearance.listener;

import org.angler22.directionalblockappearance.view.ViewService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class ViewListener implements Listener {

    private final Plugin plugin;
    private final ViewService viewService;


    public ViewListener(Plugin plugin, ViewService viewService) {
        this.plugin = plugin;
        this.viewService = viewService;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        Location from = e.getFrom();
        Location to = e.getTo();

        if(to == null){
            return;
        }
        if(to.getBlockX() == from.getBlockX() && to.getBlockY() == from.getBlockY() && to.getBlockZ() == from.getBlockZ()){
            return;
        }
        viewService.update(e.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> viewService.update(player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        viewService.clear(e.getPlayer());
    }
}

