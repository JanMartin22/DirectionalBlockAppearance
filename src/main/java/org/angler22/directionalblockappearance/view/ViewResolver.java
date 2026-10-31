package org.angler22.directionalblockappearance.view;

import org.angler22.directionalblockappearance.model.ViewBlock;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public interface ViewResolver {
    BlockData resolve(ViewBlock block, Location eye);
}
