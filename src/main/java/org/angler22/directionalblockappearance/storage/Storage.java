package org.angler22.directionalblockappearance.storage;

import org.angler22.directionalblockappearance.manager.AppearanceManager;

/**
 * Saves and loads all groups and their blocks.
 * Implementations decide where the data is stored, for example a YAML file.
 * The plugin only talks to this interface, so the storage can be replaced
 * later, for example with a database.
 */
public interface Storage {

    /**
     * Saves all groups of the manager and replaces any previously saved data.
     * Errors are handled by the implementation and do not throw.
     *
     * @param manager the manager whose groups are saved
     */
    void saveAll(AppearanceManager manager);

    /**
     * Loads all saved groups into the manager.
     * Invalid data is skipped instead of stopping the whole load.
     *
     * @param manager the manager to load the groups into
     */
    void loadAll(AppearanceManager manager);
}
