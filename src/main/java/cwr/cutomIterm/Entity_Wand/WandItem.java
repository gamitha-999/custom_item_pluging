package cwr.cutomIterm.Entity_Wand;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.UUID;

public class WandItem {

    private static final int MAX_DURABILITY = 1000;

    public static ItemStack createWand() {
        try {
            // Create wand with stack size of 1
            ItemStack wand = new ItemStack(Material.STICK, 1); // Explicitly set amount to 1
            ItemMeta meta = wand.getItemMeta();

            if (meta == null) {
                return wand;
            }

            // Custom name
            meta.setDisplayName("§bEntity Wand");

            // Set enchantment glint override (1.21.1 API)
            meta.setEnchantmentGlintOverride(true);

            // MARK AS NON-STACKABLE - THIS IS CRITICAL
            // Method 1: Use setMaxStackSize if available (Bukkit 1.20.5+)
            try {
                meta.setMaxStackSize(1);
            } catch (NoSuchMethodError e) {
                // Fallback for older versions
            }

            // Mark as unique wand using PersistentDataContainer
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(EntityWandPlugin.WAND_KEY, PersistentDataType.BYTE, (byte) 1);
            pdc.set(EntityWandPlugin.DURABILITY_KEY, PersistentDataType.INTEGER, MAX_DURABILITY);

            // ADD UNIQUE ID TO PREVENT STACKING
            pdc.set(new NamespacedKey(EntityWandPlugin.getInstance(), "unique_id"),
                    PersistentDataType.STRING,
                    UUID.randomUUID().toString());

            // Set lore with durability
            meta.setLore(Arrays.asList(
                    "§7Hold right-click to move entities",
                    "§7Durability: §e" + MAX_DURABILITY + "/" + MAX_DURABILITY,
                    "§8§oUnique Item - Cannot be stacked"
            ));

            wand.setItemMeta(meta);

            // Ensure amount is 1
            wand.setAmount(1);
            return wand;
        } catch (Exception e) {
            e.printStackTrace();
            ItemStack fallback = new ItemStack(Material.STICK, 1); // Always 1
            fallback.setAmount(1); // Double ensure
            return fallback;
        }
    }

    public static boolean isWand(ItemStack item) {
        if (item == null || item.getType() != Material.STICK || item.getAmount() > 1) {
            return false; // Reject if stack size > 1
        }

        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return false;

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            return pdc.has(EntityWandPlugin.WAND_KEY, PersistentDataType.BYTE);
        } catch (Exception e) {
            return false;
        }
    }

    public static int getDurability(ItemStack wand) {
        if (!isWand(wand)) return -1;

        try {
            ItemMeta meta = wand.getItemMeta();
            if (meta == null) return -1;

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            Integer durability = pdc.get(EntityWandPlugin.DURABILITY_KEY, PersistentDataType.INTEGER);
            return durability != null ? durability : MAX_DURABILITY;
        } catch (Exception e) {
            return MAX_DURABILITY;
        }
    }

    public static boolean setDurability(ItemStack wand, int durability) {
        if (!isWand(wand)) return false;

        try {
            ItemMeta meta = wand.getItemMeta();
            if (meta == null) return false;

            PersistentDataContainer pdc = meta.getPersistentDataContainer();

            int newDurability = Math.max(0, Math.min(MAX_DURABILITY, durability));
            pdc.set(EntityWandPlugin.DURABILITY_KEY, PersistentDataType.INTEGER, newDurability);

            // Update lore
            meta.setLore(Arrays.asList(
                    "§7Hold right-click to move entities",
                    "§7Durability: §e" + newDurability + "/" + MAX_DURABILITY,
                    "§8§oUnique Item - Cannot be stacked"
            ));

            wand.setItemMeta(meta);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean decreaseDurability(Player player, ItemStack wand, int amount) {
        // First check stack size
        if (wand.getAmount() > 1) {
            // Split stack - create a single wand
            wand.setAmount(wand.getAmount() - 1);
            ItemStack singleWand = createWand();
            if (player != null && player.isOnline()) {
                player.getInventory().addItem(singleWand);
            }
            return false;
        }

        int currentDurability = getDurability(wand);
        if (currentDurability <= 0) return false;

        int newDurability = currentDurability - amount;

        if (newDurability <= 0) {
            // Wand broke - play sound and remove item
            if (player != null && player.isOnline()) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 1.0f);
            }
            wand.setAmount(0); // Remove item
            return false;
        }

        return setDurability(wand, newDurability);
    }
}