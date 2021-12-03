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
                Eggfort.mapSetup();
                commandSender.sendMessage("Set up map!");
                return true;
            } else {
                commandSender.sendMessage("You are not oped!");
                return false;
            }
        }
    }

    // start defense part of game
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
            if(commandSender instanceof Player){
                commandSender.sendMessage("Player team " + Eggfort.getPlayerTeam((Player) commandSender));
                return true;
            } else {
                commandSender.sendMessage("Must be sent by player, dummy");
                return false;
            }
        }
    }
}
