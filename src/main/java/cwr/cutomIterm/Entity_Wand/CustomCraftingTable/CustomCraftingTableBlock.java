package cwr.cutomIterm.Entity_Wand.CustomCraftingTable;

import cwr.cutomIterm.Entity_Wand.EntityWandPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class CustomCraftingTableBlock {

    private static final NamespacedKey PLACED_TABLE_KEY = new NamespacedKey(EntityWandPlugin.getInstance(), "placed_custom_table");

    public static boolean isPlacedCustomCraftingTable(Block block) {
        if (block == null || block.getType() != Material.CRAFTING_TABLE) {
            return false;
        }

        BlockState state = block.getState();
        if (state instanceof TileState) {
            TileState tileState = (TileState) state;
            PersistentDataContainer pdc = tileState.getPersistentDataContainer();
            return pdc.has(PLACED_TABLE_KEY, PersistentDataType.BYTE);
        }

        return false;
    }

    public static void markAsCustomTable(Block block) {
        BlockState state = block.getState();
        if (state instanceof TileState) {
            TileState tileState = (TileState) state;
            PersistentDataContainer pdc = tileState.getPersistentDataContainer();
            pdc.set(PLACED_TABLE_KEY, PersistentDataType.BYTE, (byte) 1);
            tileState.update();
        }
    }

    public static void playPlacementEffects(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        // Dragon's breath particles
        for (int i = 0; i < 10; i++) {
            double offsetX = (Math.random() - 0.5) * 1.5;
            double offsetY = Math.random() * 2;
            double offsetZ = (Math.random() - 0.5) * 1.5;

            Location particleLoc = location.clone().add(0.5, 0.5, 0.5)
                    .add(offsetX, offsetY, offsetZ);

            world.spawnParticle(Particle.DRAGON_BREATH, particleLoc, 1, 0, 0, 0, 0);
        }

        // Try ENCHANTMENT_TABLE, fall back to ENCHANT if needed
        Particle enchantParticle;
        try {
            enchantParticle = Particle.valueOf("ENCHANTMENT_TABLE");
        } catch (IllegalArgumentException e) {
            // For newer versions, use ENCHANT
            enchantParticle = Particle.ENCHANT;
        }

        // Enchantment table particles
        for (int i = 0; i < 5; i++) {
            double offsetX = (Math.random() - 0.5) * 2;
            double offsetY = Math.random() * 2;
            double offsetZ = (Math.random() - 0.5) * 2;

            Location particleLoc = location.clone().add(0.5, 0.5, 0.5)
                    .add(offsetX, offsetY, offsetZ);

            world.spawnParticle(enchantParticle, particleLoc, 1, 0, 0, 0, 0);
        }

        // Sound effects
        world.playSound(location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 0.8f);
        world.playSound(location, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.2f);
        world.playSound(location, Sound.BLOCK_ANVIL_PLACE, 0.7f, 1.0f);
    }

    public static void startParticleEffect(Location location) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if block is still a custom crafting table
                Block block = location.getBlock();
                if (!isPlacedCustomCraftingTable(block)) {
                    this.cancel();
                    return;
                }

                World world = location.getWorld();
                if (world == null) {
                    this.cancel();
                    return;
                }

                // Create continuous particle effects
                double x = location.getX() + 0.5;
                double y = location.getY() + 1.0;
                double z = location.getZ() + 0.5;

                // Dragon's breath particles floating upward
                for (int i = 0; i < 3; i++) {
                    double offsetX = (Math.random() - 0.5) * 0.8;
                    double offsetZ = (Math.random() - 0.5) * 0.8;

                    Location particleLoc = new Location(world, x + offsetX, y, z + offsetZ);
                    world.spawnParticle(Particle.DRAGON_BREATH, particleLoc, 1, 0, 0.1, 0, 0);
                }

                // Try ENCHANTMENT_TABLE, fall back to ENCHANT if needed
                Particle enchantParticle;
                try {
                    enchantParticle = Particle.valueOf("ENCHANTMENT_TABLE");
                } catch (IllegalArgumentException e) {
                    // For newer versions, use ENCHANT
                    enchantParticle = Particle.ENCHANT;
                }

                // Enchantment particles around the table
                double angle = System.currentTimeMillis() / 1000.0;
                double radius = 0.7;

                double particleX = x + Math.cos(angle) * radius;
                double particleZ = z + Math.sin(angle) * radius;

                world.spawnParticle(enchantParticle,
                        new Location(world, particleX, y, particleZ),
                        1, 0, 0, 0, 0);

                // Try SPELL_WITCH, fall back to SPELL_MOB if needed
                Particle witchParticle;
                try {
                    witchParticle = Particle.valueOf("SPELL_WITCH");
                } catch (IllegalArgumentException e) {
                    // For newer versions, use SPELL_MOB or check for alternatives
                    try {
                        witchParticle = Particle.valueOf("SPELL_MOB");
                    } catch (IllegalArgumentException e2) {
                        // For even newer versions, try ENTITY_EFFECT
                        witchParticle = Particle.ENTITY_EFFECT;
                    }
                }

                // Purple particles for magical effect
                if (Math.random() < 0.3) {
                    double purpleX = x + (Math.random() - 0.5) * 1.2;
                    double purpleY = y + Math.random() * 0.5;
                    double purpleZ = z + (Math.random() - 0.5) * 1.2;

                    world.spawnParticle(witchParticle,
                            new Location(world, purpleX, purpleY, purpleZ),
                            1, 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(EntityWandPlugin.getInstance(), 0L, 10L); // Run every half second
    }
}