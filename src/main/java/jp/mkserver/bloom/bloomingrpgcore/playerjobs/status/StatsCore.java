package jp.mkserver.bloom.bloomingrpgcore.playerjobs.status;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import jp.mkserver.bloom.bloomingrpgcore.playerjobs.jobs.AbstractJob;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

public class StatsCore implements Listener, CommandExecutor {

    BloomingRPGCore plugin;
    public HashMap<UUID,Stats> playerStats = new HashMap<>();


    public StatsCore(BloomingRPGCore plugin){
        this.plugin = plugin;
        plugin.getCommand("skillpoint").setExecutor(this);
        plugin.getCommand("sp").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
        reloadPlayerStats();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,()->{
            for(Stats stats : playerStats.values()){
                Player p = Bukkit.getPlayer(stats.getUuid());
                if(p==null){
                    return;
                }
                playerSPheal(p,1);
            }
        },0,100);
    }

    public void playerSPheal(Player p,int healamount){
        Stats stats = getPlayerStats(p);
        stats.addSP(healamount);
        playerStats.put(stats.getUuid(),stats);
    }

    public boolean playerSPuse(Player p,int useamount){
        Stats stats = getPlayerStats(p);
        if(!stats.useSP(useamount)){
            return false;
        }
        playerStats.put(stats.getUuid(),stats);
        return true;
    }

    public Stats getPlayerStats(Player p){
        return playerStats.get(p.getUniqueId());
    }

    public void reloadPlayerStats(){
        for(Player p : Bukkit.getOnlinePlayers()){
            if(!playerStats.containsKey(p.getUniqueId())){
                int maxsp;
                AbstractJob job = plugin.job.getUserJob(p);
                if(job==null){
                    maxsp = 20;
                }else{
                    maxsp = job.getJob_skillpoint();
                }
                playerStats.put(p.getUniqueId(),new Stats(p.getUniqueId(),maxsp));
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(!playerStats.containsKey(e.getPlayer().getUniqueId())){
            int maxsp;
            AbstractJob job = plugin.job.getUserJob(e.getPlayer());
            if(job==null){
                maxsp = 20;
            }else{
                maxsp = job.getJob_skillpoint();
            }
            playerStats.put(e.getPlayer().getUniqueId(),new Stats(e.getPlayer().getUniqueId(),maxsp));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            return true;
        }
        Player p = (Player) sender;
        if(args.length == 0){
            AbstractJob job = plugin.job.getUserJob(p);
            p.sendMessage("§6"+job.getJob_spName()+"§f: §a"+getPlayerStats(p).getSp());
        }
        return true;
    }
}
