package moe.timgor.eggfort;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.logging.Logger;

public class Eggfort extends JavaPlugin {
    static boolean defenseMode;
    static Logger LOGGING;
    static int borderRadius;
    static FileConfiguration config;
    static JavaPlugin plugin;
    static Egg[] eggs;
    static GameLoop gameLoop;

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
        setDefenseMode(config.getBoolean("defense-mode"));
        LOGGING.info("[eggfort] Defense mode: " + defenseMode);
        // start game loop
        gameLoop = new GameLoop();
        gameLoop.runTaskTimer(this, 0, 20);


    }

    @Override
    public void onDisable() {

    }

    public static void setDefenseMode(boolean mode) {
        defenseMode = mode;
        config.set("defense-mode", mode);
        plugin.saveConfig();
        // guarantee that map is set up, mostly so egg objects get created
        if (defenseMode)
            mapSetup();
        for (Egg e : eggs) {
            e.resetEgg();
        }
    }

    public static void mapSetup() {
        World overworld = Bukkit.getWorld("world");
        // set border
        overworld.getWorldBorder().setSize(borderRadius);
        LOGGING.info("[eggfort] Set world border to " + borderRadius);
        // place egg + platform for each team
        for (int t = -1; t < 2; t += 2) {
            // test if eggs are already placed
            boolean hasEgg = false;
            Block testEgg = Bukkit.getWorld("world").getHighestBlockAt(t * borderRadius / 3, 0);
            while (!hasEgg && testEgg.getY() > 0) {
                hasEgg = testEgg.getType() == Material.DRAGON_EGG;
                testEgg = Bukkit.getWorld("world").getBlockAt(testEgg.getLocation().add(0, -1, 0));
            }
            if (hasEgg) {
                LOGGING.info("[eggfort] Eggs already placed, not regenerating");
            } else {
                Location loc = overworld.getHighestBlockAt(
                        new Location(overworld, t * (double) borderRadius / 3, 0, 0)
                ).getLocation();
                // create bedrock platform
                for (int i = -1; i < 2; i++) {
                    for (int j = -1; j < 2; j++) {
                        overworld.getBlockAt(loc.clone().add(i, 0, j)).setType(Material.BEDROCK);
                    }
                }
                // fill rest with air
                for (int i = -1; i < 2; i++) {  // x
                    for (int j = -1; j < 2; j++) { // z
                        for (int k = 1; k < 4; k++) {  // y
                            overworld.getBlockAt(loc.clone().add(i, k, j)).setType(Material.AIR);
                        }
                    }
                }
                // place egg
                overworld.getBlockAt(loc.clone().add(0, 1, 0)).setType(Material.DRAGON_EGG);
                LOGGING.info("[eggfort] Placed eggs");
            }
        }
        eggs = new Egg[]{new Egg(-1), new Egg(1)};
    }

    public static void stopGame() {
        Bukkit.broadcastMessage("Stopping game!");
    }

    public static int getPlayerTeam(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getEntryTeam(player.getName());
        if (team != null){
            LOGGING.info("[eggfort] Team " + team.getName());
            if (team.getName().equals("red")) {
                return 1;
            } else if (team.getName().equals("blue")) {
                return -1;
            }
        }
        return 0;
    }

    public static Egg getEgg(int team){
        return eggs[(team+1)/2];
    }


    class GameLoop extends BukkitRunnable {
        @Override
        public void run() {
            if(Eggfort.defenseMode){
                for(Egg egg: eggs){
                    if(egg.timeSinceFed() > 60*5){
                        Bukkit.broadcastMessage("Egg starved! team " + egg.team);
                        egg.resetEgg();
                    }
                }
            }
        }
    }

}
