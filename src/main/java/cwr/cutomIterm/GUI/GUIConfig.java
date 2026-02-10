package cwr.cutomIterm.GUI;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GUIConfig {

    private static final Map<String, GUIItem> customItems = new HashMap<>();

    static {
        registerItem("entity_wand", Material.STICK, "§bEntity Wand",
                Arrays.asList("§7Hold right-click to move entities",
                        "§7Durability: §e1000",
                        "",
                        "§aClick to view recipe"));

        registerItem("warden_sword", Material.NETHERITE_SWORD, "§5Warden Sword",
                Arrays.asList("§7Attack Damage: §c11",
                        "§7Attack Speed: §e2.5",
                        "",
                        "§dAbility: Warden Sonic Boom",
                        "§8Right-click to activate (20s cooldown)",
                        "",
                        "§aClick to view recipe"));

        registerItem("fly_voucher", Material.FEATHER, "§b§lFly Voucher",
                Arrays.asList("§7Right-click to gain flight",
                        "§7Duration: §e30 minutes",
                        "§7One-time use item",
                        "",
                        "§aClick to view recipe"));

        registerItem("recipe_book", Material.KNOWLEDGE_BOOK, "§6§lRecipe Book",
                Arrays.asList("§7Right-click to open",
                        "§7Custom Item Recipes",
                        "",
                        "§eContains all custom crafting",
                        "§erecipes for your items",
                        "",
                        "§aClick to view recipe"));

        // ADDED: Power of Gamiya Bow
        registerItem("power_bow", Material.BOW, "§6§lPower of Gamiya",
                Arrays.asList("§6§lDivine Tracking Bow",
                        "",
                        "§7Special Ability: §eAuto-Tracking Arrows",
                        "§8Arrows automatically track nearest entity",
                        "§8and summon lightning on hit",
                        "",
                        "§aClick to view recipe"));
    }

    private static void registerItem(String id, Material material, String name, java.util.List<String> lore) {
        customItems.put(id, new GUIItem(id, material, name, lore));
    }

    public static Map<String, GUIItem> getCustomItems() {
        return new HashMap<>(customItems);
    }

    public static GUIItem getItem(String id) {
        return customItems.get(id);
    }

    public static ItemStack createDisplayItem(GUIItem guiItem) {
        ItemStack item = new ItemStack(guiItem.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(guiItem.getName());
            meta.setLore(guiItem.getLore());

            // Add enchantment glint to special items
            if (guiItem.getId().equals("fly_voucher") ||
                    guiItem.getId().equals("recipe_book") ||
                    guiItem.getId().equals("warden_sword") ||
                    guiItem.getId().equals("power_bow")) {
                meta.setEnchantmentGlintOverride(true);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public static Map<Character, Material> getRecipePattern(String itemId) {
        Map<Character, Material> pattern = new HashMap<>();

        switch (itemId) {
            case "entity_wand":
                pattern.put('G', Material.GOLD_INGOT);
                pattern.put('S', Material.STICK);
                break;

            case "warden_sword":
                pattern.put('E', Material.ECHO_SHARD);
                pattern.put('S', Material.SCULK_CATALYST);
                break;

            case "fly_voucher":
                pattern.put('B', Material.EXPERIENCE_BOTTLE);
                pattern.put('F', Material.FEATHER);
                pattern.put('E', Material.EMERALD);
                pattern.put('D', Material.DIAMOND);
                break;

            case "recipe_book":
                pattern.put('B', Material.BOOK);
                pattern.put('C', Material.CRAFTING_TABLE);
                break;

            // ADDED: Power Bow recipe pattern
            case "power_bow":
                pattern.put('b', Material.BLAZE_ROD);
                pattern.put('B', Material.BOW);
                pattern.put('r', Material.REDSTONE);
                pattern.put('e', Material.ENDER_PEARL);
                break;

            default:
                pattern.put('?', Material.BARRIER);
        }

        return pattern;
    }

    public static String[] getRecipeShape(String itemId) {
        switch (itemId) {
            case "entity_wand":
                return new String[]{"GGG", "GSG", "GGG"};

            case "warden_sword":
                return new String[]{" E ", " E ", " S "};

            case "fly_voucher":
                return new String[]{"BBB", "BFB", "EDE"};

            case "recipe_book":
                return new String[]{"   ", " BC", "   "};

            // ADDED: Power Bow recipe shape (as specified: "b r", "eBr", "b r")
            case "power_bow":
                return new String[]{"b r", "eBr", "b r"};

            default:
                return new String[]{"???", "???", "???"};
        }
    }

    public static String getRecipeDescription(String itemId) {
        switch (itemId) {
            case "entity_wand":
                return "§e8 Gold Ingots around 1 Stick";
            case "warden_sword":
                return "§e2 Echo Shards + 1 Sculk Catalyst";
            case "fly_voucher":
                return "§eExp Bottles + Feather + Emerald + Diamond";
            case "recipe_book":
                return "§eBook + Crafting Table";
            case "power_bow": // ADDED
                return "§eBlaze Rod + Bow + Redstone + Ender Pearl";
            default:
                return "§cRecipe not available";
        }
    }
}