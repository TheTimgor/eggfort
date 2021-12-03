package moe.timgor.eggfort;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.*;

public class EventListener implements Listener {

    Tag<Material> resource = Tag.LOGS;

    @EventHandler
    public void OnBlockBreak(BlockBreakEvent event){
        // no breaking blocks on team turf during defense mode
        if(isOnTeamTurf(event)){
            Eggfort.LOGGING.info("[eggfort] Cancelled block break event");
            event.setCancelled(true);
        }
        // no breaking egg EVAH!!
        if(event.getBlock().getType() == Material.DRAGON_EGG){
            Eggfort.LOGGING.info("[eggfort] Cancelled egg break");
            event.setCancelled(true);
        }
        if(Eggfort.defenseMode && !isOnTeamTurf(event)){
            Egg egg = Eggfort.getEgg(Eggfort.getPlayerTeam(event.getPlayer()));
            // if the block broken is of the correct type
            if(egg.resource.isTagged(event.getBlock().getType())){
                Eggfort.LOGGING.info("[eggfort] Player collected correct resource " + egg.incrementCollected(event.getPlayer()));

            }
        }
    }

    @EventHandler
    public void OnBlockPlace(BlockPlaceEvent event){
        // no placing blocks on team turf during defense mode
        if(isOnTeamTurf(event)){
            Eggfort.LOGGING.info("[eggfort] Cancelled block place event");
            event.setCancelled(true);
            // summon lit TNT instead of place
            if(event.getBlock().getType() == Material.TNT){
                event.getBlock().getWorld().spawnEntity(event.getBlock().getLocation(), EntityType.PRIMED_TNT);
            }
        }
    }

    @EventHandler
    public void OnBucketFill(PlayerBucketFillEvent event){
        Eggfort.LOGGING.info("[eggfort] " + event.getBucket());
        if(isOnTeamTurf(event)){
            Eggfort.LOGGING.info("[eggfort] Cancelled bucket fill event");
            event.setCancelled(true);
            // allow players to remove water/lava but not fill the bucket
            // slightly discourages lava spam or water blastproofing
            BlockData data = event.getBlock().getBlockData();
            if(event.getBlock().getType() == Material.LAVA || event.getBlock().getType() == Material.WATER){
                event.getBlock().setType(Material.AIR);
            } else if (data instanceof Waterlogged && ((Waterlogged) data).isWaterlogged()) {
                Eggfort.LOGGING.info("[eggfort] emptied waterlogged block");
                ((Waterlogged) data).setWaterlogged(false);
                event.getBlock().setBlockData(data);
            }
        }
    }

    @EventHandler
    public void OnBucketEmpty(PlayerBucketEmptyEvent event){
        Eggfort.LOGGING.info("[eggfort] " + event.getBucket());
        if(isOnTeamTurf(event)){
            Eggfort.LOGGING.info("[eggfort] Cancelled bucket empty event");
            event.setCancelled(true);
        }
    }

    // handlers below just to prevent the egg from getting moved

    @EventHandler
    public void OnBlockFromTo(BlockFromToEvent event){
        if(event.getBlock().getType() == Material.DRAGON_EGG){
            Eggfort.LOGGING.info("[eggfort] Cancelled egg teleport");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void OnBlockPistonExtend(BlockPistonExtendEvent event){
        for (Block b: event.getBlocks()) {
            if(b.getType() == Material.DRAGON_EGG){
                Eggfort.LOGGING.info("[eggfort] Cancelled egg piston push");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void OnBlockPistonRetract(BlockPistonRetractEvent event){
        for (Block b: event.getBlocks()) {
            if(b.getType() == Material.DRAGON_EGG){
                Eggfort.LOGGING.info("[eggfort] Cancelled egg piston pull");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void OnEntityExplode(EntityExplodeEvent event){
        Block remove = null;
        for (Block b: event.blockList()) {
            if(b.getType() == Material.DRAGON_EGG){
                Eggfort.LOGGING.info("[eggfort] Cancelled egg explode");
                remove = b;
            }
        }
        if(remove != null){
            event.blockList().remove(remove);
        }
    }

    @EventHandler
    public void OnBlockExplode(BlockExplodeEvent event){
        Block remove = null;
        for (Block b: event.blockList()) {
            if(b.getType() == Material.DRAGON_EGG){
                Eggfort.LOGGING.info("[eggfort] Cancelled egg explode");
                remove = b;
            }
        }
        if(remove != null){
            event.blockList().remove(remove);
        }
    }

    // handler for interacting with the egg

    @EventHandler
    public void OnPlayerInteract(PlayerInteractEvent event){
        if(  // if player right clicks dragon egg
                event.getClickedBlock() != null
                && event.getClickedBlock().getType() == Material.DRAGON_EGG
                && event.getAction() == Action.RIGHT_CLICK_BLOCK
        ){
            if(Eggfort.defenseMode){
                Egg egg = Eggfort.getEgg(Integer.signum(event.getClickedBlock().getX()));

                if(Eggfort.getPlayerTeam(event.getPlayer()) == egg.team) {
                    if(egg.checkPlayer(event.getPlayer())){
                        egg.removeItems(event.getPlayer());
                        egg.resetEgg();
                        event.getPlayer().sendMessage("The egg is fed!");
                    } else {
                        event.getPlayer().sendMessage("The egg grumbles at you");
                        event.getPlayer().playSound(event.getClickedBlock().getLocation(), Sound.ENTITY_SILVERFISH_HURT, 1, 1);
                        int collected = egg.collected.getOrDefault(event.getPlayer().getName(), 0);
                        event.getPlayer().sendMessage("Collected: " + collected);
                        event.getPlayer().sendMessage("Timer: " + egg.timeSinceFed());
                    }
                } else {
                    Bukkit.broadcastMessage("Egg captured!! " + egg.team);
                }

            } else{
                event.getPlayer().sendMessage("The egg chitters at you");
                event.getPlayer().playSound(event.getClickedBlock().getLocation(), Sound.ENTITY_SILVERFISH_AMBIENT, 1, 2);
            }
        }
    }

    // respawn player in neutral territory during defense portion
    // pos is random solid block y >= 62 to prevent spawncamping

    @EventHandler
    public void OnPlayerRespawn(PlayerRespawnEvent event){
        if(Eggfort.defenseMode){
            Block b;
            int xRange = Eggfort.borderRadius / 6;
            int yRange = Eggfort.borderRadius / 2;
            // re-roll while block is invalid
            do {
                 b = Bukkit.getWorld("world").getHighestBlockAt(
                         (int)(xRange*(2*Math.random()-1)),
                         (int)(yRange*(2*Math.random()-1))
                 );
            } while (b.isLiquid() || b.getY() < 62);
            event.setRespawnLocation(b.getLocation());
        }
    }

    public boolean isOnTeamTurf(BlockEvent event) {
        return isOnTeamTurf(event.getBlock().getLocation());
    }

    public boolean isOnTeamTurf(PlayerBucketEvent event) {
        return isOnTeamTurf(event.getBlock().getLocation());
    }

    // check if defense mode is enabled and location is on team territory
    public boolean isOnTeamTurf(Location loc) {
        return Eggfort.defenseMode && Math.abs(loc.getX()) > (double)Eggfort.borderRadius / 6;
    }
}
