package org.angler22.directionalblockappearance.model;

import org.bukkit.block.data.BlockData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ViewGroup {
    private final String name;
    private final ViewMode mode;
    private BlockData defaultBlock;
    private final Map<BlockKey, ViewBlock> blocks = new HashMap<>();

    public ViewGroup(String name, ViewMode mode, BlockData defaultBlock) {
        this.name = name;
        this.mode = mode;
        this.defaultBlock = defaultBlock;
    }

    public String getName() {
        return name;
    }

    public BlockData getDefaultBlock() {
        return defaultBlock;
    }

    public ViewMode getMode() {
        return mode;
    }

    public Map<BlockKey, ViewBlock> getBlocks() {
        return Collections.unmodifiableMap(blocks);
    }

    public void setDefaultBlock(BlockData defaultBlock) {
        this.defaultBlock = defaultBlock;
    }

    public void addBlock(ViewBlock block){
        blocks.put(block.getKey(), block);
    }

    public void removeBlock(BlockKey key){
        blocks.remove(key);
    }

    public ViewBlock getBlock(BlockKey key){
        return blocks.get(key);
    }
}
