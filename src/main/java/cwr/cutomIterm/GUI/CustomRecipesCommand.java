package cwr.cutomIterm.GUI;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CustomRecipesCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command,
                             @Nonnull String label, @Nonnull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (!player.hasPermission("customrecipes.use")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        // Open the main custom recipes GUI
        CustomGUI gui = new CustomGUI();
        gui.open(player);

        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command,
                                      @Nonnull String alias, @Nonnull String[] args) {
        return new ArrayList<>(); // No tab completion needed
    }
}