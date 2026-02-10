package cwr.cutomIterm.recipe;

import cwr.cutomIterm.Entity_Wand.EntityWandPlugin;
import cwr.cutomIterm.Entity_Wand.FlyVoucher;
import cwr.cutomIterm.Entity_Wand.RecipeBook;
import cwr.cutomIterm.Entity_Wand.WandItem;
import cwr.cutomIterm.Entity_Wand.WardenSword;
import cwr.cutomIterm.Entity_Wand.bwo.PowerBow;
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
    public static final NamespacedKey POWER_BOW_RECIPE_KEY = new NamespacedKey("customitems", "power_bow_recipe"); // ADDED

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

    // ADDED: Register Power Bow recipe
    public static void registerPowerBowRecipe(EntityWandPlugin plugin) {
        try {
            ItemStack powerBow = PowerBow.createPowerBow();
            powerBow.setAmount(1);

            // Recipe: b r / eBr / b r
            // b = Blaze Rod, B = Bow, r = Redstone, e = Ender Pearl
            ShapedRecipe recipe = new ShapedRecipe(POWER_BOW_RECIPE_KEY, powerBow);
            recipe.shape("b r", "eBr", "b r");
            recipe.setIngredient('b', Material.BLAZE_ROD);
            recipe.setIngredient('B', Material.BOW);
            recipe.setIngredient('r', Material.REDSTONE);
            recipe.setIngredient('e', Material.ENDER_PEARL);

            if (Bukkit.getRecipe(POWER_BOW_RECIPE_KEY) != null) {
                Bukkit.removeRecipe(POWER_BOW_RECIPE_KEY);
            }
            Bukkit.addRecipe(recipe);
            plugin.getLogger().info("Power of Gamiya Bow recipe registered with key: " + POWER_BOW_RECIPE_KEY);

        } catch (Exception e) {
            plugin.getLogger().severe("Could not register Power Bow recipe: " + e.getMessage());
        }
    }
}