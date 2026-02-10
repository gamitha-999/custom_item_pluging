package cwr.cutomIterm.Entity_Wand;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class EntityMovementManager {

    private static final Map<UUID, Entity> heldEntities = new HashMap<>();
    private static final Map<UUID, ItemStack> heldWands = new HashMap<>();
    private static final Map<UUID, Integer> tickCounters = new HashMap<>();
    private static final Map<UUID, Long> lastRightClick = new HashMap<>();

    private static BukkitRunnable movementTask;

    public static void start(EntityWandPlugin plugin) {
        if (movementTask != null) {
            movementTask.cancel();
        }

        movementTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // Clean up invalid entries
                    cleanUp();

                    // Move all held entities
                    for (Map.Entry<UUID, Entity> entry : new HashMap<>(heldEntities).entrySet()) {
                        moveEntity(entry.getKey(), entry.getValue());
                    }
                } catch (Exception e) {
                    // Silent fail - don't spam console
                }
            }
        };

        movementTask.runTaskTimer(plugin, 0L, 1L);
    }

    public static void stop() {
        if (movementTask != null) {
            movementTask.cancel();
            movementTask = null;
        }
        heldEntities.clear();
        heldWands.clear();
        tickCounters.clear();
        lastRightClick.clear();
    }

    private static void cleanUp() {
        Iterator<UUID> iterator = heldEntities.keySet().iterator();
        long currentTime = System.currentTimeMillis();

        while (iterator.hasNext()) {
            UUID playerId = iterator.next();
            Player player = Bukkit.getPlayer(playerId);

            // Check if player is online
            if (player == null || !player.isOnline()) {
                iterator.remove();
                heldWands.remove(playerId);
                tickCounters.remove(playerId);
                lastRightClick.remove(playerId);
                continue;
            }

            // Check if player is still holding right-click (within last 250ms)
            Long lastClick = lastRightClick.get(playerId);
            if (lastClick == null || currentTime - lastClick > 250) {
                iterator.remove();
                heldWands.remove(playerId);
                tickCounters.remove(playerId);
                lastRightClick.remove(playerId);
                continue;
            }

            // Check if player still has wand in hand
            ItemStack currentItem = player.getInventory().getItemInMainHand();
            if (!WandItem.isWand(currentItem)) {
                iterator.remove();
                heldWands.remove(playerId);
                tickCounters.remove(playerId);
                lastRightClick.remove(playerId);
            }
        }
    }

    public static void grabEntity(Player player, Entity entity) {
        UUID playerId = player.getUniqueId();

        // Release any existing entity
        releaseEntity(playerId);

        // Start holding new entity
        ItemStack wand = player.getInventory().getItemInMainHand();
        heldEntities.put(playerId, entity);
        heldWands.put(playerId, wand);
        tickCounters.put(playerId, 0);
        updateRightClick(playerId);
    }

    public static void updateRightClick(UUID playerId) {
        lastRightClick.put(playerId, System.currentTimeMillis());
    }

    private static void moveEntity(UUID playerId, Entity entity) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline() || entity == null || entity.isDead()) {
            releaseEntity(playerId);
            return;
        }

        // Get the wand
        ItemStack wand = heldWands.get(playerId);
        if (wand == null || !WandItem.isWand(wand)) {
            releaseEntity(playerId);
            return;
        }

        // Check durability
        int durability = WandItem.getDurability(wand);
        if (durability <= 0) {
            releaseEntity(playerId);
            return;
        }

        // Calculate position 3 blocks in front of player
        Location eyeLocation = player.getEyeLocation().clone();
        Vector direction = eyeLocation.getDirection().normalize();

        // Move 3 blocks in front
        Location targetLocation = eyeLocation.add(direction.multiply(3));

        // Adjust height for better visibility
        targetLocation.setY(targetLocation.getY() + 0.5);

        // Teleport the entity
        entity.teleport(targetLocation);

        // Update durability every 5 ticks
        int tickCount = tickCounters.getOrDefault(playerId, 0) + 1;
        tickCounters.put(playerId, tickCount);

        if (tickCount >= 5) {
            if (!WandItem.decreaseDurability(player, wand, 1)) {
                // Wand broke
                releaseEntity(playerId);
            } else {
                // Update the wand in player's hand
                player.getInventory().setItemInMainHand(wand);
                heldWands.put(playerId, wand);
            }
            tickCounters.put(playerId, 0);
        }
    }

    public static void releaseEntity(UUID playerId) {
        heldEntities.remove(playerId);
        heldWands.remove(playerId);
        tickCounters.remove(playerId);
        lastRightClick.remove(playerId);
    }

    public static void releaseAll() {
        heldEntities.clear();
        heldWands.clear();
        tickCounters.clear();
        lastRightClick.clear();
    }
}