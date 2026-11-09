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

/**
 * Sends the fake blocks to the players.
 * <p>
 * For every player it remembers which block was last sent for each DBA block
 * (sentViews). On an update, only blocks whose view changed are sent again.
 * <p>
 * As a safety net, {@link #resyncAll()} runs regularly (view.resync-interval)
 * and sends all nearby blocks again, even if nothing changed. This fixes
 * fake blocks the client lost in situations the plugin does not catch.
 * <p>
 * Which view a player sees is decided by the {@link ViewResolver}
 * that belongs to the mode of the group.
 */
public final class ViewService {

    private final AppearanceManager manager;
    private final double radius;

    private final Map<ViewMode, ViewResolver> resolvers = new HashMap<>();
    private final Map<UUID, Map<BlockKey, BlockData>> sentViews = new HashMap<>();

    /**
     * Creates the view service.
     *
     * @param manager the manager that holds all groups and blocks
     * @param radius  the distance in blocks around a player in which fake blocks are updated
     */
    public ViewService(AppearanceManager manager, double radius) {
        this.manager = manager;
        this.radius = radius;
        resolvers.put(ViewMode.TWO_SIDED, new TwoSidedResolver());
    }

    /**
     * Updates all DBA blocks near the player.
     * Only sends blocks whose view changed since the last update.
     *
     * @param player the player to update
     */
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

    /**
     * Forgets everything that was sent to the player, without sending anything.
     * Used when the player leaves the server.
     *
     * @param player the player to forget
     */
    public void clear(Player player) {

        sentViews.remove(player.getUniqueId());
    }

    /**
     * Forgets the given block for all players, without sending anything.
     * Used when the real block changed and the DBA block was removed,
     * so the client already shows the new real block.
     *
     * @param key the position of the block to forget
     */
    public void forget(BlockKey key) {
        for (Map<BlockKey, BlockData> playerViews : sentViews.values()) {
            playerViews.remove(key);
        }
    }


    /**
     * Forgets what was sent to the player and sends all nearby blocks again.
     * Fixes fake blocks the client lost, for example after a teleport
     * or after the server sent the real block.
     *
     * @param player the player to resync
     */
    public void resync(Player player) {
        sentViews.remove(player.getUniqueId());
        update(player);
    }


    /**
     * Shows the real block to every player for every fake block that was sent.
     * Used when the plugin is disabled.
     */
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

    /**
     * Resyncs all online players. Runs regularly as a safety net.
     */
    public void resyncAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            resync(player);
        }
    }

    /**
     * Shows the real block at the given position to every player
     * who currently sees a fake block there.
     * Used when a DBA block is removed with /dba clear or /dba undo.
     *
     * @param key the position of the block to restore
     */
    public void restore(BlockKey key) {
        Location loc = key.toCenter();
        for (Map.Entry<UUID, Map<BlockKey, BlockData>> entry : sentViews.entrySet()) {
            if (entry.getValue().remove(key) == null) {
                continue;
            }
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || loc == null) {
                continue;
            }
            player.sendBlockChange(loc, loc.getBlock().getBlockData());
        }
    }
}

