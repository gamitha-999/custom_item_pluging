package cwr.cutomIterm.GUI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;

public class RecipeGUI implements InventoryHolder {

    private final Inventory inventory;
    private final String itemId;
    private final GUIItem guiItem;

    public RecipeGUI(String itemId) {
        this.itemId = itemId;
        this.guiItem = GUIConfig.getItem(itemId);
        this.inventory = Bukkit.createInventory(this, 54, "§6§lCrafting Recipe");
        populateRecipeGUI();
    }

    private void populateRecipeGUI() {
        inventory.clear();

        if (guiItem == null) {
            ItemStack error = new ItemStack(Material.BARRIER);
            ItemMeta meta = error.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§cError: Item not found");
                error.setItemMeta(meta);
            }
            inventory.setItem(22, error);
            return;
        }

        // Display the result item
        ItemStack resultItem = GUIConfig.createDisplayItem(guiItem);
        inventory.setItem(25, resultItem);

        // Get recipe pattern
        String[] shape = GUIConfig.getRecipeShape(itemId);
        Map<Character, Material> pattern = GUIConfig.getRecipePattern(itemId);

        // Display crafting grid
        int[] craftingSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        int patternIndex = 0;

        for (int i = 0; i < 3; i++) {
            String row = shape[i];
            for (int j = 0; j < 3; j++) {
                char symbol = row.charAt(j);
                Material material = pattern.get(symbol);

                if (material != null && material != Material.AIR) {
                    ItemStack ingredient = new ItemStack(material);
                    ItemMeta meta = ingredient.getItemMeta();

                    if (meta != null) {
                        meta.setDisplayName("§f" + getMaterialName(material));
                        meta.setLore(Arrays.asList("§7Symbol: §e" + symbol));
                        ingredient.setItemMeta(meta);
                    }

                    inventory.setItem(craftingSlots[patternIndex], ingredient);
                }
                patternIndex++;
            }
        }

        // Add craft arrow
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = arrow.getItemMeta();
        if (arrowMeta != null) {
            arrowMeta.setDisplayName("§aCrafts into →");
            arrow.setItemMeta(arrowMeta);
        }
        inventory.setItem(24, arrow);

        // Add recipe description
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§e§lRecipe Info");
            infoMeta.setLore(Arrays.asList(
                    "§7" + GUIConfig.getRecipeDescription(itemId),
                    "",
                    "§7Pattern:",
                    "§f" + shape[0],
                    "§f" + shape[1],
                    "§f" + shape[2],
                    "",
                    "§8Shift-click in crafting table",
                    "§8to craft this item"
            ));
            info.setItemMeta(infoMeta);
        }
        inventory.setItem(49, info);

        // Add back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§a§l← Back to List");
            backMeta.setLore(Arrays.asList("§7Click to return to item list"));
            back.setItemMeta(backMeta);
        }
        inventory.setItem(45, back);

        // Add close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§c§lClose");
            closeMeta.setLore(Arrays.asList("§7Click to close this menu"));
            close.setItemMeta(closeMeta);
        }
        inventory.setItem(53, close);

        // Fill borders with glass panes
        int[] borderSlots = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,46,47,48,50,51,52};
        for (int slot : borderSlots) {
            ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta paneMeta = pane.getItemMeta();
            if (paneMeta != null) {
                paneMeta.setDisplayName(" ");
                pane.setItemMeta(paneMeta);
            }
            inventory.setItem(slot, pane);
        }
    }

    private String getMaterialName(Material material) {
        String name = material.toString().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= inventory.getSize()) return;

        // Handle back button
        if (slot == 45) {
            CustomGUI mainGUI = new CustomGUI();
            mainGUI.open(player);
            return;
        }

        // Handle close button
        if (slot == 53) {
            player.closeInventory();
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}