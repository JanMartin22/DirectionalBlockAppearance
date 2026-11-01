package org.angler22.directionalblockappearance.model;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

import java.util.EnumMap;
import java.util.Map;

public class ViewBlock {
    private final BlockKey key;
    private final ViewGroup group;
    private final Map<BlockFace, BlockData> views = new EnumMap<>(BlockFace.class);


    public ViewBlock(BlockKey key, ViewGroup group) {
        this.key = key;
        this.group = group;
    }

    public BlockKey getKey() {
        return key;
    }

    public ViewGroup getGroup() {
        return group;
    }

    public Map<BlockFace, BlockData> getViews() {
        return views;
    }

    public void setView(BlockFace face, BlockData data){
        views.put(face, data);
    }

    public void removeView(BlockFace face){
        views.remove(face);
    }

    public BlockData getView(BlockFace face) {
        BlockData view = views.get(face);
        if (view == null) {
            return getRealBlock();
        }
        return view;
    }

    public BlockData getRealBlock() {
        Location center = key.toCenter();
        if (center == null) {
            return null;
        }
        return center.getBlock().getBlockData();
    }
}

