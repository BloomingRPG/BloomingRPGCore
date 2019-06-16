package jp.mkserver.bloom.bloomingrpgcore.playerjobs.status;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import jp.mkserver.bloom.bloomingrpgcore.playerjobs.StatsViewer;
import jp.mkserver.bloom.bloomingrpgcore.playerjobs.jobs.Job;
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
        plugin.getCommand("stats").setExecutor(this);
        plugin.getCommand("st").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
        reloadPlayerStats();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,()->{
            for(Player p :Bukkit.getOnlinePlayers()){
                Job job = plugin.job.getUserJob(p);
                StatsViewer.showActionBar(p,"§6Lv."+plugin.job.getUserLevel(p)+" §e"+job.getJob_spName()+"§e: §a"+getPlayerStats(p).getSp()+"§f/§b"+getPlayerStats(p).getMaxsp());
            }
        },0,2);
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

    private void reloadPlayerStats(){
        for(Player p : Bukkit.getOnlinePlayers()){
            if(!playerStats.containsKey(p.getUniqueId())){
                int maxsp;
                Job job = plugin.job.getUserJob(p);
                if(job==null){
                    return;
                }else{
                    maxsp = job.getJob_skillpoint(plugin.job.getUserLevel(p));
                }
                playerStats.put(p.getUniqueId(),new Stats(p.getUniqueId(),maxsp));
                plugin.job.playerStatsSync(p,job);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(!playerStats.containsKey(e.getPlayer().getUniqueId())) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                Job job = plugin.job.getUserJob(e.getPlayer());
                int maxsp = job.getJob_skillpoint(plugin.job.getUserLevel(e.getPlayer()));
                playerStats.put(e.getPlayer().getUniqueId(), new Stats(e.getPlayer().getUniqueId(), maxsp));
                plugin.job.playerStatsSync(e.getPlayer(), job);
            }, 10);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            if(args.length == 3){
                if(args[0].equalsIgnoreCase("giveexp")){
                    int exp;
                    try{
                        exp = Integer.parseInt(args[1]);
                    }catch (NumberFormatException e){
                        plugin.getLogger().info("数字じゃないやん");
                        return true;
                    }
                    Player p = Bukkit.getPlayer(args[2]);
                    if(p==null){
                        plugin.getLogger().info("オフラインじゃないの～？");
                        return true;
                    }
                    plugin.job.playerAddEXP(p,exp);
                }
            }
            return true;
        }
        Player p = (Player) sender;
        if(args.length == 0){
            int level = plugin.job.getUserLevel(p);
            Job job = plugin.job.getUserJob(p);
            p.sendMessage("§6§l"+p.getName()+"§rの「"+job.getJob_ViewName()+"」§e§lLv."+plugin.job.getUserLevel(p)+" §f§lステータス");
            p.sendMessage("§aHP(最大値)"+"§f: §a"+p.getHealth()+"§e("+p.getHealthScale()+")");
            p.sendMessage("§e"+job.getJob_spName()+"(最大値)§f: §a"+getPlayerStats(p).getSp()+"§e("+getPlayerStats(p).getMaxsp()+")");
            p.sendMessage("§c攻撃力§f: §c"+job.getAttack(level));
            p.sendMessage("§3防御力§f: §3"+job.getDefense(level));
            p.sendMessage("§a速度§f: §a通常の x"+job.getSpeed(level));
            p.sendMessage("§e"+job.getJob_spName()+"§e回復速度§f: §a"+job.getSphealsecond(level)+"秒に"+job.getSphealvalue(level)+"回復");
            p.sendMessage("§dHP§e回復速度§f: §a"+job.getHphealsecond(level)+"秒に"+job.getHphealvalue(level)+"回復");
        }
        return true;
    }
}
