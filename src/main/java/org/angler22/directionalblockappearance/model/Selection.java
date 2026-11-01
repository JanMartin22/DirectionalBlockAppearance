package org.angler22.directionalblockappearance.model;

import org.bukkit.block.Block;

public class Selection {

    private Block pos1;
    private Block pos2;

    public Block getPos1(){
        return pos1;
    }

    public Block getPos2(){
        return pos2;
    }

    public void setPos1(Block pos1){
        this.pos1 = pos1;
    }

    public void setPos2(Block pos2){
        this.pos2 = pos2;
    }

    public boolean isComplete(){
        return pos1 != null && pos2 != null;
    }
}
