package org.angler22.directionalblockappearance.manager;

import org.angler22.directionalblockappearance.model.BlockKey;
import org.angler22.directionalblockappearance.model.ViewBlock;
import org.angler22.directionalblockappearance.model.ViewGroup;
import org.angler22.directionalblockappearance.model.ViewMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

import java.util.*;

public class AppearanceManager {
    private final Map<String, ViewGroup> groups = new HashMap<>();
    private final Map<BlockKey, ViewBlock> index = new HashMap<>();
    private boolean changed = false;

public ViewGroup createGroup(String name, ViewMode mode, BlockData defaultBlock){
    if(groups.containsKey(name)){
        return null;  // Gibt null zurück wenn der Name schon vergeben ist.
    } else {
       ViewGroup vg = new ViewGroup(name, mode, defaultBlock);
       groups.put(name, vg);
       changed = true;
       return vg;
    }
}
public ViewGroup getGroup(String name){
    return groups.get(name);
}

public ViewBlock getBlock(BlockKey key){
    return index.get(key);
}

public ViewBlock addBlock(ViewGroup group, Block block, BlockFace face, BlockData data){
    BlockKey key = new BlockKey(block);
    if(index.containsKey(key)){
        return null;
    }
    ViewBlock vb = new ViewBlock(key, group);
    vb.setView(face, data);
    group.addBlock(vb);
    index.put(key, vb);
    changed = true;
    return vb;
}

public void removeBlock(BlockKey key){
    ViewBlock vb = index.get(key);
    index.remove(key);
    if(vb != null){
        vb.getGroup().removeBlock(key);
        changed = true;
    }
}

public List<ViewBlock> getNearby(Location loc, double r){
    List<ViewBlock> list = new ArrayList<>();

    for(ViewBlock vb : index.values()){
        Location center = vb.getKey().toCenter();
       if(center == null){
           continue;
       }
        if (!center.getWorld().equals(loc.getWorld())) {
            continue;
        }
        if(center.distanceSquared(loc) <= r * r){
            list.add(vb);
        }
    }
    return list;
}
    public Collection<ViewGroup> getGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    public ViewBlock addBlock(ViewGroup group, BlockKey key) {
        if (index.containsKey(key)) {
            return null;
        }
        ViewBlock vb = new ViewBlock(key, group);
        group.addBlock(vb);
        index.put(key, vb);
        changed = true;
        return vb;
    }
    public boolean hasChanged() {
        return changed;
    }

    public void markSaved() {
        changed = false;
    }
}
