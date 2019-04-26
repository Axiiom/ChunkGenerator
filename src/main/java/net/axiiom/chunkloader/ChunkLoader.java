package net.axiiom.chunkloader;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class ChunkLoader extends JavaPlugin {

    private GENERATOR tp;

    @Override
    public void onEnable() {
        // Plugin startup logic
        tp = new GENERATOR();
        tp.runTaskTimer(this, 0L, 20L);

        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender _sender, Command _command, String _label, String[] _args)
    {
        if(_sender instanceof Player && _command.getName().equalsIgnoreCase("generate")) {
            tp.setPlayer((Player) _sender);
            _sender.sendMessage("[" + ChatColor.GOLD + "ChunkGenerator" + ChatColor.WHITE + "] Beginning World Generation.");
            return true;
        }

        return false;
    }

    private class GENERATOR extends BukkitRunnable
    {

        private int generationDistance;
        private int viewDistance;
        private int waitTime;

        private Player player;
        private long runTime;

        private boolean reachedTop;
        private boolean reachedBot;
        private boolean canStart;

        private Location middleOfMap;


        public GENERATOR() {
            canStart = false;
            reachedTop = false;
            reachedBot = true;

            generationDistance = getConfig().getInt("generation-distance");
            viewDistance = getConfig().getInt("view-distance") * 16 * 2;
            waitTime = getConfig().getInt("wait-time") * 1000;
        }

        public void setPlayer(Player _player) {
            runTime = System.currentTimeMillis();
            player = _player;
            canStart = true;

            Location bottomLeft = player.getLocation();
            bottomLeft.setZ(bottomLeft.getZ() - generationDistance);
            bottomLeft.setX(bottomLeft.getX() - generationDistance);
            bottomLeft.setY(200.0);

            middleOfMap = player.getLocation();
            player.teleport(bottomLeft);
        }

        public void run() {
            if (canStart && System.currentTimeMillis() - waitTime >= runTime
                    && player.getLocation().getX() <= middleOfMap.getX() + generationDistance)
            {
                player.sendMessage("[" + ChatColor.GOLD + "ChunkGenerator" + ChatColor.WHITE + "] GENERATING NEXT SET OF CHUNKS....");
                if (!reachedTop) {
                    Location newLocation = player.getLocation();
                    newLocation.setZ(newLocation.getZ() + viewDistance);
                    player.teleport(newLocation);

                    if (player.getLocation().getZ() >= middleOfMap.getZ() + generationDistance) {
                        reachedTop = true;
                        reachedBot = false;
                        Location tpRight = player.getLocation();
                        tpRight.setX(player.getLocation().getX() + viewDistance);

                        player.teleport(tpRight);
                        player.sendMessage("[" + ChatColor.GOLD + "ChunkGenerator" + ChatColor.WHITE + "] REACHED TOP");
                    }
                }

                if (!reachedBot) {
                    Location newLocation = player.getLocation();
                    newLocation.setZ(newLocation.getZ() - viewDistance);
                    player.teleport(newLocation);

                    if (player.getLocation().getZ() <= middleOfMap.getZ() - generationDistance) {
                        reachedBot = true;
                        reachedTop = false;
                        Location tpRight = player.getLocation();
                        tpRight.setX(player.getLocation().getX() + viewDistance);

                        player.teleport(tpRight);
                        player.sendMessage("[" + ChatColor.GOLD + "ChunkGenerator" + ChatColor.WHITE + "] REACHED BOT");
                    }
                }

                runTime = System.currentTimeMillis();
            } else if (player != null && player.getLocation().getX() >= middleOfMap.getX() + generationDistance) {
                player.sendMessage("[" + ChatColor.GOLD + "ChunkGenerator" + ChatColor.WHITE + "] GENERATION COMPLETED");
                System.out.print("[" + ChatColor.GOLD + "ChunkGenerator" + ChatColor.WHITE + "] GENERATION COMPLETED");
                this.cancel();
            }
        }
    }
}
