package cwr.cutomIterm.recipe;

import cwr.cutomIterm.Entity_Wand.EntityWandPlugin;
import cwr.cutomIterm.Entity_Wand.FlyVoucher;
import cwr.cutomIterm.Entity_Wand.RecipeBook;
import cwr.cutomIterm.Entity_Wand.WandItem;
import cwr.cutomIterm.Entity_Wand.WardenSword;
import cwr.cutomIterm.Entity_Wand.CustomCraftingTable.CustomCraftingTable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;


public class RecipeManager {

    // Public constants for recipe keys
    public static final NamespacedKey WAND_RECIPE_KEY = new NamespacedKey("customitems", "entity_wand_recipe");
    public static final NamespacedKey SWORD_RECIPE_KEY = new NamespacedKey("customitems", "warden_sword_recipe");
    public static final NamespacedKey VOUCHER_RECIPE_KEY = new NamespacedKey("customitems", "fly_voucher_recipe");
    public static final NamespacedKey RECIPE_BOOK_RECIPE_KEY = new NamespacedKey("customitems", "recipe_book_recipe");
    public static final NamespacedKey CUSTOM_TABLE_RECIPE_KEY = new NamespacedKey("customitems", "custom_crafting_table_recipe");

    public static void registerWandRecipe(EntityWandPlugin plugin) {
        ItemStack wand = WandItem.createWand();
        wand.setAmount(1);

        ShapedRecipe recipe = new ShapedRecipe(WAND_RECIPE_KEY, wand);
        recipe.shape("GGG", "GSG", "GGG");
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('S', Material.STICK);

        try {
            if (Bukkit.getRecipe(WAND_RECIPE_KEY) != null) {
                Bukkit.removeRecipe(WAND_RECIPE_KEY);
            }
            Bukkit.addRecipe(recipe);
            plugin.getLogger().info("Entity Wand recipe registered with key: " + WAND_RECIPE_KEY);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not register recipe: " + e.getMessage());
        }
    }

    public static void registerWardenSwordRecipe(EntityWandPlugin plugin) {
        ItemStack wardenSword = WardenSword.createWardenSword();
        wardenSword.setAmount(1);

        ShapedRecipe recipe = new ShapedRecipe(SWORD_RECIPE_KEY, wardenSword);
        recipe.shape(" E ", " E ", " S ");
        recipe.setIngredient('E', Material.ECHO_SHARD);
        recipe.setIngredient('S', Material.SCULK_CATALYST);

        try {
            if (Bukkit.getRecipe(SWORD_RECIPE_KEY) != null) {
                Bukkit.removeRecipe(SWORD_RECIPE_KEY);
            }
            Bukkit.addRecipe(recipe);
            plugin.getLogger().info("Warden Sword recipe registered with key: " + SWORD_RECIPE_KEY);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not register Warden Sword recipe: " + e.getMessage());
        }
    }

    public static void registerFlyVoucherRecipe(EntityWandPlugin plugin) {
        ItemStack flyVoucher = FlyVoucher.createFlyVoucher();
        flyVoucher.setAmount(1);

        ShapedRecipe recipe = new ShapedRecipe(VOUCHER_RECIPE_KEY, flyVoucher);
        recipe.shape("BBB", "BFB", "EDE");
        recipe.setIngredient('B', Material.EXPERIENCE_BOTTLE);
        recipe.setIngredient('F', Material.FEATHER);
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('D', Material.DIAMOND);

        try {
            if (Bukkit.getRecipe(VOUCHER_RECIPE_KEY) != null) {
                Bukkit.removeRecipe(VOUCHER_RECIPE_KEY);
            }
            Bukkit.addRecipe(recipe);
            plugin.getLogger().info("Fly Voucher recipe registered with key: " + VOUCHER_RECIPE_KEY);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not register Fly Voucher recipe: " + e.getMessage());
        }
    }

    public static void registerRecipeBookRecipe(EntityWandPlugin plugin) {
        try {
            ItemStack recipeBook = RecipeBook.createRecipeBook();
            recipeBook.setAmount(1);

            ShapedRecipe recipe = new ShapedRecipe(RECIPE_BOOK_RECIPE_KEY, recipeBook);
            recipe.shape("   ", " BC", "   ");
            recipe.setIngredient('B', Material.BOOK);
            recipe.setIngredient('C', Material.CRAFTING_TABLE);

            if (Bukkit.getRecipe(RECIPE_BOOK_RECIPE_KEY) != null) {
                Bukkit.removeRecipe(RECIPE_BOOK_RECIPE_KEY);
            }
            Bukkit.addRecipe(recipe);
            plugin.getLogger().info("Recipe Book crafting recipe registered with key: " + RECIPE_BOOK_RECIPE_KEY);

        } catch (Exception e) {
            plugin.getLogger().severe("Could not register Recipe Book recipe: " + e.getMessage());
        }
    }

    public static void registerCustomCraftingTableRecipe(EntityWandPlugin plugin) {
        ItemStack customTable = CustomCraftingTable.createCustomCraftingTable();
        customTable.setAmount(1);

        ShapedRecipe recipe = new ShapedRecipe(CUSTOM_TABLE_RECIPE_KEY, customTable);

        // Recipe as requested:
        // Row 1: "ODO" (Obsidian, Diamond, Obsidian)
        // Row 2: "DCD" (Diamond, Crafting Table, Diamond)
        // Row 3: "ODO" (Obsidian, Diamond, Obsidian)
        recipe.shape("ODO", "DCD", "ODO");

        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('C', Material.CRAFTING_TABLE);

        try {
            if (Bukkit.getRecipe(CUSTOM_TABLE_RECIPE_KEY) != null) {
                Bukkit.removeRecipe(CUSTOM_TABLE_RECIPE_KEY);
            }
            Bukkit.addRecipe(recipe);
            plugin.getLogger().info("Custom Crafting Table recipe registered with key: " + CUSTOM_TABLE_RECIPE_KEY);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not register Custom Crafting Table recipe: " + e.getMessage());
        }
    }
}