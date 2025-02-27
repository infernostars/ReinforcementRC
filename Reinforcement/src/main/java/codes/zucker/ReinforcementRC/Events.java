package codes.zucker.ReinforcementRC;

import java.util.*;

import codes.zucker.ReinforcementRC.util.LangYaml;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;

import codes.zucker.ReinforcementRC.entity.ReinforcedBlock;
import codes.zucker.ReinforcementRC.util.ConfigurationYaml;
import codes.zucker.ReinforcementRC.util.Utils;

public class Events implements Listener {

    static Set<ReinforcedBlock> visibleBlocks = new HashSet<>();

    @EventHandler
    public static void onClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        ReinforcedBlock reinforcedAtTarget = ReinforcedBlock.getAtLocation(event.getClickedBlock().getLocation());
        PlayerInventory inventory = player.getInventory();
        Material hand = inventory.getItemInMainHand().getType();
        Block block = event.getClickedBlock();

        // fix double printing on right click
        if (event.getHand().equals(EquipmentSlot.OFF_HAND)) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (Commands.rAdminModeToggle.contains(player) && reinforcedAtTarget != null) {
                Utils.sendMessage(player, LangYaml.getString("admin_who_placed") + Bukkit.getOfflinePlayer(reinforcedAtTarget.getOwner()).getName());
            }
            if (Commands.reToggle.contains(player) && reinforcedAtTarget != null && hand == Material.getMaterial(ConfigurationYaml.getString("reinforcement_material_remove_reinforcement"))) {
                int countToGive = reinforcedAtTarget.getMaterialCount();
                Material materialToGive = reinforcedAtTarget.getMaterialUsed().getMaterial();
                Location blockLocation = reinforcedAtTarget.getLocation();
                reinforcedAtTarget.destroyBlock(false);
                Objects.requireNonNull(blockLocation.getWorld()).dropItemNaturally(blockLocation, new ItemStack(materialToGive, countToGive));
            }
            return;
        }

        if (!Commands.reToggle.contains(player)) {
            if (player.getGameMode() == GameMode.CREATIVE && reinforcedAtTarget != null)
                reinforcedAtTarget.destroyBlock(false);
            return;
        }
        if (Commands.rAdminModeToggle.contains(player)) {
            if (reinforcedAtTarget != null)
                reinforcedAtTarget.destroyBlock(false);
            return;
        }

        
        ReinforceMaterial reinforceMaterial = canReinforceWithMaterial(hand);

        if (reinforceMaterial == null) return;

        if (reinforcedAtTarget != null) {
            if (reinforcedAtTarget.getBreaksLeft() >= reinforceMaterial.getMaxAllowedBreaks()) {
                event.setCancelled(true);
                return;
            }
            if (!reinforceMaterial.getMaterial().equals(reinforcedAtTarget.getMaterialUsed().getMaterial()))
                return;
        }

        // quick check to disallow reinforcing all blacklisted blocks in the config
        String materialName = event.getClickedBlock().getType().toString();

        if (!getReinforceable(materialName))
            return;

        Utils.removeFromHand(inventory, hand, 1);

        DustOptions particle = new DustOptions(Color.GRAY, 0.5f);
        for (int j = 0; j < Utils.random.nextInt(10) + 30; j++) { // fancy particles
            Location location = block.getLocation().clone();
            location.add(Utils.random.nextDouble() * 2f - 0.5f, Utils.random.nextDouble() * 2f - 0.5f, Utils.random.nextDouble() * 2f - 0.5f);
            player.getWorld().spawnParticle(Particle.REDSTONE, location, 5, particle);
        }

        ReinforcedBlock reinforced = reinforcedAtTarget != null ? reinforcedAtTarget : new ReinforcedBlock(block.getLocation(), 0, reinforceMaterial, player.getUniqueId());
        reinforced.reinforceBlock(reinforceMaterial.getBreaksPerReinforce());
        reinforced.displayNearest(player.getLocation());

        event.setCancelled(true);
    }

        public static boolean getReinforceable(String materialName) {
        for(String entry : (ArrayList<String>)ConfigurationYaml.getList("reinforcement_not_reinforceable")) {
            if (materialName.contains(entry) && !materialName.contains("BLOCK"))
                return false;
        }
        return true;
    }

    public static ReinforceMaterial canReinforceWithMaterial(Material material) {
        for(ReinforceMaterial reinforceMaterial : ReinforceMaterial.entries) {
            if (material.equals(reinforceMaterial.getMaterial())) {
                return reinforceMaterial;
            }
        }
        return null;
    }

    @EventHandler
    public static void playerMove(PlayerMoveEvent event) {
        Set<ReinforcedBlock> visibleBlocksClone = new HashSet<>(visibleBlocks);
        visibleBlocksClone.forEach(visibleBlock -> {
            if (visibleBlock.getLocation() != null && Utils.getPlayersNear(visibleBlock.getLocation(), 5).isEmpty()) {
                visibleBlock.getHologram().hideHologramDelayed();
                visibleBlocks.remove(visibleBlock);
            }
        });

        Player player = event.getPlayer();
        if (!Commands.rvToggle.contains(player)) return;
        Utils.getNearbyBlocks(player.getLocation(), 5).forEach(b -> {
            ReinforcedBlock reinforced = ReinforcedBlock.getAtLocation(b.getLocation());
            if (reinforced != null && 
                ((ConfigurationYaml.getBoolean("reinforcement_visibility_only_show_to_owner") && reinforced.getOwner().equals(player.getUniqueId()))
                || (!ConfigurationYaml.getBoolean("reinforcement_visibility_only_show_to_owner")))) {
                reinforced.displayNearest(player.getLocation(), false);
                visibleBlocks.add(reinforced);
            }
        });
        
    }

    @EventHandler
    public static void blockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ReinforcedBlock block = ReinforcedBlock.getAtLocation(event.getBlock().getLocation());
        if (block != null) {
            if (block.getOwner() == player.getUniqueId()) {

            }
            block.damageBlock(player.getEyeLocation(), 1);
            if (block.getBreaksLeft() >= 0) {
                boolean damageOnlyOnLastBreak = ConfigurationYaml.getBoolean("reinforcement_damage_on_break_only");

                if (!damageOnlyOnLastBreak || block.getBreaksLeft() == 0) {
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    if (hand.getItemMeta() instanceof Damageable) {
                        Damageable meta = (Damageable) hand.getItemMeta();
                        meta.setDamage(meta.getDamage() + 1);
                        hand.setItemMeta(meta);
                    }
                    player.updateInventory();
                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public static void onPistonPush(BlockPistonExtendEvent event) {
        List<Block> eventBlocks = event.getBlocks();
        for(Block eventBlock : eventBlocks) {
            ReinforcedBlock block = ReinforcedBlock.getAtLocation(eventBlock.getLocation());
            if (block != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public static void onPistonPull(BlockPistonRetractEvent event) {
        List<Block> eventBlocks = event.getBlocks();
        for(Block eventBlock : eventBlocks) {
            ReinforcedBlock block = ReinforcedBlock.getAtLocation(eventBlock.getLocation());
            if (block != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public static void onBlockExplode(BlockExplodeEvent event) {
        List<Block> affectedBlocks = event.blockList();
        Location center = event.getBlock().getLocation();
        blockExplode(center, affectedBlocks, false);
    }

    @EventHandler
    public static void onEntityExplode(EntityExplodeEvent event) {
        List<Block> affectedBlocks = event.blockList();
        Location center = event.getLocation();
        blockExplode(center, affectedBlocks, true);
    }

    static void blockExplode(Location sourceLocation, List<Block> affectedBlocks, boolean entityExplosion) {
        int maxDamage = ConfigurationYaml.getInt("reinforcement_explosion_strength");
        Iterator<Block> affectedBlockIterator = affectedBlocks.iterator();
        while(affectedBlockIterator.hasNext()) {
            Block block = affectedBlockIterator.next();
            ReinforcedBlock reinforcedBlock = ReinforcedBlock.getAtLocation(block.getLocation());
            if (reinforcedBlock == null || block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR) continue;
            affectedBlockIterator.remove();
            int calculatedDmg = (int)Math.floor(reinforcedBlock.getLocation().distance(sourceLocation));
            calculatedDmg = (int) (calculatedDmg * reinforcedBlock.getMaterialUsed().getExplosiveMultiplier());
            int damage = (int)Utils.clamp(maxDamage - calculatedDmg, 0, maxDamage);
            if (reinforcedBlock.getBreaksLeft() - damage <= 0) {
                if (entityExplosion) {
                    block.breakNaturally();
                }
                else {
                    reinforcedBlock.getLocation().getWorld().dropItemNaturally(reinforcedBlock.getLocation(), new ItemStack(reinforcedBlock.getLocation().getBlock().getType(), 1));
                    reinforcedBlock.getLocation().getBlock().setType(Material.AIR);
                }
            }
            reinforcedBlock.damageBlock(sourceLocation, damage);
        }
    }

    @EventHandler
    public static void fireSpread(BlockIgniteEvent event) {
        Block blockSpreadTo = event.getBlock();
        ReinforcedBlock reinforcedBlock = ReinforcedBlock.getAtLocation(blockSpreadTo.getLocation());
        boolean doFireSpread = ConfigurationYaml.getBoolean("reinforcement_blocks_can_burn");
        if (reinforcedBlock != null && !doFireSpread) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public static void fireDamage(BlockBurnEvent event) {
        Block blockSpreadTo = event.getBlock();
        ReinforcedBlock reinforcedBlock = ReinforcedBlock.getAtLocation(blockSpreadTo.getLocation());
        boolean doFireSpread = ConfigurationYaml.getBoolean("reinforcement_blocks_can_burn");
        if (reinforcedBlock != null) {
            event.setCancelled(true);
            if (doFireSpread)
                reinforcedBlock.damageBlock(reinforcedBlock.getLocation(), 1);
        }  
    }
}
