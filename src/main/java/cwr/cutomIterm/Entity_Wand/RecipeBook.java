package cwr.cutomIterm.Entity_Wand;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.UUID;

public class RecipeBook {

    private static final NamespacedKey RECIPE_BOOK_KEY = new NamespacedKey(EntityWandPlugin.getInstance(), "recipe_book");
    private static final NamespacedKey RECIPE_BOOK_RECIPE_KEY = new NamespacedKey(EntityWandPlugin.getInstance(), "recipe_book_recipe");

    public static ItemStack createRecipeBook() {
        ItemStack book = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta meta = book.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6§lCustom Recipes Book");

            // Add enchantment glow effect
            meta.setEnchantmentGlintOverride(true);

            // Mark as recipe book using PersistentDataContainer
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(RECIPE_BOOK_KEY, PersistentDataType.BYTE, (byte) 1);

            // Add unique ID to prevent stacking
            pdc.set(new NamespacedKey(EntityWandPlugin.getInstance(), "recipe_book_unique"),
                    PersistentDataType.STRING,
                    UUID.randomUUID().toString());

            // Set lore
            meta.setLore(Arrays.asList(
                    "§7Right-click to open",
                    "§7Custom Item Recipes",
                    "",
                    "§eContains all custom crafting",
                    "§erecipes for your items",
                    "",
                    "§8§oAutomatically given on first join"
            ));

            book.setItemMeta(meta);
        }

        return book;
    }

    public static boolean isRecipeBook(ItemStack item) {
        if (item == null || item.getType() != Material.KNOWLEDGE_BOOK || !item.hasItemMeta()) {
            return false;
        }

        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return false;

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            return pdc.has(RECIPE_BOOK_KEY, PersistentDataType.BYTE);
        } catch (Exception e) {
            return false;
        }
    }

    // ADDED: Method to register the recipe book crafting recipe
    public static void registerRecipeBookRecipe() {
        try {
            ItemStack recipeBook = createRecipeBook();
            ShapedRecipe recipe = new ShapedRecipe(RECIPE_BOOK_RECIPE_KEY, recipeBook);

            // Simple recipe: Book surrounded by glowstone dust
            recipe.shape("GGG", "GBG", "GGG");
            recipe.setIngredient('G', Material.GLOWSTONE_DUST);
            recipe.setIngredient('B', Material.BOOK);

            Bukkit.addRecipe(recipe);
            EntityWandPlugin.getInstance().getLogger().info("Recipe Book crafting recipe registered!");
        } catch (Exception e) {
            EntityWandPlugin.getInstance().getLogger().warning("Failed to register recipe book recipe: " + e.getMessage());
        }
    }

    // ADDED: Method to unregister the recipe book crafting recipe
    public static void unregisterRecipeBookRecipe() {
        try {
            Bukkit.removeRecipe(RECIPE_BOOK_RECIPE_KEY);
            EntityWandPlugin.getInstance().getLogger().info("Recipe Book recipe unregistered!");
        } catch (Exception e) {
            EntityWandPlugin.getInstance().getLogger().warning("Failed to unregister recipe book recipe: " + e.getMessage());
        }
    }
}