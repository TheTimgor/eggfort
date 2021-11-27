package moe.timgor.eggfort;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands {

    // init command
    // set up map and teams
    static class CommandInit implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
            if(commandSender.isOp()){
                World overworld = Bukkit.getWorld("world");
                // set border
                overworld.getWorldBorder().setSize(Eggfort.borderRadius);
                commandSender.sendMessage("Set world border to " + Eggfort.borderRadius);
                // place egg + platform for each team
                for(int t=-1; t<2; t+=2){
                    // test if eggs are already placed
                    boolean hasEgg = false;
                    Block testEgg = Bukkit.getWorld("world").getHighestBlockAt(t*Eggfort.borderRadius/3,0);
                    while (!hasEgg && testEgg.getY() > 0){
                        testEgg = Bukkit.getWorld("world").getBlockAt(testEgg.getLocation().add(0,-1,0));
                        hasEgg = testEgg.getType() == Material.DRAGON_EGG;
                    }
                    if (hasEgg){
                        commandSender.sendMessage("Eggs already placed, not regenerating");
                    } else {
                        Location loc = overworld.getHighestBlockAt(
                                new Location(overworld, t*(double)Eggfort.borderRadius/3, 0,0)
                        ).getLocation();
                        // create bedrock platform
                        for (int i = -1; i < 2; i++){
                            for (int j = -1; j < 2; j++){
                                overworld.getBlockAt(loc.clone().add(i,0,j)).setType(Material.BEDROCK);
                            }
                        }
                        // fill rest with air
                        for (int i = -1; i < 2; i++){  // x
                            for (int j = -1; j < 2; j++){ // z
                                for (int k = 1; k < 4; k++){  // y
                                    overworld.getBlockAt(loc.clone().add(i,k,j)).setType(Material.AIR);
                                }
                            }
                        }
                        // place egg
                        overworld.getBlockAt(loc.clone().add(0,1,0)).setType(Material.DRAGON_EGG);
                        commandSender.sendMessage("Placed eggs");
                    }
                }
                Eggfort.eggs = new Egg[]{new Egg(1), new Egg(-1)};
                return true;
            } else {
                commandSender.sendMessage("You are not oped!");
                return false;
            }
        }
    }

    // start defense part of game
    // just sets a flag lol
    static class CommandStart implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
            if(commandSender.isOp()){
                Eggfort.setDefenseMode(true);
                commandSender.sendMessage("Defense mode start!");
                return true;
            } else {
                commandSender.sendMessage("You are not oped!");
                return false;
            }
        }
    }

    // stop defense part of game
    // also just sets a flag
    static class CommandStop implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
            if(commandSender.isOp()){
                Eggfort.setDefenseMode(false);
                commandSender.sendMessage("Defense mode stop!");
                return true;
            } else {
                commandSender.sendMessage("You are not oped!");
                return false;
            }
        }
    }



    static class CommandTest implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

            commandSender.sendMessage("Defense mode " + Eggfort.defenseMode);

            return true;
        }
    }
}
