package jp.mkserver.bloom.bloomingrpgcore.extra;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Spawn implements CommandExecutor , Listener {


    BloomingRPGCore plugin;

    public Spawn(BloomingRPGCore plugin){
        this.plugin = plugin;
        plugin.getCommand("spawn").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }

    public Location getLocaton(){
        return new Location(Bukkit.getWorld(plugin.config.getString("spawnpoint.world")),
                plugin.config.getDouble("spawnpoint.x"),
                plugin.config.getDouble("spawnpoint.y"),
                plugin.config.getDouble("spawnpoint.z"));
    }

    public void setLocation(Location loc){
        plugin.config.set("spawnpoint.world",loc.getWorld().getName());
        plugin.config.set("spawnpoint.x",loc.getX());
        plugin.config.set("spawnpoint.y",loc.getY());
        plugin.config.set("spawnpoint.z",loc.getZ());
        plugin.saveConfig();
    }

    List<UUID> cooldown = new ArrayList<>();


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player p = (Player) sender;

        if(args.length == 0){
            if(cooldown.contains(p.getUniqueId())){
                p.sendMessage("§e???: §f働きたくないでござる");
                return true;
            }
            cooldown.add(p.getUniqueId());
            p.sendMessage("§e???: §f5秒間動かなければ 転送してやろう…");
            p.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), 50, 0, 0, 0);
            for(Player pp : Bukkit.getOnlinePlayers()){
                pp.playSound(p.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT,1.0f,1.5f);
            }
            Location loc = p.getLocation();
            String world = loc.getWorld().getName();
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            Bukkit.getScheduler().runTaskLater(plugin,()->{
                cooldown.remove(p.getUniqueId());
                p.sendMessage("§e???: §fもう転送してもええで");
            },20*30);
            Bukkit.getScheduler().runTaskLater(plugin,()->{
                Location locs = p.getLocation();
                String worlds = locs.getWorld().getName();
                double xs = locs.getX();
                double ys = locs.getY();
                double zs = locs.getZ();
                if(world.equalsIgnoreCase(worlds)&&x==xs&&y==ys&&z==zs){
                    p.sendMessage("§e謎の力で転送された！");
                    p.teleport(getLocaton());
                    p.getWorld().spawnParticle(Particle.END_ROD, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), 50, 0, 0, 0);
                    for(Player pp : Bukkit.getOnlinePlayers()){
                        pp.playSound(p.getLocation(), Sound.ENTITY_SHULKER_TELEPORT,1.0f,2.0f);
                    }
                }else{
                    p.sendMessage("§e???: §f動いてしまったようだな…。すまないが 転送できん。");
                    for(Player pp : Bukkit.getOnlinePlayers()){
                        pp.playSound(p.getLocation(), Sound.ENTITY_SHULKER_TELEPORT,1.0f,0.5f);
                    }
                }
            },100);
            return true;
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("set")&&p.hasPermission("brpg.setspawn")){
                setLocation(p.getLocation());
                p.sendMessage("§eスポーン地点をセットしました。");
                return true;
            }
        }

        if(p.hasPermission("brpg.setspawn")){
            p.sendMessage("§e/spawn set : スポーン地点をセット");
        }
        p.sendMessage("§a/spawn : 神様がスポーン地点に転送してくれます。5秒間動いてはいけません。クールダウン30秒");
        return true;
    }

    @EventHandler
    public void onDeathScreenCancel(PlayerDeathEvent e){
        Player p = e.getEntity();
        p.setBedSpawnLocation(plugin.spawn.getLocaton());
        p.spigot().respawn();
    }
}
