package org.angler22.directionalblockappearance.model;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

import java.util.EnumMap;
import java.util.Map;

/**
 * A snapshot of one DBA block before a change, used by /dba undo.
 * Stores the group and all views the block had at that moment.
 * If the block was not a DBA block before, the group name is null.
 */
public class BlockBackup {
    private final BlockKey key;
    private final String groupName;
    private final Map<BlockFace, BlockData> views;

    /**
     * Creates a snapshot of a block.
     * The views are copied, so later changes to the block do not affect the snapshot.
     *
     * @param key       the position of the block
     * @param groupName the name of the group the block belonged to, or null if it was not a DBA block
     * @param views     the views the block had, empty if it was not a DBA block
     */
    public BlockBackup(BlockKey key, String groupName, Map<BlockFace, BlockData> views) {
        this.key = key;
        this.groupName = groupName;
        this.views = new EnumMap<>(BlockFace.class);
        this.views.putAll(views);
    }

    /**
     * Returns the position of the saved block.
     *
     * @return the position of the block
     */
    public BlockKey getKey() {
        return key;
    }

    /**
     * Returns the group the block belonged to.
     *
     * @return the group name, or null if the block was not a DBA block
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Returns the saved views of the block.
     *
     * @return the views, mapped from side to fake block; empty if the block was not a DBA block
     */
    public Map<BlockFace, BlockData> getViews() {
        return views;
    }
}

