package cwr.cutomIterm.GUI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomGUI implements InventoryHolder {

    private Inventory inventory;
    private final Map<Integer, String> slotToItemId = new HashMap<>();
    private final Map<Integer, Integer> slotToPage = new HashMap<>();
    private int currentPage = 0;
    private int totalPages = 0;
    private final List<GUIItem> allItems;
    private final GUIConfigManager configManager;

    public CustomGUI() {
        this.configManager = GUIConfigManager.getInstance(org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass()));
        this.allItems = new ArrayList<>(GUIConfig.getCustomItems().values());
        calculateTotalPages();
        createInventory();
        populateCurrentPage();
    }

    private void calculateTotalPages() {
        int itemsPerPage = configManager.getItemsPerPage();
        totalPages = (int) Math.ceil((double) allItems.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
    }

    private void createInventory() {
        int size = configManager.getInventorySize();
        String title = configManager.getGuiTitle() + " §7(Page " + (currentPage + 1) + "/" + totalPages + ")";
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    private void populateCurrentPage() {
        inventory.clear();
        slotToItemId.clear();
        slotToPage.clear();

        // Fill header if enabled (row 0, slots 0-8)
        if (configManager.isHeaderEnabled()) {
            for (int i = 0; i < 9; i++) {
                inventory.setItem(i, configManager.getHeaderItem());
            }
        }

        // Calculate start and end indices for current page
        int itemsPerPage = configManager.getItemsPerPage();
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allItems.size());

        // Fill content slots (rows 1-4, slots 9-44)
        int slot = 9; // Start at row 1, column 0

        for (int i = startIndex; i < endIndex; i++) {
            GUIItem guiItem = allItems.get(i);
            ItemStack displayItem = GUIConfig.createDisplayItem(guiItem);

            inventory.setItem(slot, displayItem);
            slotToItemId.put(slot, guiItem.getId());
            slotToPage.put(slot, currentPage);
            slot++;

            // Move to next row if we reach the end of current row
            if (slot % 9 == 0) {
                // Check if we've filled all content rows
                if (slot >= 45) { // 5th row starts at slot 45
                    break;
                }
            }
        }

        // Fill footer if enabled (row 5, slots 45-53)
        if (configManager.isFooterEnabled()) {
            int footerRowStart = 45; // Last row (6th row, 0-indexed)

            // Left arrow (slot 45)
            if (currentPage > 0) {
                inventory.setItem(footerRowStart, configManager.getLeftArrow());
            } else {
                inventory.setItem(footerRowStart, configManager.getEmptyPane());
            }

            // Fill middle slots with empty panes (slots 46-51)
            for (int i = 1; i <= 6; i++) {
                inventory.setItem(footerRowStart + i, configManager.getEmptyPane());
            }

            // Info book (slot 51)
            inventory.setItem(footerRowStart + 6, configManager.getInfoBook(allItems.size()));

            // Close button (slot 52)
            inventory.setItem(footerRowStart + 7, configManager.getCloseButton());

            // Right arrow (slot 53)
            if (currentPage < totalPages - 1) {
                inventory.setItem(footerRowStart + 8, configManager.getRightArrow());
            } else {
                inventory.setItem(footerRowStart + 8, configManager.getEmptyPane());
            }
        }

        // Fill empty slots with panes
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, configManager.getEmptyPane());
            }
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= inventory.getSize()) return;

        // Handle footer buttons (last row: slots 45-53)
        int footerRowStart = 45;

        // Left arrow (slot 45)
        if (slot == footerRowStart && currentPage > 0) {
            currentPage--;
            refreshGUI(player);
            return;
        }

        // Right arrow (slot 53)
        if (slot == footerRowStart + 8 && currentPage < totalPages - 1) {
            currentPage++;
            refreshGUI(player);
            return;
        }

        // Close button (slot 52)
        if (slot == footerRowStart + 7) {
            player.closeInventory();
            return;
        }

        // Info book (slot 51) - do nothing
        if (slot == footerRowStart + 6) {
            return;
        }

        // Handle item click (content area: slots 9-44)
        if (slot >= 9 && slot <= 44) {
            String itemId = slotToItemId.get(slot);
            if (itemId != null && slotToPage.get(slot) == currentPage) {
                RecipeGUI recipeGUI = new RecipeGUI(itemId);
                recipeGUI.open(player);
            }
        }
    }

    private void refreshGUI(Player player) {
        createInventory();
        populateCurrentPage();
        player.updateInventory();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}