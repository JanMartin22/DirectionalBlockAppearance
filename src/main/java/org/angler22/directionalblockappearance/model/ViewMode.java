package org.angler22.directionalblockappearance.model;

/**
 * Decides how a block chooses which view to show to a player.
 */
public enum ViewMode {
    /**
     * Players on the side with a view see the fake block,
     * players anywhere else see the real block.
     * Used by all groups created with /dba group create.
     */
    TWO_SIDED,
    /**
     * Every side can show its own fake block.
     * Not supported yet, planned for a later version.
     */
    SIX_SIDED
}
