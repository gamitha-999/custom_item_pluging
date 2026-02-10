package cwr.cutomIterm.GUI;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GUIConfigManager {

    private static GUIConfigManager instance;
    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;

    private GUIConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public static GUIConfigManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new GUIConfigManager(plugin);
        }
        return instance;
    }

    private void loadConfig() {
        try {
            configFile = new File(plugin.getDataFolder(), "gui_config.yml");

            if (!configFile.exists()) {
                plugin.saveResource("gui_config.yml", false);
                plugin.getLogger().info("Created gui_config.yml");
            }

            config = YamlConfiguration.loadConfiguration(configFile);
            plugin.getLogger().info("Loaded GUI configuration");

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load GUI config: " + e.getMessage());
        }
    }

    public void reloadConfig() {
        loadConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    // Header methods
    public boolean isHeaderEnabled() {
        return config.getBoolean("header.enabled", true);
    }

    public ItemStack getHeaderItem() {
        String materialName = config.getString("header.material", "GRAY_STAINED_GLASS_PANE");
        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) material = Material.GRAY_STAINED_GLASS_PANE;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    config.getString("header.name", " ")));
            item.setItemMeta(meta);
        }

        return item;
    }

    // Footer methods
    public boolean isFooterEnabled() {
        return config.getBoolean("footer.enabled", true);
    }

    public ItemStack getLeftArrow() {
        String materialName = config.getString("footer.left_arrow.material", "ARROW");
        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) material = Material.ARROW;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    config.getString("footer.left_arrow.name", "&a&l← Previous Page")));

            List<String> lore = new ArrayList<>();
            for (String line : config.getStringList("footer.left_arrow.lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(lore);

            item.setItemMeta(meta);
        }

        return item;
    }

    public ItemStack getRightArrow() {
        String materialName = config.getString("footer.right_arrow.material", "ARROW");
        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) material = Material.ARROW;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    config.getString("footer.right_arrow.name", "&a&lNext Page →")));

            List<String> lore = new ArrayList<>();
            for (String line : config.getStringList("footer.right_arrow.lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(lore);

            item.setItemMeta(meta);
        }

        return item;
    }

    public ItemStack getInfoBook(int totalItems) {
        String materialName = config.getString("footer.info_book.material", "BOOK");
        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) material = Material.BOOK;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    config.getString("footer.info_book.name", "&6&lMod Information")));

            List<String> lore = new ArrayList<>();
            for (String line : config.getStringList("footer.info_book.lore")) {
                line = line.replace("%total_items%", String.valueOf(totalItems));
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(lore);

            item.setItemMeta(meta);
        }

        return item;
    }

    public ItemStack getCloseButton() {
        String materialName = config.getString("footer.close_button.material", "BARRIER");
        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) material = Material.BARRIER;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    config.getString("footer.close_button.name", "&c&lClose")));

            List<String> lore = new ArrayList<>();
            for (String line : config.getStringList("footer.close_button.lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(lore);

            item.setItemMeta(meta);
        }

        return item;
    }

    // Display methods
    public String getGuiTitle() {
        return ChatColor.translateAlternateColorCodes('&',
                config.getString("display.gui_title", "&6&lCustom Recipes"));
    }

    public int getItemsPerPage() {
        return config.getInt("display.items_per_page", 36);
    }

    public int getTotalRows() {
        return config.getInt("display.rows", 6);
    }

    public int getInventorySize() {
        return getTotalRows() * 9;
    }

    // Customization methods
    public Material getBorderMaterial() {
        String materialName = config.getString("customization.border_color", "GRAY_STAINED_GLASS_PANE");
        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) material = Material.GRAY_STAINED_GLASS_PANE;
        return material;
    }

    public ItemStack getEmptyPane() {
        Material material = getBorderMaterial();
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            pane.setItemMeta(meta);
        }
        return pane;
    }
}