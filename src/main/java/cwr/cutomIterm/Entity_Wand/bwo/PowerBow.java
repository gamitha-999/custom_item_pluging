package cwr.cutomIterm.Entity_Wand.bwo;

import cwr.cutomIterm.Entity_Wand.EntityWandPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class PowerBow {

    private static final NamespacedKey POWER_BOW_KEY = new NamespacedKey(EntityWandPlugin.getInstance(), "power_bow");
    private static final NamespacedKey TRACKING_ARROW_KEY = new NamespacedKey(EntityWandPlugin.getInstance(), "tracking_arrow");

    // Track active tracking arrows
    private static final Map<UUID, Entity> trackingArrows = new HashMap<>();

    public static ItemStack createPowerBow() {
        try {
            ItemStack bow = new ItemStack(Material.BOW, 1);
            ItemMeta meta = bow.getItemMeta();

            if (meta == null) {
                return bow;
            }

            // Custom name
            meta.setDisplayName("§6§lPower of Gamiya");

            // Add enchantment glint
            meta.setEnchantmentGlintOverride(true);

            // Set max stack size to 1
            try {
                meta.setMaxStackSize(1);
            } catch (NoSuchMethodError e) {
                // Fallback for older versions
            }

            // Mark as power bow
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(POWER_BOW_KEY, PersistentDataType.BYTE, (byte) 1);

            // Add unique ID to prevent stacking
            pdc.set(new NamespacedKey(EntityWandPlugin.getInstance(), "power_bow_unique"),
                    PersistentDataType.STRING,
                    UUID.randomUUID().toString());

            // Set lore
            meta.setLore(Arrays.asList(
                    "§6§lDivine Tracking Bow",
                    "",
                    "§7Special Ability: §eAuto-Tracking Arrows",
                    "§8Arrows automatically track nearest entity",
                    "§8and summon lightning on hit",
                    "",
                    "§a► Shoot to activate tracking",
                    "§a► Lightning strikes on hit"
            ));

            bow.setItemMeta(meta);
            return bow;
        } catch (Exception e) {
            e.printStackTrace();
            return new ItemStack(Material.BOW, 1);
        }
    }

    public static boolean isPowerBow(ItemStack item) {
        if (item == null || item.getType() != Material.BOW || item.getAmount() > 1) {
            return false;
        }

        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return false;

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            return pdc.has(POWER_BOW_KEY, PersistentDataType.BYTE);
        } catch (Exception e) {
            return false;
        }
    }

    public static void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        ItemStack bow = event.getBow();

        if (!isPowerBow(bow)) {
            return;
        }

        // Mark the arrow as tracking arrow
        if (event.getProjectile() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getProjectile();

            // Add tracking metadata to arrow
            arrow.getPersistentDataContainer().set(TRACKING_ARROW_KEY, PersistentDataType.BYTE, (byte) 1);

            // Store arrow for tracking
            trackingArrows.put(arrow.getUniqueId(), arrow);

            // Start tracking task
            startTrackingTask(arrow, player);

            // Play special sound
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.5f);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RETURN, 0.8f, 1.2f);

            // Particle effect on arrow - 1.21.1 particles
            arrow.getWorld().spawnParticle(Particle.CRIT, arrow.getLocation(), 10, 0.2, 0.2, 0.2, 0.1);
            arrow.getWorld().spawnParticle(Particle.PORTAL, arrow.getLocation(), 5, 0.1, 0.1, 0.1, 0.05);

            // Send message to player
            player.sendMessage("§6§lPower of Gamiya Activated!");
            player.sendMessage("§7Arrow will track nearest entity...");
        }
    }

    private static void startTrackingTask(Arrow arrow, Player shooter) {
        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 200; // 10 seconds max tracking

            @Override
            public void run() {
                // Check if arrow is still valid
                if (arrow == null || arrow.isDead() || arrow.isOnGround() || !arrow.isValid()) {
                    trackingArrows.remove(arrow.getUniqueId());
                    this.cancel();
                    return;
                }

                // Stop tracking after max time
                if (ticks >= MAX_TICKS) {
                    trackingArrows.remove(arrow.getUniqueId());
                    arrow.remove();
                    this.cancel();
                    return;
                }

                // Find nearest entity to track
                Entity nearestEntity = findNearestEntity(arrow, shooter);

                if (nearestEntity != null) {
                    // Calculate direction to entity
                    Location arrowLoc = arrow.getLocation();
                    Location entityLoc = nearestEntity.getLocation();

                    // Adjust entity location for better targeting
                    double eyeHeight = 1.0; // Default eye height
                    if (nearestEntity instanceof LivingEntity) {
                        eyeHeight = ((LivingEntity) nearestEntity).getEyeHeight();
                    }
                    entityLoc.setY(entityLoc.getY() + eyeHeight / 2);

                    Vector direction = entityLoc.toVector().subtract(arrowLoc.toVector()).normalize();

                    // Apply tracking force (gradual tracking, not instant)
                    Vector currentVelocity = arrow.getVelocity();
                    Vector newVelocity = currentVelocity.clone().add(direction.multiply(0.3)).normalize().multiply(currentVelocity.length());

                    arrow.setVelocity(newVelocity);

                    // Visual tracking effect - 1.21.1 particles
                    arrow.getWorld().spawnParticle(Particle.ENCHANTED_HIT, arrow.getLocation(), 3, 0.1, 0.1, 0.1, 0);
                    arrow.getWorld().spawnParticle(Particle.WITCH, arrow.getLocation(), 2, 0.05, 0.05, 0.05, 0);

                    // Add some dust particles for effect
                    arrow.getWorld().spawnParticle(Particle.DUST, arrow.getLocation(), 1, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.RED, 1.0f));
                }

                ticks++;
            }
        }.runTaskTimer(EntityWandPlugin.getInstance(), 1L, 1L); // Run every tick
    }

    private static Entity findNearestEntity(Arrow arrow, Player shooter) {
        Location arrowLoc = arrow.getLocation();
        Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        // Search radius (increases with arrow flight time)
        double searchRadius = 15.0;

        for (Entity entity : arrow.getWorld().getNearbyEntities(arrowLoc, searchRadius, searchRadius, searchRadius)) {
            // Skip invalid targets
            if (entity == shooter || entity == arrow ||
                    entity instanceof Item || entity instanceof Arrow ||
                    entity instanceof Projectile || entity instanceof ExperienceOrb) {
                continue;
            }

            // Only target living entities
            if (!(entity instanceof LivingEntity)) {
                continue;
            }

            LivingEntity living = (LivingEntity) entity;

            // Don't target dead entities
            if (living.isDead()) {
                continue;
            }

            // Calculate distance
            double distance = arrowLoc.distance(entity.getLocation());

            // Check line of sight (optional, makes it smarter)
            if (distance < nearestDistance && arrowLoc.getWorld().rayTraceBlocks(
                    arrowLoc, entity.getLocation().toVector().subtract(arrowLoc.toVector()).normalize(),
                    distance) == null) {

                nearest = entity;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    public static void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) {
            return;
        }

        Arrow arrow = (Arrow) event.getEntity();
        UUID arrowId = arrow.getUniqueId();

        // Check if this is a tracking arrow
        if (!trackingArrows.containsKey(arrowId)) {
            return;
        }

        // Get the hit entity or block
        Entity hitEntity = event.getHitEntity();
        org.bukkit.block.Block hitBlock = event.getHitBlock();

        Location hitLocation;
        if (hitEntity != null) {
            hitLocation = hitEntity.getLocation();
        } else if (hitBlock != null) {
            hitLocation = hitBlock.getLocation().add(0.5, 1, 0.5);
        } else {
            hitLocation = arrow.getLocation();
        }

        // Summon lightning (effect only, no damage from lightning itself)
        try {
            arrow.getWorld().strikeLightningEffect(hitLocation);
        } catch (Exception e) {
            // Fallback to regular lightning if effect doesn't work
            arrow.getWorld().strikeLightning(hitLocation);
        }

        // Damage effect for living entities
        if (hitEntity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) hitEntity;
            living.damage(8.0); // Extra lightning damage

            // Get shooter for damage attribution
            ProjectileSource shooter = arrow.getShooter();
            if (shooter instanceof Player) {
                living.damage(2.0, (Player) shooter); // Additional player damage
            }

            // Apply special effects
            living.setFireTicks(60); // Set on fire for 3 seconds

            // Play sound effect
            living.getWorld().playSound(living.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 0.8f);
        }

        // Visual effects
        arrow.getWorld().playSound(hitLocation, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.0f);

        // Use explosion sound
        arrow.getWorld().playSound(hitLocation, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.2f);

        // Particle explosion - 1.21.1 particles
        // Large explosion particle
        arrow.getWorld().spawnParticle(Particle.EXPLOSION, hitLocation, 1, 0.5, 0.5, 0.5, 0);

        // Flame particles
        arrow.getWorld().spawnParticle(Particle.FLAME, hitLocation, 20, 1.0, 1.0, 1.0, 0.1);

        // Smoke particles
        arrow.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, hitLocation, 10, 0.5, 0.5, 0.5, 0.05);

        // Add some extra effects
        arrow.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, hitLocation, 15, 0.3, 0.3, 0.3, 0.02);

        // Remove arrow from tracking
        trackingArrows.remove(arrowId);

        // Remove the arrow entity
        arrow.remove();

        // Find shooter for message
        if (arrow.getShooter() instanceof Player) {
            Player shooter = (Player) arrow.getShooter();
            if (hitEntity != null) {
                String entityName;
                if (hitEntity instanceof Player) {
                    entityName = ((Player) hitEntity).getName();
                } else {
                    entityName = hitEntity.getType().toString().toLowerCase().replace("_", " ");
                    entityName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
                }
                shooter.sendMessage("§6⚡ §lLightning Strike! §eHit " + entityName + "!");
            } else {
                shooter.sendMessage("§6⚡ §lLightning Strike! §eTarget destroyed!");
            }
        }
    }

    public static void clearTrackingArrows() {
        // Remove all tracking arrows from the world
        for (Entity arrow : trackingArrows.values()) {
            if (arrow != null && arrow.isValid()) {
                arrow.remove();
            }
        }
        trackingArrows.clear();
    }
}