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

    private static final Map<UUID, Entity> trackingArrows = new HashMap<>();
    private static final Map<UUID, List<Location>> arrowTrails = new HashMap<>(); // Store trail locations
    private static final double TRACKING_RADIUS = 3.0; // 3-block radius for tracking

    // Trail settings
    private static final int TRAIL_LENGTH = 10; // How many previous positions to remember
    private static final double TRAIL_SPACING = 0.5; // Distance between trail particles

    public static ItemStack createPowerBow() {
        try {
            ItemStack bow = new ItemStack(Material.BOW, 1);
            ItemMeta meta = bow.getItemMeta();

            if (meta == null) return bow;

            meta.setDisplayName("§6§lPower of Gamiya");
            meta.setEnchantmentGlintOverride(true);

            try {
                meta.setMaxStackSize(1);
            } catch (NoSuchMethodError ignored) {}

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(POWER_BOW_KEY, PersistentDataType.BYTE, (byte) 1);
            pdc.set(new NamespacedKey(EntityWandPlugin.getInstance(), "power_bow_unique"),
                    PersistentDataType.STRING, UUID.randomUUID().toString());

            meta.setLore(Arrays.asList(
                    "§6§lDivine Tracking Bow",
                    "",
                    "§7Special Ability: §eAuto-Tracking Arrows",
                    "§8Arrows automatically track nearest entity",
                    "§8within §e3 blocks §8radius",
                    "§8Leaves a §cflame trail §8as it flies",
                    "§8and summons lightning on hit",
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
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack bow = event.getBow();

        if (!isPowerBow(bow)) return;

        if (event.getProjectile() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getProjectile();
            arrow.getPersistentDataContainer().set(TRACKING_ARROW_KEY, PersistentDataType.BYTE, (byte) 1);
            trackingArrows.put(arrow.getUniqueId(), arrow);

            // Initialize trail for this arrow
            arrowTrails.put(arrow.getUniqueId(), new ArrayList<>());

            startTrackingTask(arrow, player);

            // Play sounds
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.5f);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RETURN, 0.8f, 1.2f);
        }
    }

    private static void startTrackingTask(Arrow arrow, Player shooter) {
        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 200;

            @Override
            public void run() {
                if (arrow == null || arrow.isDead() || !arrow.isValid()) {
                    cleanupArrow(arrow);
                    this.cancel();
                    return;
                }

                // Stop tracking if arrow is stuck in a block
                if (arrow.isOnGround() || arrow.isInBlock()) {
                    cleanupArrow(arrow);
                    this.cancel();
                    return;
                }

                if (ticks >= MAX_TICKS) {
                    cleanupArrow(arrow);
                    arrow.remove();
                    this.cancel();
                    return;
                }

                // Update arrow trail
                updateArrowTrail(arrow);

                // Find nearest entity within 3 blocks
                Entity nearestEntity = findNearestEntityInRadius(arrow, shooter);

                if (nearestEntity != null) {
                    // Stronger tracking - redirect arrow more aggressively
                    Location arrowLoc = arrow.getLocation();
                    Location entityLoc = nearestEntity.getLocation();

                    // Aim for center of entity
                    entityLoc.setY(entityLoc.getY() + (nearestEntity.getHeight() / 2));

                    Vector direction = entityLoc.toVector().subtract(arrowLoc.toVector()).normalize();

                    // Strong tracking force (0.5 instead of 0.3 for more aggressive tracking)
                    Vector currentVelocity = arrow.getVelocity();
                    double speed = currentVelocity.length();

                    // If arrow is too slow, give it a minimum speed
                    if (speed < 0.5) {
                        speed = 1.0;
                    }

                    // Calculate new direction (70% towards target, 30% original direction)
                    Vector newDirection = direction.multiply(0.7).add(currentVelocity.normalize().multiply(0.3)).normalize();
                    Vector newVelocity = newDirection.multiply(speed);

                    arrow.setVelocity(newVelocity);

                    // Visual tracking effect
                    arrow.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, arrow.getLocation(), 2, 0.05, 0.05, 0.05, 0.01);

                    // Show particle line to target
                    if (ticks % 5 == 0) { // Every 5 ticks
                        showTrackingLine(arrowLoc, entityLoc, arrow.getWorld());
                    }
                }

                ticks++;
            }
        }.runTaskTimer(EntityWandPlugin.getInstance(), 1L, 1L);
    }

    private static void updateArrowTrail(Arrow arrow) {
        UUID arrowId = arrow.getUniqueId();
        List<Location> trail = arrowTrails.get(arrowId);

        if (trail == null) {
            trail = new ArrayList<>();
            arrowTrails.put(arrowId, trail);
        }

        // Add current position to trail
        Location currentPos = arrow.getLocation().clone();
        trail.add(currentPos);

        // Keep only the last TRAIL_LENGTH positions
        while (trail.size() > TRAIL_LENGTH) {
            trail.remove(0);
        }

        // Spawn flame particles along the trail
        spawnFlameTrail(trail, arrow.getWorld());
    }

    private static void spawnFlameTrail(List<Location> trail, World world) {
        if (trail.size() < 2) return;

        // Spawn flame particles at each trail point
        for (int i = 0; i < trail.size(); i++) {
            Location point = trail.get(i);

            // Spawn multiple flame particles at each point
            int particleCount = 2;
            if (i == trail.size() - 1) {
                // More particles at the newest point (arrow head)
                particleCount = 4;
            }

            for (int j = 0; j < particleCount; j++) {
                double offsetX = (Math.random() - 0.5) * 0.2;
                double offsetY = (Math.random() - 0.5) * 0.2;
                double offsetZ = (Math.random() - 0.5) * 0.2;

                Location particleLoc = point.clone().add(offsetX, offsetY, offsetZ);

                // Use FLAME particle
                world.spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0, 0);

                // Also add some smoke for variety
                if (Math.random() < 0.3) {
                    try {
                        // Try to use SMOKE particle (newer versions)
                        world.spawnParticle(Particle.valueOf("SMOKE"), particleLoc, 1, 0.05, 0.05, 0.05, 0);
                    } catch (IllegalArgumentException e) {
                        try {
                            // Try to use SMOKE_NORMAL particle (older versions)
                            world.spawnParticle(Particle.valueOf("SMOKE_NORMAL"), particleLoc, 1, 0.05, 0.05, 0.05, 0);
                        } catch (IllegalArgumentException e2) {
                            // If neither exists, just skip or use a different particle
                            world.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, particleLoc, 1, 0.05, 0.05, 0.05, 0);
                        }
                    }
                }
            }
        }

        // Connect the dots with a continuous flame trail
        for (int i = 0; i < trail.size() - 1; i++) {
            Location start = trail.get(i);
            Location end = trail.get(i + 1);

            // Create a smooth trail between points
            Vector direction = end.toVector().subtract(start.toVector());
            double distance = direction.length();
            direction.normalize();

            // Spawn particles along the line between points
            int particlesAlongLine = (int) (distance / TRAIL_SPACING);
            for (int j = 0; j < particlesAlongLine; j++) {
                double ratio = (double) j / particlesAlongLine;
                Location particleLoc = start.clone().add(direction.clone().multiply(distance * ratio));

                // Spawn flame particles
                world.spawnParticle(Particle.FLAME, particleLoc, 1, 0.02, 0.02, 0.02, 0);

                // Occasionally spawn larger fire particles
                if (Math.random() < 0.2) {
                    world.spawnParticle(Particle.LAVA, particleLoc, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    private static Entity findNearestEntityInRadius(Arrow arrow, Player shooter) {
        Location arrowLoc = arrow.getLocation();
        Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        // Search in 3-block radius (as requested)
        double searchRadius = TRACKING_RADIUS;

        for (Entity entity : arrow.getWorld().getNearbyEntities(arrowLoc, searchRadius, searchRadius, searchRadius)) {
            // Skip invalid targets
            if (entity == shooter || entity == arrow ||
                    entity instanceof Item || entity instanceof Arrow ||
                    entity instanceof Projectile || entity instanceof ExperienceOrb ||
                    entity instanceof EnderCrystal || entity instanceof ItemFrame) {
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

            // Check if entity is within 3 blocks
            if (distance <= TRACKING_RADIUS) {
                // Prioritize closest entity within radius
                if (distance < nearestDistance) {
                    // Optional: Check line of sight (can be removed for through-wall tracking)
                    if (arrowLoc.getWorld().rayTraceBlocks(
                            arrowLoc, entity.getLocation().toVector().subtract(arrowLoc.toVector()).normalize(),
                            distance) == null) {
                        nearest = entity;
                        nearestDistance = distance;
                    }
                }
            }
        }

        return nearest;
    }

    private static void showTrackingLine(Location from, Location to, World world) {
        // Create a visual line showing the tracking path
        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        direction.normalize();

        // Spawn particles along the line
        int particles = (int) (distance * 2);
        for (int i = 0; i < particles; i++) {
            double ratio = (double) i / particles;
            Location particleLoc = from.clone().add(direction.clone().multiply(distance * ratio));
            world.spawnParticle(Particle.ENCHANT, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    public static void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;

        UUID arrowId = arrow.getUniqueId();
        if (!trackingArrows.containsKey(arrowId)) return;

        Entity hitEntity = event.getHitEntity();
        org.bukkit.block.Block hitBlock = event.getHitBlock();
        Location hitLocation = hitEntity != null ? hitEntity.getLocation() :
                hitBlock != null ? hitBlock.getLocation().add(0.5, 1, 0.5) :
                        arrow.getLocation();

        // Create a final flame burst effect
        createFlameBurst(hitLocation, arrow.getWorld());

        // Lightning effect
        arrow.getWorld().strikeLightningEffect(hitLocation);

        // Damage
        if (hitEntity instanceof LivingEntity living) {
            living.damage(8.0);
            ProjectileSource shooter = arrow.getShooter();
            if (shooter instanceof Player playerShooter) {
                living.damage(2.0, playerShooter);
            }
            living.setFireTicks(60);
        }

        // Effects
        arrow.getWorld().playSound(hitLocation, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.0f);
        arrow.getWorld().playSound(hitLocation, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.2f);

        // Explosion particles
        arrow.getWorld().spawnParticle(Particle.EXPLOSION, hitLocation, 5, 0.5, 0.5, 0.5, 0);
        arrow.getWorld().spawnParticle(Particle.FLAME, hitLocation, 20, 1.0, 1.0, 1.0, 0.1);

        cleanupArrow(arrow);
        arrow.remove();
    }

    private static void createFlameBurst(Location location, World world) {
        // Create a burst of flame particles on impact
        int burstParticles = 30;
        for (int i = 0; i < burstParticles; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 1.5;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = Math.random() * 1.5;

            Location particleLoc = location.clone().add(x, y, z);
            Vector velocity = new Vector(x * 0.2, y * 0.1, z * 0.2);

            // Spawn flame particles with some velocity
            world.spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0, 0, null, true);
        }

        // Add some lava particles for extra effect
        for (int i = 0; i < 5; i++) {
            double offsetX = (Math.random() - 0.5) * 0.5;
            double offsetY = Math.random() * 0.5;
            double offsetZ = (Math.random() - 0.5) * 0.5;

            Location particleLoc = location.clone().add(offsetX, offsetY, offsetZ);
            world.spawnParticle(Particle.LAVA, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    private static void cleanupArrow(Arrow arrow) {
        if (arrow == null) return;

        UUID arrowId = arrow.getUniqueId();
        trackingArrows.remove(arrowId);
        arrowTrails.remove(arrowId);
    }

    public static void clearTrackingArrows() {
        for (Entity arrow : trackingArrows.values()) {
            if (arrow != null && arrow.isValid()) {
                arrow.remove();
            }
        }
        trackingArrows.clear();
        arrowTrails.clear();
    }
    public static int getActiveArrowCount() {
        return trackingArrows.size();
    }
}