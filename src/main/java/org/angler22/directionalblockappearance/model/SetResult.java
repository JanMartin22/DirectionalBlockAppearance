package org.angler22.directionalblockappearance.model;

/**
 * The result of setting a view on a block with /dba set or /dba quickset.
 */
public enum SetResult {
    /** The block was not a DBA block yet and was added to the group. */
    ADDED,
    /** The block was already in the group and its view was replaced. */
    UPDATED,
    /** The block belongs to another group and was left unchanged. */
    SKIPPED
}
