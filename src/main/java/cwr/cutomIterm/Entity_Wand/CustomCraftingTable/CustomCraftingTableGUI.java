package cwr.cutomIterm.Entity_Wand.CustomCraftingTable;

import cwr.cutomIterm.Entity_Wand.EntityWandPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class CustomCraftingTableGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Player player;

    // Crafting grid slots (6x6 = 36 slots)
    private final int[] craftingSlots = {
            10, 11, 12, 13, 14, 15,
            19, 20, 21, 22, 23, 24,
            28, 29, 30, 31, 32, 33,
            37, 38, 39, 40, 41, 42,
            46, 47, 48, 49, 50, 51,
            55, 56, 57, 58, 59, 60
    };

    // Result slot
    private final int resultSlot = 25;

    public CustomCraftingTableGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 72, "§6§lAdvanced Crafting Table");
        setupGUI();
    }

    private void setupGUI() {
        // Clear inventory
        inventory.clear();

        // Fill borders with glass panes
        for (int i = 0; i < inventory.getSize(); i++) {
            if (!isCraftingSlot(i) && i != resultSlot) {
                inventory.setItem(i, createBorderItem());
            }
        }

        // Add crafting slot indicators
        for (int slot : craftingSlots) {
            inventory.setItem(slot, createCraftingSlotIndicator());
        }

        // Add result slot indicator
        inventory.setItem(resultSlot, createResultSlotIndicator());

        // Add info items
        inventory.setItem(8, createInfoItem());
        inventory.setItem(53, createCloseItem());

        // Add crafting guide
        inventory.setItem(45, createGuideItem());
    }

    private boolean isCraftingSlot(int slot) {
        for (int craftingSlot : craftingSlots) {
            if (craftingSlot == slot) {
                return true;
            }
        }
        return false;
    }

    private ItemStack createBorderItem() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            pane.setItemMeta(meta);
        }
        return pane;
    }

    private ItemStack createCraftingSlotIndicator() {
        ItemStack item = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7Crafting Slot");
            meta.setLore(Arrays.asList(
                    "§8Place ingredients here",
                    "§86x6 crafting grid"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createResultSlotIndicator() {
        ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eResult Slot");
            meta.setLore(Arrays.asList(
                    "§7Crafted items appear here",
                    "§8Shift-click to craft quickly"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createInfoItem() {
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lAdvanced Crafting");
            meta.setLore(Arrays.asList(
                    "§76x6 Crafting Grid",
                    "§7Supports complex recipes",
                    "",
                    "§eHow to use:",
                    "§7• Place ingredients in grid",
                    "§7• Result appears in center",
                    "§7• Click result to craft",
                    "",
                    "§8Special recipes available"
            ));
            info.setItemMeta(meta);
        }
        return info;
    }

    private ItemStack createCloseItem() {
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta meta = close.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lClose");
            meta.setLore(Arrays.asList("§7Click to close crafting table"));
            close.setItemMeta(meta);
        }
        return close;
    }

    private ItemStack createGuideItem() {
        ItemStack guide = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta meta = guide.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§lCrafting Guide");
            meta.setLore(Arrays.asList(
                    "§7Supported recipes:",
                    "§8• Advanced Tools",
                    "§8• Magical Items",
                    "§8• Custom Weapons",
                    "§8• Special Blocks",
                    "",
                    "§aCheck recipe book for details"
            ));
            guide.setItemMeta(meta);
        }
        return guide;
    }

    public void open() {
        player.openInventory(inventory);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 0.8f, 1.2f);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getRawSlot() == 53) { // Close button
            player.closeInventory();
            return;
        }

        // Allow clicking in crafting slots
        if (isCraftingSlot(event.getRawSlot())) {
            event.setCancelled(false);
            // Schedule recipe check
            Bukkit.getScheduler().runTaskLater(EntityWandPlugin.getInstance(), this::checkRecipe, 1L);
        }

        // Handle result slot click
        if (event.getRawSlot() == resultSlot) {
            ItemStack result = inventory.getItem(resultSlot);
            if (result != null && result.getType() != Material.YELLOW_STAINED_GLASS_PANE) {
                // Give item to player
                player.getInventory().addItem(result.clone());

                // Clear crafting grid
                for (int slot : craftingSlots) {
                    ItemStack item = inventory.getItem(slot);
                    if (item != null && item.getType() != Material.LIGHT_GRAY_STAINED_GLASS_PANE) {
                        item.setAmount(item.getAmount() - 1);
                        if (item.getAmount() <= 0) {
                            inventory.setItem(slot, createCraftingSlotIndicator());
                        }
                    }
                }

                // Clear result slot
                inventory.setItem(resultSlot, createResultSlotIndicator());

                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }
        }
    }

    private void checkRecipe() {
        // Get items from crafting grid
        ItemStack[] matrix = new ItemStack[36];
        for (int i = 0; i < craftingSlots.length; i++) {
            ItemStack item = inventory.getItem(craftingSlots[i]);
            if (item != null && item.getType() != Material.LIGHT_GRAY_STAINED_GLASS_PANE) {
                matrix[i] = item.clone();
                matrix[i].setAmount(1); // Normalize to single items for comparison
            } else {
                matrix[i] = null;
            }
        }

        // Check for recipes
        ItemStack result = checkAdvancedRecipes(matrix);

        if (result != null) {
            inventory.setItem(resultSlot, result);
        } else {
            inventory.setItem(resultSlot, createResultSlotIndicator());
        }
    }

    private ItemStack checkAdvancedRecipes(ItemStack[] matrix) {
        // Here you would implement your 6x6 recipes
        // Example: Check for a custom sword recipe
        // This is where you'd add your custom 6x6 recipes

        // For now, return null (no recipe found)
        return null;
    }

    public void handleDrag(InventoryDragEvent event) {
        // Check if any dragged slot is a crafting slot
        for (int slot : event.getRawSlots()) {
            if (isCraftingSlot(slot) || slot == resultSlot) {
                event.setCancelled(true);
                return;
            }
        }
    }

    public void handleClose(InventoryCloseEvent event) {
        // Return items from crafting grid to player
        for (int slot : craftingSlots) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.LIGHT_GRAY_STAINED_GLASS_PANE) {
                player.getInventory().addItem(item);
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}