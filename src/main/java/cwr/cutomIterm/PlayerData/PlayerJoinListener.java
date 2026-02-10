package cwr.cutomIterm.PlayerData;

import cwr.cutomIterm.Entity_Wand.EntityMovementManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private final PlayerDataManager dataManager;

    public PlayerJoinListener() {
        this.dataManager = PlayerDataManager.getInstance();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            // Check if player has played before and give recipe book if not
            dataManager.giveRecipeBookIfFirstJoin(event.getPlayer());

            // Note: Recipe checking is now handled by RecipeUnlockListener
            // to avoid duplicate code and ensure consistency
        } catch (Exception e) {
            // Silent fail - don't break player join
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            // Player leaving releases any held entity
            EntityMovementManager.releaseEntity(event.getPlayer().getUniqueId());
        } catch (Exception e) {
            // Silent fail
        }
    }
}