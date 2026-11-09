package org.angler22.directionalblockappearance.wand;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * The selection wand. Left click sets pos1, right click sets pos2.
 * <p>
 * The wand is recognized by a hidden tag in its item data
 * (persistent data container), not by its name or material.
 * So renaming the item does not break it, and a normal item
 * of the same type is not treated as a wand.
 */
public class Wand {
    private final NamespacedKey key;
    private final Material material;

    /**
     * Creates the wand helper and reads the wand item from the config (wand.item).
     * Falls back to a golden axe if the configured item is missing or invalid.
     *
     * @param plugin the plugin, used for the tag key and the config
     */
    public Wand(Plugin plugin) {
        this.key = new NamespacedKey(plugin, "wand");
        Material m = Material.matchMaterial(plugin.getConfig().getString("wand.item", "GOLDEN_AXE"));
        if (m != null && m.isItem() && !m.isAir()) {
            this.material = m;
        } else {
            this.material = Material.GOLDEN_AXE;
        }
    }

    /**
     * Creates a new wand item with name, lore and the wand tag.
     *
     * @return a new wand item
     */
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

    /**
     * Checks if the given item is a DBA wand.
     *
     * @param item the item to check, may be null
     * @return true if the item has the wand tag
     */
    public boolean isWand(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}
