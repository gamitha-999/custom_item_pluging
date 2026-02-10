package cwr.cutomIterm.Entity_Wand.CustomCraftingTable;

import cwr.cutomIterm.Entity_Wand.EntityWandPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.UUID;

public class CustomCraftingTable {

    private static final NamespacedKey CUSTOM_TABLE_KEY = new NamespacedKey(EntityWandPlugin.getInstance(), "custom_crafting_table");

    public static ItemStack createCustomCraftingTable() {
        try {
            ItemStack table = new ItemStack(Material.CRAFTING_TABLE, 1);
            ItemMeta meta = table.getItemMeta();

            if (meta == null) {
                return table;
            }

            // Custom name
            meta.setDisplayName("§6§lAdvanced Crafting Table");

            // Add enchantment glow
            meta.setEnchantmentGlintOverride(true);

            // Set max stack size to 1
            try {
                meta.setMaxStackSize(1);
            } catch (NoSuchMethodError e) {
                // Fallback for older versions
            }

            // Mark as custom crafting table
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(CUSTOM_TABLE_KEY, PersistentDataType.BYTE, (byte) 1);

            // Add unique ID to prevent stacking
            pdc.set(new NamespacedKey(EntityWandPlugin.getInstance(), "table_unique_id"),
                    PersistentDataType.STRING,
                    UUID.randomUUID().toString());

            // Set lore
            meta.setLore(Arrays.asList(
                    "§6§lAdvanced Crafting Station",
                    "",
                    "§7Place to create a §e6x6 crafting table",
                    "§7Supports advanced custom recipes",
                    "",
                    "§eEmits magical particles when placed",
                    "§8§oEnchanted with crafting magic",
                    "",
                    "§a► Place to activate 6x6 crafting"
            ));

            table.setItemMeta(meta);
            return table;
        } catch (Exception e) {
            e.printStackTrace();
            return new ItemStack(Material.CRAFTING_TABLE, 1);
        }
    }

    public static boolean isCustomCraftingTable(ItemStack item) {
        if (item == null || item.getType() != Material.CRAFTING_TABLE || item.getAmount() > 1) {
            return false;
        }

        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return false;

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            return pdc.has(CUSTOM_TABLE_KEY, PersistentDataType.BYTE);
        } catch (Exception e) {
            return false;
        }
    }
}