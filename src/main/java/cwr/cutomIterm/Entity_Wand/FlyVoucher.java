package cwr.cutomIterm.Entity_Wand;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FlyVoucher {

    private static final NamespacedKey FLY_VOUCHER_KEY = new NamespacedKey(EntityWandPlugin.getInstance(), "fly_voucher");
    private static final NamespacedKey FLY_TIMER_KEY = new NamespacedKey(EntityWandPlugin.getInstance(), "fly_timer");

    // Track active fly vouchers
    private static final Map<UUID, Integer> activeFlyTimers = new HashMap<>();

    // Flight duration in milliseconds (30 minutes)
    private static final long FLIGHT_DURATION = 30 * 60 * 1000L;

    public static ItemStack createFlyVoucher() {
        try {
            ItemStack voucher = new ItemStack(Material.FEATHER, 1);
            ItemMeta meta = voucher.getItemMeta();

            if (meta == null) {
                return voucher;
            }

            // Custom name
            meta.setDisplayName("§b§lFly Voucher");

            // Add enchantment glint
            meta.setEnchantmentGlintOverride(true);

            // Set max stack size to 1 (single use)
            try {
                meta.setMaxStackSize(1);
            } catch (NoSuchMethodError e) {
                // Fallback for older versions
            }

            // Mark as fly voucher
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(FLY_VOUCHER_KEY, PersistentDataType.BYTE, (byte) 1);

            // Add unique ID to prevent stacking
            pdc.set(new NamespacedKey(EntityWandPlugin.getInstance(), "voucher_unique_id"),
                    PersistentDataType.STRING,
                    UUID.randomUUID().toString());

            // Set lore
            meta.setLore(Arrays.asList(
                    "§b§lFlight Permit",
                    "",
                    "§7Right-click to activate",
                    "§7Grants §eflight ability §7for",
                    "§730 minutes",
                    "",
                    "§e§oOne-time use item",
                    "§8§oConsumed on activation",
                    "",
                    "§a► Right-click to fly!"
            ));

            voucher.setItemMeta(meta);
            return voucher;
        } catch (Exception e) {
            e.printStackTrace();
            return new ItemStack(Material.FEATHER, 1);
        }
    }

    public static boolean isFlyVoucher(ItemStack item) {
        if (item == null || item.getType() != Material.FEATHER || item.getAmount() > 1) {
            return false;
        }

        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return false;

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            return pdc.has(FLY_VOUCHER_KEY, PersistentDataType.BYTE);
        } catch (Exception e) {
            return false;
        }
    }

    public static void useFlyVoucher(Player player, ItemStack voucher) {
        // Check if player already has flight active
        if (hasActiveFlight(player)) {
            player.sendMessage("§cYou already have active flight!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        // Consume the voucher
        voucher.setAmount(voucher.getAmount() - 1);

        // Play activation sound
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.8f, 1.2f);

        // Grant flight ability
        grantFlight(player);

        // Send messages
        player.sendMessage("§b§l✈ Fly Voucher Activated!");
        player.sendMessage("§7You can now fly for §e30 minutes§7!");
        player.sendMessage("§7Use §e/double jump §7or press §espace twice §7to fly.");

        // Add particle effect - using compatible particle names
        try {
            // Try newer particle names first
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0),
                    20, 0.5, 0.5, 0.5, 0.1);
        } catch (NoSuchFieldError | IllegalArgumentException e) {
            // Fallback to older particle names
            try {
                player.getWorld().spawnParticle(Particle.valueOf("TOTEM"), player.getLocation().add(0, 1, 0),
                        20, 0.5, 0.5, 0.5, 0.1);
            } catch (IllegalArgumentException e2) {
                // Use alternative particle if TOTEM doesn't exist
                player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0),
                        20, 0.5, 0.5, 0.5, 0.1);
            }
        }

        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0),
                15, 0.3, 0.3, 0.3, 0.05);

        // Start flight timer
        startFlightTimer(player);
    }

    private static void grantFlight(Player player) {
        player.setAllowFlight(true);
        player.setFlying(false);

        // Store the activation time
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(FLY_TIMER_KEY, PersistentDataType.LONG, System.currentTimeMillis());
    }

    private static void startFlightTimer(Player player) {
        UUID playerId = player.getUniqueId();

        // Cancel any existing timer for this player
        if (activeFlyTimers.containsKey(playerId)) {
            Bukkit.getScheduler().cancelTask(activeFlyTimers.get(playerId));
        }

        // Create new timer task
        int taskId = new BukkitRunnable() {
            private int minutesLeft = 30;
            private int lastNotification = 30;

            @Override
            public void run() {
                // Check if player is offline
                if (!player.isOnline()) {
                    this.cancel();
                    activeFlyTimers.remove(playerId);
                    return;
                }

                // Calculate time left
                PersistentDataContainer pdc = player.getPersistentDataContainer();
                Long activationTime = pdc.get(FLY_TIMER_KEY, PersistentDataType.LONG);

                if (activationTime == null) {
                    // Flight expired or was removed
                    revokeFlight(player);
                    this.cancel();
                    activeFlyTimers.remove(playerId);
                    return;
                }

                long elapsed = System.currentTimeMillis() - activationTime;
                long timeLeft = FLIGHT_DURATION - elapsed;

                if (timeLeft <= 0) {
                    // Flight time is up
                    revokeFlight(player);
                    this.cancel();
                    activeFlyTimers.remove(playerId);
                    return;
                }

                // Calculate minutes left
                int currentMinutesLeft = (int) (timeLeft / (60 * 1000));

                // Send notifications at specific intervals
                if (currentMinutesLeft < lastNotification &&
                        (currentMinutesLeft == 10 || currentMinutesLeft == 5 ||
                                currentMinutesLeft == 3 || currentMinutesLeft == 2 ||
                                currentMinutesLeft == 1 || currentMinutesLeft <= 0)) {

                    if (currentMinutesLeft > 0) {
                        player.sendMessage("§eFlight time remaining: §6" + currentMinutesLeft + " minute" +
                                (currentMinutesLeft == 1 ? "" : "s"));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f);
                    }
                    lastNotification = currentMinutesLeft;
                }

                // Update minutes left
                minutesLeft = currentMinutesLeft;

                // Visual effect when flight is about to expire (last minute)
                if (timeLeft <= 60000) { // Last 60 seconds
                    if (player.isFlying()) {
                        try {
                            // Try newer particle names
                            player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE,
                                    player.getLocation().add(0, 0.5, 0), 3, 0.2, 0.2, 0.2, 0.01);
                        } catch (NoSuchFieldError | IllegalArgumentException e) {
                            // Fallback to older particle names
                            try {
                                player.getWorld().spawnParticle(Particle.valueOf("SMOKE_NORMAL"),
                                        player.getLocation().add(0, 0.5, 0), 3, 0.2, 0.2, 0.2, 0.01);
                            } catch (IllegalArgumentException e2) {
                                // Use alternative particle
                                player.getWorld().spawnParticle(Particle.CLOUD,
                                        player.getLocation().add(0, 0.5, 0), 3, 0.2, 0.2, 0.2, 0.01);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(EntityWandPlugin.getInstance(), 20L, 20L).getTaskId(); // Check every second

        // Store the task ID
        activeFlyTimers.put(playerId, taskId);
    }

    private static void revokeFlight(Player player) {
        // Only revoke if we granted it (not if they have creative mode or other flight)
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (pdc.has(FLY_TIMER_KEY, PersistentDataType.LONG)) {
            pdc.remove(FLY_TIMER_KEY);

            // Only disable flight if they're not in creative/gamemode 1
            if (player.getGameMode() != GameMode.CREATIVE &&
                    player.getGameMode() != GameMode.SPECTATOR) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }

            // Send expiration message
            player.sendMessage("§c§lFlight Expired!");
            player.sendMessage("§7Your §eFly Voucher §7has expired.");
            player.sendMessage("§7Use another voucher to fly again.");

            // Play expiration sound
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.7f, 1.0f);
            player.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 0.8f, 1.0f);

            // Particle effect
            player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 1, 0),
                    15, 0.5, 0.5, 0.5, 0.05);
        }
    }

    public static boolean hasActiveFlight(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (!pdc.has(FLY_TIMER_KEY, PersistentDataType.LONG)) {
            return false;
        }

        Long activationTime = pdc.get(FLY_TIMER_KEY, PersistentDataType.LONG);
        if (activationTime == null) {
            return false;
        }

        long elapsed = System.currentTimeMillis() - activationTime;
        return elapsed < FLIGHT_DURATION;
    }

    public static long getRemainingFlightTime(Player player) {
        if (!hasActiveFlight(player)) {
            return 0;
        }

        PersistentDataContainer pdc = player.getPersistentDataContainer();
        Long activationTime = pdc.get(FLY_TIMER_KEY, PersistentDataType.LONG);

        if (activationTime == null) {
            return 0;
        }

        long elapsed = System.currentTimeMillis() - activationTime;
        long remaining = FLIGHT_DURATION - elapsed;
        return Math.max(0, remaining);
    }

    public static void checkAndRevokeExpiredFlight(Player player) {
        if (hasActiveFlight(player)) {
            Long activationTime = player.getPersistentDataContainer()
                    .get(FLY_TIMER_KEY, PersistentDataType.LONG);

            if (activationTime != null) {
                long elapsed = System.currentTimeMillis() - activationTime;
                if (elapsed >= FLIGHT_DURATION) {
                    revokeFlight(player);
                }
            }
        }
    }

    public static void clearFlightTimer(UUID playerId) {
        if (activeFlyTimers.containsKey(playerId)) {
            Bukkit.getScheduler().cancelTask(activeFlyTimers.get(playerId));
            activeFlyTimers.remove(playerId);
        }
    }

    public static void clearAllFlightTimers() {
        for (Integer taskId : activeFlyTimers.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        activeFlyTimers.clear();
    }
    public static int getActiveFlightCount() {
        return activeFlyTimers.size();
    }
}