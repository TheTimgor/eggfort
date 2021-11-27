package moe.timgor.eggfort;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class Egg {
    private long lastFed;
    private Block block;
    private int team;

    public Egg(int team) {
        this.team = team;
        Block block = Bukkit.getWorld("world").getHighestBlockAt(team*Eggfort.borderRadius/3,0);
        while (block.getType() != Material.DRAGON_EGG){
            block = Bukkit.getWorld("world").getBlockAt(block.getLocation().add(0,-1,0));
        }
        this.block = block;
        resetTimer();
    }

    public void resetTimer(){
        lastFed = System.currentTimeMillis();
    }

    // seconds
    public int timeSinceFed(){
        return (int)((System.currentTimeMillis()-lastFed)/1000);
    }

}
