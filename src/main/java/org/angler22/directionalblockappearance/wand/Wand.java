package org.angler22.directionalblockappearance.wand;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class Wand {
    private final NamespacedKey key;
    private final Material material;

    public Wand(Plugin plugin) {
        this.key = new NamespacedKey(plugin, "wand");
        Material m = Material.matchMaterial(plugin.getConfig().getString("wand.item", "GOLDEN_AXE"));
        if (m != null && m.isItem() && !m.isAir()) {
            this.material = m;
        } else {
            this.material = Material.GOLDEN_AXE;
        }
    }

    public ItemStack createWand() {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName("DBA-Wand");
        meta.setLore(List.of("Left click: pos1", "Right click: pos2"));
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.BYTE, (byte) 1);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public boolean isWand(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}
