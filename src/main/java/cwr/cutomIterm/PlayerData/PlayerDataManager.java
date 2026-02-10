package cwr.cutomIterm.PlayerData;

import cwr.cutomIterm.Entity_Wand.EntityWandPlugin;
import cwr.cutomIterm.Entity_Wand.RecipeBook;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerDataManager {

    private static PlayerDataManager instance;
    private final EntityWandPlugin plugin;
    private final Set<UUID> playersWithRecipeBook;
    private File dataFile;
    private FileConfiguration dataConfig;

    private PlayerDataManager(EntityWandPlugin plugin) {
        this.plugin = plugin;
        this.playersWithRecipeBook = new HashSet<>();
        loadPlayerData();
    }

    public static PlayerDataManager getInstance() {
        if (instance == null) {
            instance = new PlayerDataManager(EntityWandPlugin.getInstance());
        }
        return instance;
    }

    private void loadPlayerData() {
        try {
            dataFile = new File(plugin.getDataFolder(), "player_data.yml");
            if (!dataFile.exists()) {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            }

            dataConfig = YamlConfiguration.loadConfiguration(dataFile);

            // Load players who already received recipe book
            if (dataConfig.contains("players_with_recipe_book")) {
                for (String uuidString : dataConfig.getStringList("players_with_recipe_book")) {
                    try {
                        playersWithRecipeBook.add(UUID.fromString(uuidString));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID in player data: " + uuidString);
                    }
                }
            }

            plugin.getLogger().info("Loaded " + playersWithRecipeBook.size() + " players with recipe books");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load player data: " + e.getMessage());
        }
    }

    private void savePlayerData() {
        try {
            // Convert UUIDs to strings
            java.util.List<String> uuidStrings = new java.util.ArrayList<>();
            for (UUID uuid : playersWithRecipeBook) {
                uuidStrings.add(uuid.toString());
            }

            dataConfig.set("players_with_recipe_book", uuidStrings);
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
        }
    }

    public boolean hasReceivedRecipeBook(Player player) {
        return playersWithRecipeBook.contains(player.getUniqueId());
    }

    public void markRecipeBookReceived(Player player) {
        if (!hasReceivedRecipeBook(player)) {
            playersWithRecipeBook.add(player.getUniqueId());
            savePlayerData();
            plugin.getLogger().info("Marked " + player.getName() + " as having received recipe book");
        }
    }

    public void giveRecipeBookIfFirstJoin(Player player) {
        if (!hasReceivedRecipeBook(player)) {
            // Give the recipe book
            player.getInventory().addItem(RecipeBook.createRecipeBook());

            // Mark as received
            markRecipeBookReceived(player);

            // Send welcome message
            sendWelcomeMessage(player);

            plugin.getLogger().info("Gave recipe book to first-time player: " + player.getName());
        }
    }

    private void sendWelcomeMessage(Player player) {
        player.sendMessage("§6§lWelcome to the server! §eYou received a §6Custom Recipe Book§e!");
        player.sendMessage("§7Right-click with the book to view all custom item recipes.");
        player.sendMessage("§7You can also use §e/customrecipes §7to open the recipe GUI.");
    }

    public void reload() {
        loadPlayerData();
    }

    public Set<UUID> getPlayersWithRecipeBook() {
        return new HashSet<>(playersWithRecipeBook);
    }

    public int getTotalPlayersWithBooks() {
        return playersWithRecipeBook.size();
    }

    public boolean forceGiveRecipeBook(Player player) {
        if (!hasReceivedRecipeBook(player)) {
            giveRecipeBookIfFirstJoin(player);
            return true;
        } else {
            // Player already has one, but give another if inventory has space
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(RecipeBook.createRecipeBook());
                player.sendMessage("§aYou received another Recipe Book!");
                return true;
            } else {
                player.sendMessage("§cYour inventory is full!");
                return false;
            }
        }
    }

    public void clearPlayerData(Player player) {
        if (playersWithRecipeBook.remove(player.getUniqueId())) {
            savePlayerData();
            plugin.getLogger().info("Cleared recipe book data for: " + player.getName());
        }
    }
}