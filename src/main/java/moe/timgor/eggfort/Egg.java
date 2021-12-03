package moe.timgor.eggfort;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class Egg {
    long lastFed;
    Block block;
    int team;
    Tag<Material> resource;
    int resourceAmount;
    HashMap<String, Integer> collected;

    public Egg(int team) {
        this.team = team;
        Block block = Bukkit.getWorld("world").getHighestBlockAt(team*Eggfort.borderRadius/3,0);
        while (block.getType() != Material.DRAGON_EGG){
            block = Bukkit.getWorld("world").getBlockAt(block.getLocation().add(0,-1,0));
        }
        this.block = block;
        resetEgg();

    }

    public boolean checkPlayer(Player player){
        if(collected.getOrDefault(player.getName(), 0) < resourceAmount ){
            return false;
        }

        int count = 0;
        boolean hasEnough = false;

        for(ItemStack stack: player.getInventory()){
            if(stack != null && resource.isTagged(stack.getType())){
                int stackSize = stack.getAmount();
                if(resourceAmount - count > stackSize){
                    count += stackSize;
                } else {
                    return true;
                }
            }
        }

        return false;
    }

    public void removeItems(Player player){
        int count = 0;
        boolean hasEnough = false;

        for(ItemStack stack: player.getInventory()){
            if(stack != null && resource.isTagged(stack.getType())){
                int stackSize = stack.getAmount();
                if(resourceAmount - count > stackSize){
                    count += stackSize;
                    stack.setAmount(0);
                } else {
                    stack.setAmount(stackSize - resourceAmount + count);
                    break;
                }
            }
        }

    }

    public int incrementCollected(Player player){
        return collected.put(player.getName(), collected.getOrDefault(player.getName(), 0)+1);
    }

    public void resetEgg(){
        lastFed = System.currentTimeMillis();
        resource = Tag.LOGS;
        resourceAmount = 16;
        collected = new HashMap<String, Integer>();
    }

    // seconds
    public int timeSinceFed(){
        return (int)((System.currentTimeMillis()-lastFed)/1000);
    }

}
