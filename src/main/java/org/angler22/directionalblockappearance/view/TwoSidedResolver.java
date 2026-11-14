package org.angler22.directionalblockappearance.view;

import org.angler22.directionalblockappearance.model.ViewBlock;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

import org.bukkit.util.Vector;

/**
 * Resolver for {@link org.angler22.directionalblockappearance.model.ViewMode#TWO_SIDED}.
 * <p>
 * A two-sided block has one view on one side. Players in front of that side
 * see the fake block, everyone else sees the real block.
 * <p>
 * "In front" is checked with the dot product of
 * v = eye - block center and n = direction of the side:
 * d = v · n is the distance of the eye from the block center along n.
 * The side itself is 0.5 blocks away from the center, so d &gt; 0.5 means
 * the eye is in front of that side.
 */
public final class TwoSidedResolver implements ViewResolver {

    /**
     * {@inheritDoc}
     * <p>
     * Only the first view of the block is used. Blocks without views
     * show the real block.
     */
    @Override
    public BlockData resolve(ViewBlock block, Location eye) {
        if (block.getViews().isEmpty()) {
            return null;
        }
        BlockFace face = block.getViews().keySet().iterator().next();
        Location center = block.getKey().toCenter();
        if(center == null){
            return null;
        }
        Vector v = eye.toVector().subtract(center.toVector());
        Vector n = face.getDirection();
        double d = v.dot(n);

        if(d > 0.5){
            return block.getView(face);
        } else {
            return null;
        }
    }
}
