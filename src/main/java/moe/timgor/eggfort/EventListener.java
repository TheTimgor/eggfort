package moe.timgor.eggfort;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.*;

public class EventListener implements Listener {

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
            if(event.getBlock().getType() == Material.LAVA || event.getBlock().getType() == Material.WATER){
                event.getBlock().setType(Material.AIR);
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
                event.getPlayer().sendMessage("The egg grumbles at you");
                event.getPlayer().playSound(event.getClickedBlock().getLocation(), Sound.ENTITY_SILVERFISH_HURT, 1, 1);

                // this side's egg
                Egg egg = Eggfort.eggs[(Integer.signum(event.getClickedBlock().getX())+1)/2];
                event.getPlayer().sendMessage("Timer: " + egg.timeSinceFed());
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

    public boolean isOnTeamTurf(Location loc) {
        return Eggfort.defenseMode && Math.abs(loc.getX()) > (double)Eggfort.borderRadius / 6;
    }
}
