package cwr.cutomIterm.Entity_Wand.CustomCraftingTable;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;

public class CustomCraftingTableListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        try {
            Player player = event.getPlayer();
            ItemStack item = event.getItemInHand();
            Block placedBlock = event.getBlockPlaced();

            // Check if placing a custom crafting table
            if (CustomCraftingTable.isCustomCraftingTable(item)) {
                // Mark the placed block as a custom table
                CustomCraftingTableBlock.markAsCustomTable(placedBlock);

                // Play special effects
                CustomCraftingTableBlock.playPlacementEffects(placedBlock.getLocation());

                // Start continuous particle effects
                CustomCraftingTableBlock.startParticleEffect(placedBlock.getLocation());

                // Send message to player
                player.sendMessage("§6§lAdvanced Crafting Table Placed!");
                player.sendMessage("§7Right-click to open the 6x6 crafting interface.");
            }
        } catch (Exception e) {
            // Silent fail
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        try {
            Block block = event.getBlock();

            // Check if breaking a custom crafting table
            if (CustomCraftingTableBlock.isPlacedCustomCraftingTable(block)) {
                // Cancel the event to handle drop manually
                event.setCancelled(true);

                // Drop the custom crafting table item
                block.getWorld().dropItemNaturally(block.getLocation(),
                        CustomCraftingTable.createCustomCraftingTable());

                // Set block to air
                block.setType(Material.AIR);

                // Play break effects
                block.getWorld().playSound(block.getLocation(),
                        org.bukkit.Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);

                // Send message
                event.getPlayer().sendMessage("§ePicked up Advanced Crafting Table!");
            }
        } catch (Exception e) {
            // Silent fail
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        try {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }

            if (event.getHand() != EquipmentSlot.HAND) {
                return;
            }

            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock == null) {
                return;
            }

            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            // Check if interacting with a placed custom crafting table
            if (CustomCraftingTableBlock.isPlacedCustomCraftingTable(clickedBlock)) {
                event.setCancelled(true); // Cancel default behavior

                // Open custom 6x6 crafting GUI
                CustomCraftingTableGUI gui = new CustomCraftingTableGUI(player);
                gui.open();

                return;
            }

            // Check if holding a custom crafting table
            if (CustomCraftingTable.isCustomCraftingTable(item)) {
                // Prevent placing on certain blocks
                if (clickedBlock.getType().isInteractable()) {
                    event.setCancelled(true);
                }
            }
        } catch (Exception e) {
            // Silent fail
        }
    }
}