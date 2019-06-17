package jp.mkserver.bloom.bloomingrpgcore.playerjobs.status;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import jp.mkserver.bloom.bloomingrpgcore.MySQLManagerV2;
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

import java.sql.ResultSet;
import java.sql.SQLException;
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
    }

    public void savePlayerStats(Player p,double attack,double defense,double speed,int sp,int statspoint){
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
            if(!isUserStatsDataAlive(p)){
                int statspoints = (plugin.job.getUserLevel(p)-1)*5;
                plugin.mysql.execute("INSERT INTO stats (player,uuid,attack,defense,speed,sp,statspoint)  VALUES ('"+p.getName()+"','"+p.getUniqueId().toString()+"',"+attack+","+defense+","+speed+","+sp+","+statspoints+");");
            }else{
                plugin.mysql.execute("UPDATE stats SET attack = "+attack+", defense = "+defense+",speed = "+speed+", sp = "+sp+" , statspoint = "+statspoint+";");
            }
            Stats stats = getPlayerStats(p);
            stats.setAttack(attack);
            stats.setDefense(defense);
            stats.setSpeed(speed);
            stats.setMaxsp(sp);
            stats.setStatspoint(statspoint);
            playerStats.put(p.getUniqueId(),stats);
        });
    }

    public void loadPlayerStats(Player p){
        if(!isUserStatsDataAlive(p)){
            savePlayerStats(p,0,0,0,0,0);
        }
        MySQLManagerV2.Query query = plugin.mysql.query("SELECT * FROM stats WHERE uuid = '"+p.getUniqueId().toString()+"';");
        ResultSet rs = query.getRs();
        if(rs==null){
            query.close();
            return;
        }
        try {
            if(rs.next()){
                double attack = rs.getDouble("attack");
                double defense = rs.getDouble("defense");
                double spped = rs.getDouble("speed");
                int sp = rs.getInt("sp");
                int statspoint = rs.getInt("statspoint");
                Job job = plugin.job.getUserJob(p);
                int joblevel = plugin.job.getUserJobLevel(job.getJobname(),p);
                Stats stats = new Stats(p.getUniqueId(),attack,defense,spped,job.getJob_skillpoint(joblevel),sp,statspoint);
                playerStats.put(p.getUniqueId(),stats);
                query.close();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        query.close();
    }

    public boolean isUserStatsDataAlive(Player p){
        MySQLManagerV2.Query query = plugin.mysql.query("SELECT * FROM stats WHERE uuid = '"+p.getUniqueId().toString()+"';");
        ResultSet rs = query.getRs();
        if(rs==null){
            query.close();
            return false;
        }
        try {
            if(rs.next()){
                query.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        query.close();
        return false;
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
                loadPlayerStats(p);
                Job job = plugin.job.getUserJob(p);
                plugin.job.playerStatsSync(p,job);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(!playerStats.containsKey(e.getPlayer().getUniqueId())) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                loadPlayerStats(e.getPlayer());
                Job job = plugin.job.getUserJob(e.getPlayer());
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
            int need_exp = plugin.job.exptable_normal.get(level-1) - plugin.job.getUserExp(p);
            Job job = plugin.job.getUserJob(p);
            p.sendMessage("§6§l"+p.getName()+"§e(Lv."+plugin.job.getUserLevel(p)+") §fの「"+job.getJob_ViewName()+" §eLv."+plugin.job.getUserJobLevel(job.getJobname(),p)+"§f」 §f§lステータス");
            p.sendMessage("§e次のレベルまで: "+need_exp+"EXP");
            p.sendMessage("§eステータスポイント: §b"+getStatsPoint(p)+"P");
            p.sendMessage("§aHP(最大値)"+"§f: §a"+p.getHealth()+"§e("+p.getHealthScale()+")");
            p.sendMessage("§e"+job.getJob_spName()+"(最大値)§f: §a"+getPlayerStats(p).getSp()+"§e("+getPlayerStats(p).getMaxsp()+")");
            p.sendMessage("§c攻撃力§f: §c"+getATK(p));
            p.sendMessage("§3防御力§f: §3"+getDEF(p));
            p.sendMessage("§a速度§f: §a通常の x"+getSPD(p));
            p.sendMessage("§e"+job.getJob_spName()+"§e回復速度§f: §a"+job.getSphealsecond(level)+"秒に"+job.getSphealvalue(level)+"回復");
            p.sendMessage("§dHP§e回復速度§f: §a"+job.getHphealsecond(level)+"秒に"+job.getHphealvalue(level)+"回復");
            return true;
        }

        if(args.length == 1) {
            if (args[0].equalsIgnoreCase("powerup")) {
                Job job = plugin.job.getUserJob(p);
                p.sendMessage("§e/stats powerup atk/def/spd/sp 割り振り分 : ポイントを割り振ります");
                p.sendMessage("§cATK: 攻撃力 1Pにつき0.2上昇します。");
                p.sendMessage("§3DEF: 防御力 1Pにつき0.2上昇します。");
                p.sendMessage("§aSPD: 速度 1Pにつき5%上昇します。");
                p.sendMessage("§6SP: §e"+job.getJob_spName()+" 1Pにつき最大値が1上昇します。");
                return true;
            }
        }

        if(args.length == 3){
            if(args[0].equalsIgnoreCase("powerup")){
                if(args[1].equals("ATK")||args[1].equals("DEF")||args[1].equals("SPD")||args[1].equals("SP")){
                    StatsType type = StatsType.valueOf(args[1]);
                    int i;
                    try{
                        i = Integer.parseInt(args[2]);
                        useStatsPoint(p,type,i);
                    }catch (NumberFormatException e){
                        p.sendMessage("§cポイント指定は数字で入力して下さい。");
                    }
                    return true;
                }else{
                    p.sendMessage("§cステータスタイプは ATK/DEF/SPD/SP のいずれかを選択してください。");
                    return true;
                }
            }
        }

        p.sendMessage("§e/stats : 自分のステータスを確認します。");
        p.sendMessage("§e/stats powerup : ステータス割り振りのヘルプを見ます。");
        p.sendMessage("§e/stats powerup atk/def/spd/sp 割り振り分 : ポイントを割り振ります");
        return true;
    }

    public double getATK(Player p){
        double stats = getPlayerStats(p).getAttack();
        Job job = plugin.job.getUserJob(p);
        int joblevel = plugin.job.getUserJobLevel(job.getJobname(),p);
        double jobstats = job.getAttack(joblevel);
        return stats+jobstats;
    }

    public double getDEF(Player p){
        double stats = getPlayerStats(p).getDefense();
        Job job = plugin.job.getUserJob(p);
        int joblevel = plugin.job.getUserJobLevel(job.getJobname(),p);
        double jobstats = job.getDefense(joblevel);
        return stats+jobstats;
    }

    public double getSPD(Player p){
        double stats = getPlayerStats(p).getSpeed();
        Job job = plugin.job.getUserJob(p);
        int joblevel = plugin.job.getUserJobLevel(job.getJobname(),p);
        double jobstats = job.getSpeed(joblevel);
        return stats+jobstats;
    }

    public int getStatsPoint(Player p){
        return getPlayerStats(p).getStatspoint();
    }

    enum StatsType{
        ATK,DEF,SPD,SP
    }

    public void useStatsPoint(Player p,StatsType type,int i){
        if(i<1){
            p.sendMessage("§c1以上で指定してください！");
            return;
        }
        if(i>getStatsPoint(p)){
            p.sendMessage("§cステータスポイントが足りません！ 必要: "+i+"P");
            return;
        }
        if(type==StatsType.ATK){
            Stats stats = getPlayerStats(p);
            savePlayerStats(p,stats.getAttack()+(i*0.2),stats.getDefense(), stats.getSpeed(), stats.getStats_sp(),stats.getStatspoint()-i);
            p.sendMessage("§c攻撃力§aを§e"+i+"分§a強化しました。");
        }else if(type==StatsType.DEF){
            Stats stats = getPlayerStats(p);
            savePlayerStats(p,stats.getAttack(),stats.getDefense()+(i*0.2), stats.getSpeed(), stats.getStats_sp(),stats.getStatspoint()-i);
            p.sendMessage("§3防御力§aを§e"+i+"P分§a強化しました。");
        }else if(type==StatsType.SPD){
            Stats stats = getPlayerStats(p);
            savePlayerStats(p,stats.getAttack(),stats.getDefense(), stats.getSpeed()+(i*0.05), stats.getStats_sp(),stats.getStatspoint()-i);
            p.sendMessage("§a速度§aを§e"+i+"P分§a強化しました。");
        }else if(type==StatsType.SP){
            Stats stats = getPlayerStats(p);
            savePlayerStats(p,stats.getAttack(),stats.getDefense(), stats.getSpeed(), stats.getStats_sp(),stats.getStatspoint()-i);
            String spname = plugin.job.getUserJob(p).getJob_spName();
            p.sendMessage("§r"+spname+"§aを§e"+i+"P分§a強化しました。");
        }
    }

}
