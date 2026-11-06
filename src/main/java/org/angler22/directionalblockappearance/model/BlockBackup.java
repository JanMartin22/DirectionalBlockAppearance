package org.angler22.directionalblockappearance.model;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

import java.util.EnumMap;
import java.util.Map;

public class BlockBackup {
    private final BlockKey key;
    private final String groupName;
    private final Map<BlockFace, BlockData> views;

    public BlockBackup(BlockKey key, String groupName, Map<BlockFace, BlockData> views) {
        this.key = key;
        this.groupName = groupName;
        this.views = new EnumMap<>(BlockFace.class);
        this.views.putAll(views);
    }

    public BlockKey getKey() {
        return key;
    }

    public String getGroupName() {
        return groupName;
    }

    public Map<BlockFace, BlockData> getViews() {
        return views;
    }
}

