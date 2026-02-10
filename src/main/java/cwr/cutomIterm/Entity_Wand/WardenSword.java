package cwr.cutomIterm.Entity_Wand;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class WardenSword {

    private static final NamespacedKey WARDEN_SWORD_KEY = new NamespacedKey(EntityWandPlugin.getInstance(), "warden_sword");
    private static final NamespacedKey COOLDOWN_KEY = new NamespacedKey(EntityWandPlugin.getInstance(), "warden_cooldown");
    private static final int MAX_DURABILITY = 2500; // Netherite Sword max durability
    private static final long COOLDOWN_MS = 20000; // 20 seconds in milliseconds
    private static final int ABILITY_DURABILITY_COST = 10; // Direct durability cost
    private static final double ATTACK_DAMAGE = 11.0;
    private static final double ATTACK_SPEED = 2.5;

    // Track cooldowns
    private static final Map<UUID, Long> playerCooldowns = new HashMap<>();

    public static ItemStack createWardenSword() {
        try {
            ItemStack sword = new ItemStack(Material.NETHERITE_SWORD, 1);
            ItemMeta meta = sword.getItemMeta();

            if (meta == null) {
                return sword;
            }

            // Custom name
            meta.setDisplayName("§5Warden Sword");

            // Add enchantment glint
            meta.setEnchantmentGlintOverride(true);

            // Set max stack size to 1
            try {
                meta.setMaxStackSize(1);
            } catch (NoSuchMethodError e) {
                // Fallback for older versions
            }

            // Mark as warden sword
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(WARDEN_SWORD_KEY, PersistentDataType.BYTE, (byte) 1);

            // Add unique ID to prevent stacking
            pdc.set(new NamespacedKey(EntityWandPlugin.getInstance(), "unique_id"),
                    PersistentDataType.STRING,
                    UUID.randomUUID().toString());

            // Set initial durability to max
            if (meta instanceof Damageable) {
                ((Damageable) meta).setDamage(0); // Start with full durability
            }

            // Set lore WITHOUT durability information
            meta.setLore(Arrays.asList(
                    "§5§lWarden's Wrath",
                    "",
                    "§7Attack Damage: §c" + ATTACK_DAMAGE,
                    "§7Attack Speed: §e" + ATTACK_SPEED,
                    "",
                    "§dAbility: §5Warden Sonic Boom",
                    "§7Right-click to unleash sonic energy",
                    "§7that deals damage to enemies",
                    "",
                    "§8Cooldown: §e20 seconds",
                    "§8Ability Cost: §c10 durability",
                    "",
                    "§8§oA sword infused with ancient Warden power"
            ));

            sword.setItemMeta(meta);
            return sword;
        } catch (Exception e) {
            e.printStackTrace();
            return new ItemStack(Material.NETHERITE_SWORD, 1);
        }
    }

    public static boolean isWardenSword(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SWORD || item.getAmount() > 1) {
            return false;
        }

        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return false;

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            return pdc.has(WARDEN_SWORD_KEY, PersistentDataType.BYTE);
        } catch (Exception e) {
            return false;
        }
    }

    private static int getCurrentDurability(ItemStack sword) {
        if (!isWardenSword(sword)) return -1;

        if (sword.getItemMeta() instanceof Damageable) {
            Damageable damageable = (Damageable) sword.getItemMeta();
            return MAX_DURABILITY - damageable.getDamage();
        }
        return MAX_DURABILITY;
    }

    private static int getCurrentDamage(ItemStack sword) {
        if (!isWardenSword(sword)) return 0;

        if (sword.getItemMeta() instanceof Damageable) {
            Damageable damageable = (Damageable) sword.getItemMeta();
            return damageable.getDamage();
        }
        return 0;
    }

    // REMOVED: getRemainingDurabilityDisplay method since we don't show durability in lore

    private static boolean applyDurabilityDamage(ItemStack sword, int damage) {
        if (!isWardenSword(sword)) return false;

        ItemMeta meta = sword.getItemMeta();
        if (!(meta instanceof Damageable)) return false;

        Damageable damageable = (Damageable) meta;
        int currentDamage = damageable.getDamage();
        int newDamage = currentDamage + damage;

        // Cap at max durability
        if (newDamage >= MAX_DURABILITY) {
            newDamage = MAX_DURABILITY;
            damageable.setDamage(newDamage);

            // Don't update lore - we're not showing durability
            sword.setItemMeta(meta);
            return false; // Sword is broken
        }

        damageable.setDamage(newDamage);

        // Don't update lore with durability information
        sword.setItemMeta(meta);
        return true; // Durability applied successfully
    }

    public static boolean canUseAbility(Player player) {
        UUID playerId = player.getUniqueId();
        Long lastUsed = playerCooldowns.get(playerId);

        if (lastUsed == null) {
            return true;
        }

        long timeSinceLastUse = System.currentTimeMillis() - lastUsed;
        return timeSinceLastUse >= COOLDOWN_MS;
    }

    public static long getRemainingCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        Long lastUsed = playerCooldowns.get(playerId);

        if (lastUsed == null) {
            return 0;
        }

        long timeSinceLastUse = System.currentTimeMillis() - lastUsed;
        return Math.max(0, COOLDOWN_MS - timeSinceLastUse);
    }

    public static void useSonicBoomAbility(Player player, ItemStack sword) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (!canUseAbility(player)) {
            long remaining = getRemainingCooldown(player);
            player.sendMessage("§cAbility on cooldown! §e(" + (remaining / 1000) + "s remaining)");
            return;
        }

        // Check if sword is broken
        int remainingDurability = getCurrentDurability(sword);
        if (remainingDurability <= 0) {
            player.sendMessage("§cYour Warden Sword is broken!");
            return;
        }

        // Check if there's enough durability for the ability
        if (remainingDurability < ABILITY_DURABILITY_COST) {
            player.sendMessage("§cNot enough durability to use ability!");
            return;
        }

        // Apply durability damage
        if (!applyDurabilityDamage(sword, ABILITY_DURABILITY_COST)) {
            // Sword broke during ability use
            player.sendMessage("§cYour Warden Sword broke while using the ability!");
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 1.0f);
            player.getInventory().setItemInMainHand(sword); // Update broken sword
            return;
        }

        // Set cooldown
        playerCooldowns.put(playerId, System.currentTimeMillis());

        // Play sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_ANGRY, 0.5f, 0.8f);

        // Send message (don't mention durability in the message)
        player.sendMessage("§5§lWarden's Wrath! §dSonic Boom unleashed!");

        // Create sonic boom effect
        createSonicBoomEffect(player);

        // Update item in hand
        player.getInventory().setItemInMainHand(sword);
    }

    private static void createSonicBoomEffect(Player player) {
        Location start = player.getEyeLocation().clone();
        Vector direction = start.getDirection().normalize();

        new BukkitRunnable() {
            double distance = 0;

            @Override
            public void run() {
                if (distance > 30) { // Max range 30 blocks
                    this.cancel();
                    return;
                }

                // Calculate position
                Location particleLoc = start.clone().add(direction.clone().multiply(distance));

                // Spawn particles
                player.getWorld().spawnParticle(Particle.SONIC_BOOM, particleLoc, 1, 0, 0, 0, 0);
                player.getWorld().spawnParticle(Particle.SCULK_SOUL, particleLoc, 3, 0.2, 0.2, 0.2, 0);

                // Check for entities to damage
                for (Entity entity : player.getWorld().getNearbyEntities(particleLoc, 1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity living = (LivingEntity) entity;

                        // Apply damage
                        living.damage(10.0, player);

                        // Apply knockback
                        Vector knockback = direction.clone().multiply(2.0);
                        knockback.setY(0.3);
                        living.setVelocity(knockback);

                        // Play hit effect
                        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_WARDEN_HURT, 0.7f, 1.2f);
                    }
                }

                distance += 0.5; // Move forward 0.5 blocks each tick
            }
        }.runTaskTimer(EntityWandPlugin.getInstance(), 0L, 1L); // Run every tick
    }

    public static void clearCooldown(Player player) {
        playerCooldowns.remove(player.getUniqueId());
    }

    public static void clearAllCooldowns() {
        playerCooldowns.clear();
    }

    // Helper method to get remaining durability percentage (internal use only)
    public static double getDurabilityPercentage(ItemStack sword) {
        int remaining = getCurrentDurability(sword);
        if (remaining <= 0) return 0.0;
        return (double) remaining / MAX_DURABILITY * 100.0;
    }
}