package org.angler22.directionalblockappearance.storage;

import org.angler22.directionalblockappearance.manager.AppearanceManager;

public interface Storage {
    void saveAll(AppearanceManager manager);
    void loadAll(AppearanceManager manager);
}
