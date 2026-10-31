package org.angler22.directionalblockappearance.view;

import org.angler22.directionalblockappearance.model.ViewBlock;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

import org.bukkit.util.Vector;

public final class TwoSidedResolver implements ViewResolver {
    @Override
    public BlockData resolve(ViewBlock block, Location eye) {
        if (block.getViews().isEmpty()) {
            return block.getGroup().getDefaultBlock();
        }
        BlockFace face = block.getViews().keySet().iterator().next();
        Location center = block.getKey().toCenter();
        if(center == null){
            return block.getGroup().getDefaultBlock();
        }
        Vector v = eye.toVector().subtract(center.toVector());
        Vector n = face.getDirection();
        double d = v.dot(n);

        if(d > 0.5){
            return block.getView(face);
        } else {
            return block.getGroup().getDefaultBlock();
        }
    }
}
