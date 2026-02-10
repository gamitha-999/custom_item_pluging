package cwr.cutomIterm.Entity_Wand;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class EntityWandListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        try {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            Action action = event.getAction();

            // Handle recipe book
            if (RecipeBook.isRecipeBook(item)) {
                if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                    event.setCancelled(true);
                    // Open the recipe GUI
                    cwr.cutomIterm.GUI.CustomGUI gui = new cwr.cutomIterm.GUI.CustomGUI();
                    gui.open(player);
                    return;
                }
            }

            // Handle warden sword ability
            if (WardenSword.isWardenSword(item)) {
                if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                    event.setCancelled(true);
                    WardenSword.useSonicBoomAbility(player, item);
                    return;
                }
            }

            // NEW: Handle Fly Voucher
            if (FlyVoucher.isFlyVoucher(item)) {
                if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                    event.setCancelled(true);
                    FlyVoucher.useFlyVoucher(player, item);
                    return;
                }
            }

            // Handle wand (existing code)
            if (!WandItem.isWand(item) || item.getAmount() > 1) {
                return;
            }

            UUID playerId = player.getUniqueId();

            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                // Update right-click time to keep holding entity
                EntityMovementManager.updateRightClick(playerId);
            } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                // Left click releases entity
                EntityMovementManager.releaseEntity(playerId);
            }
        } catch (Exception e) {
            // Silent fail
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        try {
            CraftingInventory inventory = event.getInventory();
            ItemStack result = inventory.getResult();

            if (result != null) {
                // Ensure crafted wands are single
                if (WandItem.isWand(result)) {
                    result.setAmount(1);
                    inventory.setResult(result);
                }
                // Ensure crafted recipe books are single
                if (RecipeBook.isRecipeBook(result)) {
                    result.setAmount(1);
                    inventory.setResult(result);
                }
                // Ensure crafted warden swords are single
                if (WardenSword.isWardenSword(result)) {
                    result.setAmount(1);
                    inventory.setResult(result);
                }
                // NEW: Ensure crafted fly vouchers are single
                if (FlyVoucher.isFlyVoucher(result)) {
                    result.setAmount(1);
                    inventory.setResult(result);
                }
            }
        } catch (Exception e) {
            // Silent fail
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            ItemStack currentItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();

            // Prevent stacking wands
            if (currentItem != null && WandItem.isWand(currentItem)) {
                if (cursorItem != null && WandItem.isWand(cursorItem)) {
                    event.setCancelled(true);
                }
            }

            // Prevent stacking recipe books
            if (currentItem != null && RecipeBook.isRecipeBook(currentItem)) {
                if (cursorItem != null && RecipeBook.isRecipeBook(cursorItem)) {
                    event.setCancelled(true);
                }
            }

            // Prevent stacking warden swords
            if (currentItem != null && WardenSword.isWardenSword(currentItem)) {
                if (cursorItem != null && WardenSword.isWardenSword(cursorItem)) {
                    event.setCancelled(true);
                }
            }

            // NEW: Prevent stacking fly vouchers
            if (currentItem != null && FlyVoucher.isFlyVoucher(currentItem)) {
                if (cursorItem != null && FlyVoucher.isFlyVoucher(cursorItem)) {
                    event.setCancelled(true);
                }
            }
        } catch (Exception e) {
            // Silent fail
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        try {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            // Check if holding the wand and ensure it's single
            if (!WandItem.isWand(item) || item.getAmount() > 1) {
                return;
            }

            event.setCancelled(true);

            Entity entity = event.getRightClicked();

            // Don't allow moving players
            if (entity instanceof Player) {
                return;
            }

            // Grab the entity
            EntityMovementManager.grabEntity(player, entity);
        } catch (Exception e) {
            // Silent fail
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        try {
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();

            // Switching items releases entity
            EntityMovementManager.releaseEntity(playerId);

            // Clear warden sword cooldown if switching away from warden sword
            ItemStack previousItem = player.getInventory().getItem(event.getPreviousSlot());
            if (previousItem != null && WardenSword.isWardenSword(previousItem)) {
                // Optionally clear cooldown when switching away
                // This is optional - remove if you want cooldowns to persist
                // WardenSword.clearCooldown(player);
            }
        } catch (Exception e) {
            // Silent fail
        }
    }

    // Note: PlayerQuitEvent is now handled in PlayerJoinListener.java
}