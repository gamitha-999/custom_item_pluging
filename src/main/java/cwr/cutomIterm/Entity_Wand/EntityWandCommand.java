package cwr.cutomIterm.Entity_Wand;

import cwr.cutomIterm.PlayerData.PlayerDataManager;
import cwr.cutomIterm.PlayerData.RecipeUnlockListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityWandCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command,
                             @Nonnull String label, @Nonnull String[] args) {
        try {
            if (args.length == 0) {
                if (sender instanceof Player player) {
                    player.sendMessage("§6§lCustom Item Commands:");
                    player.sendMessage("§e/customitem give entity_wand §7- Get Entity Wand");
                    player.sendMessage("§e/customitem give warden_sword §7- Get Warden Sword");
                    player.sendMessage("§e/customitem give fly_voucher §7- Get Fly Voucher");
                    player.sendMessage("§e/customitem give recipe_book §7- Get Recipe Book");
                    player.sendMessage("§e/customitem release §7- Release held entity");
                    player.sendMessage("§e/customrecipes §7- Open Recipe GUI");
                    player.sendMessage("§e/customitem flight §7- Check flight time");
                    if (sender.hasPermission("customitem.admin")) {
                        player.sendMessage("§e/customitem unlockrecipes [player] §7- Unlock all recipes");
                        player.sendMessage("§e/customitem stats §7- View plugin statistics");
                        player.sendMessage("§e/customitem reload §7- Reload player data");
                        player.sendMessage("§e/customitem forcebook <player> §7- Force give recipe book");
                        player.sendMessage("§e/customitem cleardata <player> §7- Clear player data");
                    }
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("give")) {
                if (!sender.hasPermission("customitem.give")) {
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /customitem give <entity_wand|warden_sword|fly_voucher|recipe_book> [player]"); // REMOVED custom_table
                    return true;
                }

                String itemType = args[1].toLowerCase();
                Player target;

                if (args.length >= 3) {
                    if (!sender.hasPermission("customitem.give.others")) {
                        return true;
                    }
                    target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sender.sendMessage("§cPlayer not found!");
                        return true;
                    }
                } else {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("§cYou must specify a player!");
                        return true;
                    }
                    target = (Player) sender;
                }

                switch (itemType) {
                    case "entity_wand":
                    case "wand":
                        giveEntityWand(target);
                        if (!target.equals(sender)) {
                            sender.sendMessage("§aGiven Entity Wand to " + target.getName());
                        }
                        break;

                    case "warden_sword":
                    case "warden":
                        giveWardenSword(target);
                        if (!target.equals(sender)) {
                            sender.sendMessage("§aGiven Warden Sword to " + target.getName());
                        }
                        break;

                    case "fly_voucher":
                    case "voucher":
                    case "fly":
                        giveFlyVoucher(target);
                        if (!target.equals(sender)) {
                            sender.sendMessage("§aGiven Fly Voucher to " + target.getName());
                        }
                        break;

                    case "recipe_book":
                    case "book":
                        giveRecipeBook(target);
                        if (!target.equals(sender)) {
                            sender.sendMessage("§aGiven Recipe Book to " + target.getName());
                        }
                        break;

                    // REMOVED: custom_table case
                    default:
                        sender.sendMessage("§cUnknown item: " + itemType);
                        break;
                }
                return true;
            }

            // ... rest of the command code remains unchanged ...
        } catch (Exception e) {
            // Silent fail
        }

        return true;
    }

    private void giveEntityWand(Player player) {
        try {
            player.getInventory().addItem(WandItem.createWand());
            player.sendMessage("§aYou received an §bEntity Wand§a!");
        } catch (Exception e) {
            // Silent fail
        }
    }

    private void giveWardenSword(Player player) {
        try {
            player.getInventory().addItem(WardenSword.createWardenSword());
            player.sendMessage("§aYou received a §5Warden Sword§a!");
        } catch (Exception e) {
            // Silent fail
        }
    }

    private void giveFlyVoucher(Player player) {
        try {
            player.getInventory().addItem(FlyVoucher.createFlyVoucher());
            player.sendMessage("§aYou received a §bFly Voucher§a!");
            player.sendMessage("§7Right-click to gain §eflight §7for 30 minutes.");
        } catch (Exception e) {
            // Silent fail
        }
    }

    private void giveRecipeBook(Player player) {
        try {
            player.getInventory().addItem(RecipeBook.createRecipeBook());
            player.sendMessage("§aYou received a §6Recipe Book§a!");
        } catch (Exception e) {
            // Silent fail
        }
    }

    // REMOVED: giveCustomCraftingTable method

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command,
                                      @Nonnull String alias, @Nonnull String[] args) {
        List<String> completions = new ArrayList<>();

        try {
            if (args.length == 1) {
                List<String> commands = new ArrayList<>(Arrays.asList("give", "release", "help", "flight", "flytime"));

                if (sender.hasPermission("customitem.admin")) {
                    commands.addAll(Arrays.asList("unlockrecipes", "stats", "reload", "forcebook", "cleardata"));
                }

                for (String cmd : commands) {
                    if (cmd.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(cmd);
                    }
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
                // REMOVED "custom_table" from completions
                List<String> items = Arrays.asList("entity_wand", "warden_sword", "fly_voucher", "recipe_book");
                for (String item : items) {
                    if (item.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(item);
                    }
                }
            } else if ((args.length == 2 &&
                    (args[0].equalsIgnoreCase("unlockrecipes") ||
                            args[0].equalsIgnoreCase("forcebook") ||
                            args[0].equalsIgnoreCase("cleardata"))) ||
                    (args.length == 3 && args[0].equalsIgnoreCase("give"))) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[args.length-1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        } catch (Exception e) {
            // Return empty list on error
        }

        return completions;
    }
}