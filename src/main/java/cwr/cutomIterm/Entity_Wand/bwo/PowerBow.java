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
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack bow = event.getBow();

        if (!isPowerBow(bow)) return;

        if (event.getProjectile() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getProjectile();
            arrow.getPersistentDataContainer().set(TRACKING_ARROW_KEY, PersistentDataType.BYTE, (byte) 1);
            trackingArrows.put(arrow.getUniqueId(), arrow);
            startTrackingTask(arrow, player);

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.5f);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RETURN, 0.8f, 1.2f);

            player.sendMessage("§6§lPower of Gamiya Activated!");
            player.sendMessage("§7Arrow will track nearest entity...");
        }
    }

    private static void startTrackingTask(Arrow arrow, Player shooter) {
        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 200;

            @Override
            public void run() {
                if (arrow == null || arrow.isDead() || arrow.isOnGround() || !arrow.isValid()) {
                    trackingArrows.remove(arrow.getUniqueId());
                    this.cancel();
                    return;
                }

                if (ticks >= MAX_TICKS) {
                    trackingArrows.remove(arrow.getUniqueId());
                    arrow.remove();
                    this.cancel();
                    return;
                }

                Entity nearestEntity = findNearestEntity(arrow, shooter);

                if (nearestEntity != null) {
                    Location arrowLoc = arrow.getLocation();
                    Location entityLoc = nearestEntity.getLocation();
                    entityLoc.setY(entityLoc.getY() + 0.5);

                    Vector direction = entityLoc.toVector().subtract(arrowLoc.toVector()).normalize();
                    Vector currentVelocity = arrow.getVelocity();
                    Vector newVelocity = currentVelocity.clone().add(direction.multiply(0.3)).normalize().multiply(currentVelocity.length());
                    arrow.setVelocity(newVelocity);

                    arrow.getWorld().spawnParticle(Particle.CRIT, arrow.getLocation(), 3, 0.1, 0.1, 0.1, 0);
                }

                ticks++;
            }
        }.runTaskTimer(EntityWandPlugin.getInstance(), 1L, 1L);
    }

    private static Entity findNearestEntity(Arrow arrow, Player shooter) {
        Location arrowLoc = arrow.getLocation();
        Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : arrow.getWorld().getNearbyEntities(arrowLoc, 15, 15, 15)) {
            if (entity == shooter || entity == arrow ||
                    entity instanceof Item || entity instanceof Arrow ||
                    entity instanceof Projectile || entity instanceof ExperienceOrb) {
                continue;
            }

            if (!(entity instanceof LivingEntity living) || living.isDead()) {
                continue;
            }

            double distance = arrowLoc.distance(entity.getLocation());
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
        if (!(event.getEntity() instanceof Arrow arrow)) return;

        UUID arrowId = arrow.getUniqueId();
        if (!trackingArrows.containsKey(arrowId)) return;

        Entity hitEntity = event.getHitEntity();
        org.bukkit.block.Block hitBlock = event.getHitBlock();
        Location hitLocation = hitEntity != null ? hitEntity.getLocation() :
                hitBlock != null ? hitBlock.getLocation().add(0.5, 1, 0.5) :
                        arrow.getLocation();

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

        trackingArrows.remove(arrowId);
        arrow.remove();

        if (arrow.getShooter() instanceof Player shooter) {
            shooter.sendMessage("§6⚡ §lLightning Strike! §e" +
                    (hitEntity != null ? "Hit " + hitEntity.getName() + "!" : "Target destroyed!"));
        }
    }

    public static void clearTrackingArrows() {
        for (Entity arrow : trackingArrows.values()) {
            if (arrow != null && arrow.isValid()) {
                arrow.remove();
            }
        }
        trackingArrows.clear();
    }
}