package moe.timgor.eggfort;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Eggfort extends JavaPlugin{
    static boolean defenseMode;
    static Logger LOGGING;
    static int borderRadius;
    static FileConfiguration config;
    static JavaPlugin plugin;
    static Egg[] eggs;

    @Override
    public void onEnable() {
        LOGGING = Bukkit.getLogger();
        Server server = getServer();
        plugin = this;

        // register commands
        this.getCommand("eggtest").setExecutor(new Commands.CommandTest());
        this.getCommand("defensestart").setExecutor(new Commands.CommandStart());
        this.getCommand("defensestop").setExecutor(new Commands.CommandStop());
        this.getCommand("gameinit").setExecutor(new Commands.CommandInit());

        // register event listeners
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        // only happens if no config.yml
        this.saveDefaultConfig();

        config = this.getConfig();
        // read config values
        borderRadius = config.getInt("border-radius");
        LOGGING.info("[eggfort] Border radius: " + borderRadius);
        defenseMode = config.getBoolean("defense-mode");
        LOGGING.info("[eggfort] Defense mode: " + borderRadius);


    }

    @Override
    public void onDisable(){
    }

    public static void setDefenseMode(boolean mode){
        defenseMode = mode;
        config.set("defense-mode", mode);
        plugin.saveConfig();

        for(Egg e: eggs){
            e.resetTimer();
        }
    }

    public static void gameSetup(){

    }

}
