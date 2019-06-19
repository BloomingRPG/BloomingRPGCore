package jp.mkserver.bloom.bloomingrpgcore.playerjobs.status;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import jp.mkserver.bloom.bloomingrpgcore.MySQLManagerV2;
import jp.mkserver.bloom.bloomingrpgcore.playerjobs.jobs.Job;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            Stats stats = getPlayerStats(p);
            if(stats==null){
                stats = new Stats(p.getUniqueId(),attack,defense,speed,sp,plugin.job.getUserJob(p).getJob_skillpoint(plugin.job.getUserJobLevel(plugin.job.getUserjobName(p),p)),statspoint);
            }
            stats.setAttack(attack);
            stats.setDefense(defense);
            stats.setSpeed(speed);
            stats.setStats_sp(sp);
            stats.setStatspoint(statspoint);
            if(!isUserStatsDataAlive(p)){
                int statspoints = (plugin.job.getUserLevel(p)-1);
                stats.setStatspoint(statspoints);
                plugin.mysql.execute("INSERT INTO stats (player,uuid,attack,defense,speed,sp,statspoint)  VALUES ('"+p.getName()+"','"+p.getUniqueId().toString()+"',"+attack+","+defense+","+speed+","+sp+","+statspoints+");");
            }else{
                plugin.mysql.execute("UPDATE stats SET attack = "+attack+", defense = "+defense+",speed = "+speed+", sp = "+sp+" , statspoint = "+statspoint+";");
            }
            playerStats.put(p.getUniqueId(),stats);
        });
    }

    public void loadPlayerStats(Player p){
        if(!isUserStatsDataAlive(p)){
            savePlayerStats(p,0,0,0,0,0);
            Bukkit.getScheduler().runTaskLater(plugin,()->{
                Stats stats = new Stats(p.getUniqueId(),0,0,0,0,plugin.job.getUserJob(p).getJob_skillpoint(plugin.job.getUserLevel(p)),0);
                playerStats.put(p.getUniqueId(),stats);
            },10);
            return;
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
                Stats stats = new Stats(p.getUniqueId(),attack,defense,spped,sp,job.getJob_skillpoint(joblevel),statspoint);
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
            }
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
            int need_exp;
            if(level!=100){
                need_exp = plugin.job.exptable_normal.get(level-1) - plugin.job.getUserExp(p);
            }else{
                need_exp = 0;
            }
            Job job = plugin.job.getUserJob(p);
            p.sendMessage("§6§l"+p.getName()+"§e(Lv."+plugin.job.getUserLevel(p)+") §fの「"+job.getJob_ViewName()+" §eLv."+plugin.job.getUserJobLevel(job.getJobname(),p)+"§f」 §f§lステータス");
            p.sendMessage("§e次のレベルまで: "+need_exp+"EXP");
            p.sendMessage("§eステータスポイント: §b"+getStatsPoint(p)+"P");
            BigDecimal bd = new BigDecimal(p.getHealth());
            p.sendMessage("§aHP(最大値)"+"§f: §a"+bd.setScale(1, RoundingMode.HALF_UP)+"§e("+p.getHealthScale()+")");
            p.sendMessage("§e"+job.getJob_spName()+"(最大値)§f: §a"+getPlayerStats(p).getSp()+"§e("+getPlayerStats(p).getMaxsp()+")");
            p.sendMessage("§c攻撃力§f: §c"+getATK(p));
            p.sendMessage("§3防御力§f: §3"+getDEF(p));
            p.sendMessage("§a速度§f: §a通常の x"+(((float)plugin.stats.getSPD(p))+1.0f));
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
            }else if(args[0].equalsIgnoreCase("reset")){
                plugin.job.userDataSave(p,plugin.job.getUserJob(p),1,0);
                savePlayerStats(p,0,0,0,0,0);
                p.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), 50, 0, 0, 0);
                Bukkit.broadcastMessage(plugin.prefix+"§a§l§o"+p.getName()+"§6§l§oはすべてのパワーを神に捧げた…");
                Player finalP = p;
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,()->{
                    Player pp = Bukkit.getPlayer(finalP.getUniqueId());
                    if(pp==null||!pp.isOnline()){
                        return;
                    }
                    pp.setWalkSpeed(plugin.job.getFloatSpeed(0.2f,(float)getSPD(pp)));
                },10);
                return true;
            }else{
                Player send = p;
                p = Bukkit.getPlayer(args[0]);

                if(p==null||!p.isOnline()){
                    send.sendMessage("§cそのプレイヤーはオンラインではありません！");
                    return true;
                }

                int level = plugin.job.getUserLevel(p);
                int need_exp;
                if(level!=100){
                    need_exp = plugin.job.exptable_normal.get(level-1) - plugin.job.getUserExp(p);
                }else{
                    need_exp = 0;
                }
                Job job = plugin.job.getUserJob(p);
                send.sendMessage("§6§l"+p.getName()+"§e(Lv."+plugin.job.getUserLevel(p)+") §fの「"+job.getJob_ViewName()+" §eLv."+plugin.job.getUserJobLevel(job.getJobname(),p)+"§f」 §f§lステータス");
                send.sendMessage("§e次のレベルまで: "+need_exp+"EXP");
                send.sendMessage("§eステータスポイント: §b"+getStatsPoint(p)+"P");
                BigDecimal bd = new BigDecimal(p.getHealth());
                send.sendMessage("§aHP(最大値)"+"§f: §a"+bd.setScale(1, RoundingMode.HALF_UP)+"§e("+p.getHealthScale()+")");
                send.sendMessage("§e"+job.getJob_spName()+"(最大値)§f: §a"+getPlayerStats(p).getSp()+"§e("+getPlayerStats(p).getMaxsp()+")");
                send.sendMessage("§c攻撃力§f: §c"+getATK(p));
                send.sendMessage("§3防御力§f: §3"+getDEF(p));
                send.sendMessage("§a速度§f: §a通常の x"+(((float)plugin.stats.getSPD(p))+1.0f));
                send.sendMessage("§e"+job.getJob_spName()+"§e回復速度§f: §a"+job.getSphealsecond(level)+"秒に"+job.getSphealvalue(level)+"回復");
                send.sendMessage("§dHP§e回復速度§f: §a"+job.getHphealsecond(level)+"秒に"+job.getHphealvalue(level)+"回復");
                return true;
            }
        }

        if(args.length == 3){
            if(args[0].equalsIgnoreCase("powerup")){
                if(args[1].equalsIgnoreCase("ATK")||args[1].equalsIgnoreCase("DEF")||args[1].equalsIgnoreCase("SPD")||args[1].equalsIgnoreCase("SP")){
                    StatsType type = StatsType.fromString(args[1]);
                    if(type==null){
                        p.sendMessage("§cステータスタイプは ATK/DEF/SPD/SP のいずれかを選択してください。");
                        return true;
                    }
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
        p.sendMessage("§e/stats powerup ATK/DEF/SPD/SP 割り振り分 : ポイントを割り振ります");
        return true;
    }

    public double getATK(Player p){
        double stats = getPlayerStats(p).getAttack();
        Job job = plugin.job.getUserJob(p);
        int joblevel = plugin.job.getUserJobLevel(job.getJobname(),p);
        double jobstats = job.getAttack(joblevel);
        BigDecimal bd1= new BigDecimal(stats+"");
        BigDecimal bd2 = new BigDecimal(jobstats+"");
        return bd1.add(bd2).doubleValue();
    }

    public double getDEF(Player p){
        double stats = getPlayerStats(p).getDefense();
        Job job = plugin.job.getUserJob(p);
        int joblevel = plugin.job.getUserJobLevel(job.getJobname(),p);
        double jobstats = job.getDefense(joblevel);
        BigDecimal bd1= new BigDecimal(stats+"");
        BigDecimal bd2 = new BigDecimal(jobstats+"");
        return bd1.add(bd2).doubleValue();
    }

    public double getSPD(Player p){
        double stats = getPlayerStats(p).getSpeed();
        Job job = plugin.job.getUserJob(p);
        int joblevel = plugin.job.getUserJobLevel(job.getJobname(),p);
        double jobstats = job.getSpeed(joblevel);
        BigDecimal bd1= new BigDecimal(stats+"");
        BigDecimal bd2 = new BigDecimal(jobstats+"");
        return bd1.add(bd2).doubleValue();
    }

    public int getStatsPoint(Player p){
        return getPlayerStats(p).getStatspoint();
    }

    enum StatsType{
        ATK,DEF,SPD,SP;

        public static StatsType fromString(String param) {
            String toUpper = param.toUpperCase();
            try {
                return valueOf(toUpper);
            }catch (Exception e){
                return null;
            }
        }
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
            BigDecimal bd1= new BigDecimal(i+"");
            BigDecimal bd2 = new BigDecimal("0.1");
            BigDecimal result2 = bd1.multiply(bd2);
            savePlayerStats(p,stats.getAttack()+result2.doubleValue(),stats.getDefense(), stats.getSpeed(), stats.getStats_sp(),stats.getStatspoint()-i);
            p.sendMessage("§c攻撃力§aを§e"+i+"P分§a強化しました。(+"+result2.doubleValue()+")");
        }else if(type==StatsType.DEF){
            Stats stats = getPlayerStats(p);
            BigDecimal bd1= new BigDecimal(i+"");
            BigDecimal bd2 = new BigDecimal("0.02");
            BigDecimal result2 = bd1.multiply(bd2);
            savePlayerStats(p,stats.getAttack(),stats.getDefense()+result2.doubleValue(), stats.getSpeed(), stats.getStats_sp(),stats.getStatspoint()-i);
            p.sendMessage("§3防御力§aを§e"+i+"P分§a強化しました。(+"+result2.doubleValue()+")");
        }else if(type==StatsType.SPD){
            Stats stats = getPlayerStats(p);
            BigDecimal bd1= new BigDecimal(i+"");
            BigDecimal bd2 = new BigDecimal("0.01");
            BigDecimal result2 = bd1.multiply(bd2);
            savePlayerStats(p,stats.getAttack(),stats.getDefense(), stats.getSpeed()+result2.doubleValue(), stats.getStats_sp(),stats.getStatspoint()-i);
            p.sendMessage("§a速度§aを§e"+i+"P分§a強化しました。(+"+bd1.doubleValue()+"%)");
            p.setWalkSpeed(plugin.job.getFloatSpeed(0.2f,(float)(stats.getSpeed()+result2.doubleValue())));
        }else if(type==StatsType.SP){
            if(i%5!=0){
                p.sendMessage("§cSPのみ、5ポイントごとの使用が必要です！");
                return;
            }
            Stats stats = getPlayerStats(p);
            savePlayerStats(p,stats.getAttack(),stats.getDefense(), stats.getSpeed(), stats.getStats_sp()+(i/5),stats.getStatspoint()-i);
            String spname = plugin.job.getUserJob(p).getJob_spName();
            p.sendMessage("§e"+spname+"§aを§e"+i+"P分§a強化しました。(+"+(i/5)+")");
        }
    }

}
