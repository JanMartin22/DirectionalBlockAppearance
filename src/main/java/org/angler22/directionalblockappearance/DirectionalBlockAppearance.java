package org.angler22.directionalblockappearance;

import org.angler22.directionalblockappearance.command.AppearanceCommand;
import org.angler22.directionalblockappearance.listener.ProtectionListener;
import org.angler22.directionalblockappearance.listener.ViewListener;
import org.angler22.directionalblockappearance.manager.AppearanceManager;
import org.angler22.directionalblockappearance.manager.SelectionManager;
import org.angler22.directionalblockappearance.storage.Storage;
import org.angler22.directionalblockappearance.storage.YamlStorage;
import org.angler22.directionalblockappearance.view.ViewService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class DirectionalBlockAppearance extends JavaPlugin {
    private AppearanceManager manager;
    private Storage storage;
    private ViewService viewService;
    private SelectionManager selectionManager;

    @Override
    public void onEnable() {
        manager = new AppearanceManager();
        storage = new YamlStorage(new File(getDataFolder(), "groups.yml"), getLogger());
        storage.loadAll(manager);
        manager.markSaved();
        viewService = new ViewService(manager, 48);
        selectionManager = new SelectionManager();

        getServer().getPluginManager().registerEvents(new ProtectionListener(manager, viewService, this), this);

        Bukkit.getScheduler().runTaskTimer(this, () -> viewService.resyncAll(), 40L, 40L);

        getServer().getPluginManager().registerEvents(new ViewListener(this, viewService), this);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (manager.hasChanged()) {
                storage.saveAll(manager);
                manager.markSaved();
            }
        }, 600L, 600L);



        getCommand("dba").setExecutor(new AppearanceCommand(manager, viewService, selectionManager));

        getLogger().info("Enabled");
    }
    @Override
    public void onDisable(){
        viewService.restoreAll();
        storage.saveAll(manager);
    }
}