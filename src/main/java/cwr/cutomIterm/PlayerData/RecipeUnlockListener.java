package cwr.cutomIterm.PlayerData;

import cwr.cutomIterm.Entity_Wand.EntityWandPlugin;
import cwr.cutomIterm.recipe.RecipeManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class RecipeUnlockListener implements Listener {

    private final EntityWandPlugin plugin;

    public RecipeUnlockListener(EntityWandPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Material pickedUpMaterial = event.getItem().getItemStack().getType();

        // Check for Entity Wand recipe unlocks
        if (pickedUpMaterial == Material.GOLD_INGOT || pickedUpMaterial == Material.STICK) {
            unlockEntityWandRecipe(player);
        }

        // Check for Warden Sword recipe unlocks
        if (pickedUpMaterial == Material.ECHO_SHARD || pickedUpMaterial == Material.SCULK_CATALYST) {
            unlockWardenSwordRecipe(player);
        }

        // Check for Fly Voucher recipe unlocks
        if (pickedUpMaterial == Material.FEATHER ||
                pickedUpMaterial == Material.EXPERIENCE_BOTTLE ||
                pickedUpMaterial == Material.EMERALD ||
                pickedUpMaterial == Material.DIAMOND) {
            unlockFlyVoucherRecipe(player);
        }

        // Check for Recipe Book recipe unlocks
        if (pickedUpMaterial == Material.BOOK || pickedUpMaterial == Material.CRAFTING_TABLE) {
            unlockRecipeBookRecipe(player);
        }

        // REMOVED: Custom Crafting Table recipe unlock check
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        checkAndUnlockRecipes(player);
    }

    private void checkAndUnlockRecipes(Player player) {
        boolean hasGoldOrStick = player.getInventory().contains(Material.GOLD_INGOT) ||
                player.getInventory().contains(Material.STICK);
        boolean hasEchoOrCatalyst = player.getInventory().contains(Material.ECHO_SHARD) ||
                player.getInventory().contains(Material.SCULK_CATALYST);
        boolean hasVoucherIngredient = player.getInventory().contains(Material.FEATHER) ||
                player.getInventory().contains(Material.EXPERIENCE_BOTTLE) ||
                player.getInventory().contains(Material.EMERALD) ||
                player.getInventory().contains(Material.DIAMOND);
        boolean hasRecipeBookIngredient = player.getInventory().contains(Material.BOOK) ||
                player.getInventory().contains(Material.CRAFTING_TABLE);

        // REMOVED: hasCustomTableIngredient check

        if (hasGoldOrStick) {
            unlockEntityWandRecipe(player);
        }
        if (hasEchoOrCatalyst) {
            unlockWardenSwordRecipe(player);
        }
        if (hasVoucherIngredient) {
            unlockFlyVoucherRecipe(player);
        }
        if (hasRecipeBookIngredient) {
            unlockRecipeBookRecipe(player);
        }
        // REMOVED: unlockCustomCraftingTableRecipe call
    }

    private void unlockEntityWandRecipe(Player player) {
        try {
            if (player.hasDiscoveredRecipe(RecipeManager.WAND_RECIPE_KEY)) {
                return;
            }

            if (plugin.getServer().getRecipe(RecipeManager.WAND_RECIPE_KEY) != null) {
                player.discoverRecipe(RecipeManager.WAND_RECIPE_KEY);
                plugin.getLogger().info("Unlocked Entity Wand recipe for player: " + player.getName());

                player.sendMessage("§aYou've discovered the §bEntity Wand§a recipe!");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to unlock Entity Wand recipe: " + e.getMessage());
        }
    }

    private void unlockWardenSwordRecipe(Player player) {
        try {
            if (player.hasDiscoveredRecipe(RecipeManager.SWORD_RECIPE_KEY)) {
                return;
            }

            if (plugin.getServer().getRecipe(RecipeManager.SWORD_RECIPE_KEY) != null) {
                player.discoverRecipe(RecipeManager.SWORD_RECIPE_KEY);
                plugin.getLogger().info("Unlocked Warden Sword recipe for player: " + player.getName());

                player.sendMessage("§aYou've discovered the §5Warden Sword§a recipe!");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to unlock Warden Sword recipe: " + e.getMessage());
        }
    }

    private void unlockFlyVoucherRecipe(Player player) {
        try {
            if (player.hasDiscoveredRecipe(RecipeManager.VOUCHER_RECIPE_KEY)) {
                return;
            }

            if (plugin.getServer().getRecipe(RecipeManager.VOUCHER_RECIPE_KEY) != null) {
                player.discoverRecipe(RecipeManager.VOUCHER_RECIPE_KEY);
                plugin.getLogger().info("Unlocked Fly Voucher recipe for player: " + player.getName());

                player.sendMessage("§aYou've discovered the §bFly Voucher§a recipe!");
                player.sendMessage("§7Craft it with: §eExp Bottles, Feather, Emerald, Diamond");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to unlock Fly Voucher recipe: " + e.getMessage());
        }
    }

    private void unlockRecipeBookRecipe(Player player) {
        try {
            if (player.hasDiscoveredRecipe(RecipeManager.RECIPE_BOOK_RECIPE_KEY)) {
                return;
            }

            if (plugin.getServer().getRecipe(RecipeManager.RECIPE_BOOK_RECIPE_KEY) != null) {
                player.discoverRecipe(RecipeManager.RECIPE_BOOK_RECIPE_KEY);
                plugin.getLogger().info("Unlocked Recipe Book recipe for player: " + player.getName());

                player.sendMessage("§aYou've discovered the §6Recipe Book§a recipe!");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to unlock Recipe Book recipe: " + e.getMessage());
        }
    }

    // REMOVED: unlockCustomCraftingTableRecipe method

    public static void unlockAllCustomRecipes(Player player, EntityWandPlugin plugin) {
        try {
            player.discoverRecipe(RecipeManager.WAND_RECIPE_KEY);
            player.discoverRecipe(RecipeManager.SWORD_RECIPE_KEY);
            player.discoverRecipe(RecipeManager.VOUCHER_RECIPE_KEY);
            player.discoverRecipe(RecipeManager.RECIPE_BOOK_RECIPE_KEY);
            // REMOVED: CUSTOM_TABLE_RECIPE_KEY

            plugin.getLogger().info("Manually unlocked all custom recipes for player: " + player.getName());
            player.sendMessage("§aAll custom recipes have been unlocked!");

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to unlock all custom recipes: " + e.getMessage());
        }
    }
}