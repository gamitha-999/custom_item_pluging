package cwr.cutomIterm.Entity_Wand;

import cwr.cutomIterm.GUI.CustomRecipesCommand;
import cwr.cutomIterm.GUI.GUIListener;
import cwr.cutomIterm.GUI.GUIConfigManager;
import cwr.cutomIterm.PlayerData.PlayerDataManager;
import cwr.cutomIterm.PlayerData.PlayerJoinListener;
import cwr.cutomIterm.PlayerData.RecipeUnlockListener;
import cwr.cutomIterm.recipe.RecipeManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class EntityWandPlugin extends JavaPlugin {

    private static EntityWandPlugin instance;
    public static NamespacedKey WAND_KEY;
    public static NamespacedKey DURABILITY_KEY;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        instance = this;
        WAND_KEY = new NamespacedKey(this, "entity_wand");
        DURABILITY_KEY = new NamespacedKey(this, "wand_durability");

        try {
            // Initialize player data manager
            playerDataManager = PlayerDataManager.getInstance();

            // Register all custom recipes
            RecipeManager.registerWandRecipe(this);
            RecipeManager.registerWardenSwordRecipe(this);
            RecipeManager.registerFlyVoucherRecipe(this);
            RecipeManager.registerRecipeBookRecipe(this);
            RecipeManager.registerPowerBowRecipe(this); // ADD THIS LINE

            // Register event listeners
            getServer().getPluginManager().registerEvents(new GUIListener(), this);
            getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
            getServer().getPluginManager().registerEvents(new EntityWandListener(), this);

            // Register the recipe unlock listener
            getServer().getPluginManager().registerEvents(new RecipeUnlockListener(this), this);

            // Register commands
            getCommand("customrecipes").setExecutor(new CustomRecipesCommand());

            // Only register customitem command
            if (getCommand("customitem") != null) {
                getCommand("customitem").setExecutor(new EntityWandCommand());
                getCommand("customitem").setTabCompleter(new EntityWandCommand());
            }

            // Initialize GUI config manager
            GUIConfigManager.getInstance(this);

            // Start movement manager
            EntityMovementManager.start(this);

            getLogger().info("=======================================");
            getLogger().info("EntityWand Plugin Enabled!");
            getLogger().info("Recipe Unlock System: ACTIVE");
            getLogger().info("GUI Configuration: LOADED");
            getLogger().info("- Entity Wand: Pick up Gold Ingot or Stick");
            getLogger().info("- Warden Sword: Pick up Echo Shard or Sculk Catalyst");
            getLogger().info("- Fly Voucher: Pick up Feather, Exp Bottle, Emerald, or Diamond");
            getLogger().info("- Recipe Book: Pick up Book or Crafting Table");
            getLogger().info("- Power of Gamiya: Pick up Blaze Rod, Bow, Redstone, or Ender Pearl"); // ADDED
            getLogger().info("Loaded " + playerDataManager.getTotalPlayersWithBooks() + " players with recipe books");
            getLogger().info("=======================================");

        } catch (Exception e) {
            getLogger().severe("Failed to enable: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        EntityMovementManager.stop();
        WardenSword.clearAllCooldowns();
        FlyVoucher.clearAllFlightTimers();
        getLogger().info("EntityWand has been disabled!");
    }

    public static EntityWandPlugin getInstance() {
        return instance;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}