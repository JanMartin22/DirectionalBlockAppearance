package org.angler22.directionalblockappearance.view;

import org.angler22.directionalblockappearance.model.ViewBlock;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

/**
 * Decides which block a player sees for a DBA block.
 * There is one resolver per {@link org.angler22.directionalblockappearance.model.ViewMode}.
 * New modes can be added by writing a new resolver, without changing the rest of the plugin.
 */
public interface ViewResolver {
    /**
     * Returns the block a player sees from the given eye position.
     *
     * @param block the DBA block to look at
     * @param eye   the eye position of the player
     * @return the fake block or the real block, or null if the world is not loaded
     */
    BlockData resolve(ViewBlock block, Location eye);
}
