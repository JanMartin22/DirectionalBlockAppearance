package org.angler22.directionalblockappearance.view;

import org.angler22.directionalblockappearance.manager.AppearanceManager;
import org.angler22.directionalblockappearance.model.BlockKey;
import org.angler22.directionalblockappearance.model.ViewBlock;
import org.angler22.directionalblockappearance.model.ViewMode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ViewService {

    private final AppearanceManager manager;
    private final double radius;

    private final Map<ViewMode, ViewResolver> resolvers = new HashMap<>();
    private final Map<UUID, Map<BlockKey, BlockData>> sentViews = new HashMap<>();


    public ViewService(AppearanceManager manager, double radius) {
        this.manager = manager;
        this.radius = radius;
        resolvers.put(ViewMode.TWO_SIDED, new TwoSidedResolver());
    }

    public void update(Player player) {
        Map<BlockKey, BlockData> playerViews = sentViews.get(player.getUniqueId());
        if (playerViews == null) {
            playerViews = new HashMap<>();
            sentViews.put(player.getUniqueId(), playerViews);
        }

        Location eye = player.getEyeLocation();

        for (ViewBlock vb : manager.getNearby(player.getLocation(), radius)) {
            ViewResolver resolver = resolvers.get(vb.getGroup().getMode());
            if (resolver == null) {
                continue;
            }
            BlockData data = resolver.resolve(vb, eye);
            if (data == null) {
                continue;
            }

            if (data.equals(playerViews.get(vb.getKey()))) {
                continue;
            }

            Location loc = vb.getKey().toCenter();
            if (loc == null) {
                continue;
            }
            player.sendBlockChange(loc, data);
            playerViews.put(vb.getKey(), data);
        }
    }
    public void clear(Player player) {
        sentViews.remove(player.getUniqueId());
    }
    public void forget(BlockKey key) {
        for (Map<BlockKey, BlockData> playerViews : sentViews.values()) {
            playerViews.remove(key);
        }
    }
    public void resync(Player player) {
        sentViews.remove(player.getUniqueId());
        update(player);
    }
    public void restoreAll() {
        for (Map.Entry<UUID, Map<BlockKey, BlockData>> entry : sentViews.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                continue;
            }
            for (BlockKey key : entry.getValue().keySet()) {
                Location loc = key.toCenter();
                if (loc == null) {
                    continue;
                }
                player.sendBlockChange(loc, loc.getBlock().getBlockData());
            }
        }
        sentViews.clear();
    }
    public void resyncAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            resync(player);
        }
    }
}

