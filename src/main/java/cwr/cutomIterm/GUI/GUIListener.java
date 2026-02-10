package cwr.cutomIterm.GUI;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        if (event.getInventory().getHolder() instanceof CustomGUI customGUI) {
            customGUI.handleClick(event);
        } else if (event.getInventory().getHolder() instanceof RecipeGUI recipeGUI) {
            recipeGUI.handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Optional cleanup if needed
    }
}