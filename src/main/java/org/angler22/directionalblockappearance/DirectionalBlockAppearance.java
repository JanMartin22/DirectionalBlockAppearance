package org.angler22.directionalblockappearance;

import org.angler22.directionalblockappearance.command.AppearanceCommand;
import org.angler22.directionalblockappearance.listener.ProtectionListener;
import org.angler22.directionalblockappearance.listener.ViewListener;
import org.angler22.directionalblockappearance.manager.AppearanceManager;
import org.angler22.directionalblockappearance.storage.Storage;
import org.angler22.directionalblockappearance.storage.YamlStorage;
import org.angler22.directionalblockappearance.view.ViewService;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class DirectionalBlockAppearance extends JavaPlugin {
    private AppearanceManager manager;
    private Storage storage;
    private ViewService viewService;

    @Override
    public void onEnable() {
        manager = new AppearanceManager();
        storage = new YamlStorage(new File(getDataFolder(), "groups.yml"), getLogger());
        storage.loadAll(manager);
        viewService = new ViewService(manager, 48);

        getServer().getPluginManager().registerEvents(new ProtectionListener(manager, viewService, this), this);

        getServer().getPluginManager().registerEvents(new ViewListener(this, viewService), this);

        getCommand("dba").setExecutor(new AppearanceCommand(manager, viewService));

        getLogger().info("Enabled");
    }
    @Override
    public void onDisable(){
        viewService.restoreAll();
        storage.saveAll(manager);
    }
}