package jp.mkserver.bloom.bloomingrpgcore.playerjobs.jobs;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import jp.mkserver.bloom.bloomingrpgcore.MySQLManagerV2;
import jp.mkserver.bloom.bloomingrpgcore.playerjobs.StatsViewer;
import jp.mkserver.bloom.bloomingrpgcore.playerjobs.status.Stats;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class JobsCore implements Listener, CommandExecutor {

    public BloomingRPGCore plugin;
    HashMap<String, Job> jobs = new HashMap<>();
    HashMap<UUID, PlayerStats> playerstats = new HashMap<>();

    class PlayerStats{
        UUID uuid;
        int level;
        int exp;
        String jobname;
        int joblevel;
        boolean jobisoverflow;
    }

    public JobsCore(BloomingRPGCore plugin) {
        this.plugin = plugin;
        plugin.getCommand("job").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
        loadJobs();
        loadExpTable();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,()->{
            for(Player p :Bukkit.getOnlinePlayers()){
                Job job = getUserJob(p);
                StatsViewer.showActionBar(p,"§6"+p.getName()+"§e(Lv."+getUserLevel(p)+") §r"+job.getJob_ViewName()+"§e(Lv."+getUserJobLevel(job.getJobname(),p)+"§e) "+job.getJob_spName()+"§e: §a"+plugin.stats.getPlayerStats(p).getSp()+"§f/§b"+plugin.stats.getPlayerStats(p).getMaxsp());
            }
        },0,2);
    }


    public List<Integer> exptable_normal = new ArrayList<>();

    public void loadExpTable(){
        int exp = 100;
        for(int count=1;count<=50;count++){
            exptable_normal.add(exp);
            exp = exp + 30;
        }
    }

    public void levelUpcheck(Player p){
        Job job = getUserJob(p);
        List<Integer> exptable = exptable_normal;
        int level = getUserLevel(p);
        int joblevel = getUserJobLevel(job.getJobname(),p);
        if(level <= 100){
            int exp = getUserExp(p);
            int needexp = exptable.get(level-1);
            if(exp>=needexp){
                if((isUserJobOverflow(job.getJobname(),p)&&joblevel<=100)||(!isUserJobOverflow(job.getJobname(),p)&&joblevel<=50)){
                    p.sendMessage(plugin.prefix+"§e§lJobLevelUP!! §6§l"+joblevel+" => "+(joblevel+1));
                    playerJobDataSave(p,job,joblevel+1,isUserJobOverflow(job.getJobname(),p));
                    Stats stats = plugin.stats.getPlayerStats(p);
                    plugin.stats.savePlayerStats(p,stats.getAttack(),stats.getDefense(), stats.getSpeed(), stats.getStats_sp(),stats.getStatspoint()+5);
                    plugin.stats.getPlayerStats(p).setMaxsp(job.getJob_skillpoint(joblevel+1));
                }
                p.sendMessage(plugin.prefix+"§e§lLevelUP!! §6§l"+level+" => "+(level+1));
                for(Player pp : Bukkit.getOnlinePlayers()){
                    pp.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,1.0f,1.5f);
                }
                p.getWorld().spawnParticle(Particle.TOTEM, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), 50, 0, 0, 0);
                userDataSave(p,job,level+1,exp-needexp);
            }
        }
    }

    /*
    /plugins/BloomingRPGCore/jobs/ジョブ名.yml

    viewname: '§7ソード§6マスター' #PHのジョブ名で出てくるもの
    maxsp: 40 #そのジョブの最大SP
    spname: '闘志' #そのジョブのSPの名前

    attack: 0.0 #攻撃力。ここに書いた分だけ追加でダメージが入る
    defense: 0.0 #防御力。ここに書いた分だけダメージが減る
    speed: 1.0 #速度。1が通常。10が最大で歩くスピードを変えられる。
    addhp: 0 #最大HP追加量。1でハート半分。
    sphealsec: 5 #スキルポイントの回復間隔。秒単位。
    sphealval: 1 #スキルポイントの回復量。
    hphealsec: 0 #HPの回復間隔。秒単位。
    hphealval: 0 #HPの回復量。

    #ここから下のやつは1レベルごとにどんどん変動されます。
    attacklvup: 0.0 #レベルアップごとにどれだけ攻撃力が変動するか
    defenselvup: 0.0 #レベルアップごとにどれだけ防御力が変動するか。
    speedlvup: 0.0 #レベルアップごとにどれだけ速度が変動するか。
    addhplvup: 0 #レベルアップごとにどれだけ最大HPが変動するか。
    splvup: 1 #レベルアップごとにどれだけ最大SP量が変動するか。
    sphealseclvup: 0 #レベルアップごとにどれだけSP回復時間が変動するか。
    sphealvallvup: 0 #レベルアップごとにどれだけSP回復量が変動するか。
    hphealseclvup: 0 #レベルアップごとにどれだけHP回復時間が変動するか。
    hphealvallvup: 0 #レベルアップごとにどれだけHP回復量が変動するか。

    levelup: #レベルアップボーナスを個別で。
      2: #2~100まで対応
       attack: 1.0 #攻撃力。ここに書いた分だけ追加でダメージが入る
       defense: 1.0 #防御力。ここに書いた分だけダメージが減る
       speed: 0.1 #速度。1が通常。10が最大で歩くスピードを変えられる。
       addhp: 2 #最大HP追加量。1でハート半分。
       sphealsec: 1 #スキルポイントの回復間隔。秒単位。
       sphealval: 1 #スキルポイントの回復量。
       hphealsec: 0 #HPの回復間隔。秒単位。
       hphealval: 0 #HPの回復量。
       sp: 5 #どれだけ最大SP量が変動するか。

     */

    private void loadJobs(){
        jobs.clear();

        File folder = new File(plugin.getDataFolder(), File.separator + "jobs");
        if(!folder.exists()){
            folder.mkdir();
        }
        for(String s: folder.list()){
            File f = new File(folder, File.separator + s);
            FileConfiguration data = YamlConfiguration.loadConfiguration(f);

            if (f.exists()) {
                String jobname = s.replaceFirst(".yml","").split("_",2)[0];
                Job job = new Job(this,data,jobname,data.getString("viewname"),data.getInt("maxsp"),data.getString("spname"),
                        data.getDouble("attack"),data.getDouble("defense"),data.getDouble("speed"),data.getDouble("addhp"),
                        data.getInt("sphealsec"),data.getInt("sphealval"),data.getInt("hphealsec"),data.getInt("hphealval"),
                        data.getDouble("attacklvup"),data.getDouble("defenselvup"),data.getDouble("speedlvup"),data.getDouble("addhplvup"),
                        data.getInt("splvup"),data.getInt("sphealseclvup"),data.getInt("sphealvallvup"),data.getInt("hphealseclvup"),data.getInt("hphealval"));
                jobs.put(jobname,job);
            }
        }

        for(Player p : Bukkit.getOnlinePlayers()){
            if(!playerstats.containsKey(p.getUniqueId())){
                Job job = getUserJob(p);
                int exp = getUserExp(p);
                int level = getUserLevel(p);
                PlayerStats stats = new PlayerStats();
                stats.uuid = p.getUniqueId();
                stats.exp = exp;
                stats.jobname = job.getJobname();
                stats.level = level;
                stats.jobisoverflow = isUserJobOverflow(stats.jobname,p);
                stats.joblevel = getUserJobLevel(stats.jobname,p);
                playerstats.put(p.getUniqueId(),stats);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(!isUserDataAlive(e.getPlayer())){
            userDataSave(e.getPlayer(),jobs.get("swordmaster"),1,0);
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,()->{
                if(!isUserJobDataAlive(getUserJob(e.getPlayer()).getJobname(),e.getPlayer())){
                    playerJobDataSave(e.getPlayer(),getUserJob(e.getPlayer()),1,false);
                }
            },10);
        }else{
            if(!isUserJobDataAlive(getUserJob(e.getPlayer()).getJobname(),e.getPlayer())){
                playerJobDataSave(e.getPlayer(),getUserJob(e.getPlayer()),1,false);
                return;
            }
            PlayerStats stats = new PlayerStats();
            stats.uuid = e.getPlayer().getUniqueId();
            stats.exp = getUserExp(e.getPlayer());
            stats.jobname = getUserjobName(e.getPlayer());
            stats.level = getUserLevel(e.getPlayer());
            stats.jobisoverflow = isUserJobOverflow(stats.jobname,e.getPlayer());
            stats.joblevel = getUserJobLevel(stats.jobname,e.getPlayer());
            playerstats.put(e.getPlayer().getUniqueId(),stats);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if (e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
            double damage = e.getDamage();
            double defense = plugin.stats.getDEF(p);
            if(damage <= defense){
                e.setCancelled(true);
            }else{
                e.setDamage(damage-defense);
            }
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e){
        if (e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
            double damage = e.getDamage();
            double attack = plugin.stats.getATK(p);
            e.setDamage(damage+attack);
        }
    }

    public void playerStatsSync(Player p,Job job){
        int level = getUserJobLevel(job.getJobname(),p);

        if(job.getAddhp(level)!=0){
            p.setHealthScale(20+job.getAddhp(level));
        }

        if(plugin.stats.getSPD(p)!=1.0){
            p.setWalkSpeed((float)plugin.stats.getSPD(p));
        }

        if(job.getSphealsecond(level)!=0&&job.getSphealvalue(level)!=0){
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,()->{
                plugin.stats.playerSPheal(p,job.getSphealvalue(level));
            },0,job.getSphealsecond(level)*20);
        }

        if(job.getHphealsecond(level)!=0&&job.getHphealvalue(level)!=0){
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,()->{
                if(p.getHealth()+job.getHphealvalue(level)>p.getHealthScale()){
                    p.setHealth(p.getHealthScale());
                }else{
                    p.setHealth(p.getHealth()+job.getHphealvalue(level));
                }
            },0,job.getHphealsecond(level)*20);
        }
    }




    public void userDataSave(Player p, Job job, int level, int exp){
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
            int levels = level;
            if(level>100){
                levels = 100;
            }
            if(level<1){
                levels = 1;
            }

            int exps = exp;

            if(level==100){
                exps = 0;
            }
            if(!isUserDataAlive(p)){
                plugin.mysql.execute("INSERT INTO jobs (player,uuid,job,level,exp)  VALUES ('"+p.getName()+"','"+p.getUniqueId().toString()+"','"+job.getJobname()+"',"+levels+","+exps+");");
                PlayerStats stats = new PlayerStats();
                stats.uuid = p.getUniqueId();
                stats.exp = exps;
                stats.jobname = job.getJobname();
                stats.level = levels;
                stats.jobisoverflow = isUserJobOverflow(stats.jobname,p);
                stats.joblevel = getUserJobLevel(stats.jobname,p);
                playerstats.put(p.getUniqueId(),stats);
                levelUpcheck(p);
                return;
            }
            plugin.mysql.execute("UPDATE jobs SET job = '"+job.getJobname()+"' , level = "+levels+" , exp = "+exps+" WHERE uuid = '"+p.getUniqueId().toString()+"';");
            PlayerStats stats = new PlayerStats();
            stats.uuid = p.getUniqueId();
            stats.exp = exps;
            stats.jobname = job.getJobname();
            stats.level = levels;
            stats.jobisoverflow = isUserJobOverflow(stats.jobname,p);
            stats.joblevel = getUserJobLevel(stats.jobname,p);
            playerstats.put(p.getUniqueId(),stats);
            levelUpcheck(p);
        });
    }

    public void playerJobDataSave(Player p, Job job, int level, boolean overflow){
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
            int levels = level;
            if(overflow&&level>100){
                levels = 100;
            }

            if(!overflow&&level>50){
                levels = 50;
            }

            if(level<1){
                levels = 1;
            }
            if(!isUserJobDataAlive(job.getJobname(),p)){
                plugin.mysql.execute("INSERT INTO my_jobs (player,uuid,job,level,overflow)  VALUES ('"+p.getName()+"','"+p.getUniqueId().toString()+"','"+job.getJobname()+"',"+levels+","+overflow+");");
                PlayerStats stats = new PlayerStats();
                stats.uuid = p.getUniqueId();
                stats.exp = getUserExp(p);
                stats.jobname = job.getJobname();
                stats.joblevel = levels;
                stats.jobisoverflow = overflow;
                stats.level = getUserLevel(p);
                playerstats.put(p.getUniqueId(),stats);
                levelUpcheck(p);
                return;
            }
            plugin.mysql.execute("UPDATE my_jobs SET level = "+levels+" , overflow = "+overflow+" WHERE uuid = '"+p.getUniqueId().toString()+"' AND job = '"+job.getJobname()+"';");
            PlayerStats stats = new PlayerStats();
            stats.uuid = p.getUniqueId();
            stats.exp = getUserExp(p);
            stats.jobname = job.getJobname();
            stats.joblevel = levels;
            stats.jobisoverflow = overflow;
            stats.level = getUserLevel(p);
            playerstats.put(p.getUniqueId(),stats);
        });
    }

    public void playerAddEXP(Player p,int exp){
        p.sendMessage(plugin.prefix+"§e"+exp+"EXPをゲットした。");
        for(Player pp : Bukkit.getOnlinePlayers()){
            pp.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1.0f,1.5f);
        }
        plugin.job.userDataSave(p,plugin.job.getUserJob(p),plugin.job.getUserLevel(p),plugin.job.getUserExp(p)+exp);
    }

    public boolean isUserJobDataAlive(String jobname,Player p){
        MySQLManagerV2.Query query = plugin.mysql.query("SELECT * FROM my_jobs WHERE uuid = '"+p.getUniqueId().toString()+"' AND job = '"+jobname+"';");
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

    public int getUserJobLevel(String jobname,Player p){

        if(playerstats.containsKey(p.getUniqueId())){
            if(jobname.equalsIgnoreCase(playerstats.get(p.getUniqueId()).jobname)){
                return playerstats.get(p.getUniqueId()).joblevel;
            }
        }

        MySQLManagerV2.Query query = plugin.mysql.query("SELECT * FROM my_jobs WHERE uuid = '"+p.getUniqueId().toString()+"' AND job = '"+jobname+"';");
        ResultSet rs = query.getRs();
        if(rs==null){
            query.close();
            return -1;
        }
        try {
            if(rs.next()){
                int i = rs.getInt("level");
                query.close();
                return i;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        query.close();
        return -1;
    }

    public boolean isUserJobOverflow(String jobname,Player p){

        if(playerstats.containsKey(p.getUniqueId())){
            if(jobname.equalsIgnoreCase(playerstats.get(p.getUniqueId()).jobname)){
                return playerstats.get(p.getUniqueId()).jobisoverflow;
            }
        }

        MySQLManagerV2.Query query = plugin.mysql.query("SELECT * FROM my_jobs WHERE uuid = '"+p.getUniqueId().toString()+"' AND job = '"+jobname+"';");
        ResultSet rs = query.getRs();
        if(rs==null){
            query.close();
            return false;
        }
        try {
            if(rs.next()){
                boolean overflow = rs.getBoolean("overflow");
                query.close();
                return overflow;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        query.close();
        return false;
    }

    public boolean isUserDataAlive(Player p){
        MySQLManagerV2.Query query = plugin.mysql.query("SELECT * FROM jobs WHERE uuid = '"+p.getUniqueId().toString()+"';");
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

    public int getUserLevel(Player p){

        if(playerstats.containsKey(p.getUniqueId())){
            return playerstats.get(p.getUniqueId()).level;
        }

        MySQLManagerV2.Query query = plugin.mysql.query("SELECT * FROM jobs WHERE uuid = '"+p.getUniqueId().toString()+"';");
        ResultSet rs = query.getRs();
        if(rs==null){
            query.close();
            return -1;
        }
        try {
            if(rs.next()){
                int i = rs.getInt("level");
                query.close();
                return i;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        query.close();
        return -1;
    }

    public int getUserExp(Player p){
        if(playerstats.containsKey(p.getUniqueId())){
            return playerstats.get(p.getUniqueId()).exp;
        }

        MySQLManagerV2.Query query = plugin.mysql.query("SELECT * FROM jobs WHERE uuid = '"+p.getUniqueId().toString()+"';");
        ResultSet rs = query.getRs();
        if(rs==null){
            query.close();
            return -1;
        }
        try {
            if(rs.next()){
                int i = rs.getInt("exp");
                query.close();
                return i;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        query.close();
        return -1;
    }

    public String getUserjobName(Player p){
        if(playerstats.containsKey(p.getUniqueId())){
            return playerstats.get(p.getUniqueId()).jobname;
        }

        MySQLManagerV2.Query query = plugin.mysql.query("SELECT * FROM jobs WHERE uuid = '"+p.getUniqueId().toString()+"';");
        ResultSet rs = query.getRs();
        if(rs==null){
            query.close();
            return null;
        }
        try {
            if(rs.next()){
                String name = rs.getString("job");
                query.close();
                return name;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        query.close();
        return null;
    }

    public Job getUserJob(Player p){
        String name = getUserjobName(p);
        if(name!=null){
            return jobs.get(name);
        }
        return null;
    }

    public Job getJob(String str){
        return jobs.get(str);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player p = (Player) sender;

        if(args.length == 0){
            p.sendMessage("§eあなたの職業: §r"+getUserJob(p).getJob_ViewName());
            return true;
        }

        //Jobリスト
        if(args.length == 1){
            if(args[0].equalsIgnoreCase("list")){
                p.sendMessage("§e§lジョブリスト");
                for(String jobid : jobs.keySet()){
                    Job job = jobs.get(jobid);
                    if(!isUserJobDataAlive(jobid,p)){
                        p.sendMessage("§e"+jobid+"§a: §r"+job.getJob_ViewName()+" §c使用したことがありません");
                    }else{
                        p.sendMessage("§e"+jobid+"§a: §r"+job.getJob_ViewName()+" §eLv."+getUserJobLevel(jobid,p));
                    }
                }
                return true;
            }
        }

        //転職
        if(args.length == 2){
            if(args[0].equalsIgnoreCase("change")){
                if(!jobs.containsKey(args[1])){
                    p.sendMessage("§cID「"+args[1]+"」の職業は見つかりませんでした");
                    return true;
                }
                Job job = jobs.get(args[1]);
                if(getUserjobName(p).equalsIgnoreCase(job.getJobname())){
                    p.sendMessage("§c既にあなたは"+job.getJob_ViewName()+"§cです");
                    return true;
                }
                int level = getUserLevel(p);
                int exp = getUserExp(p);
                if(!isUserJobDataAlive(job.getJobname(),p)){
                    playerJobDataSave(p,job,1,false);
                }
                Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
                    userDataSave(p,job,level,exp);
                    plugin.stats.getPlayerStats(p).setMaxsp(job.getJob_skillpoint(getUserJobLevel(job.getJobname(),p)));
                    p.sendMessage("§aあなたは"+job.getJob_ViewName()+"§aに転職しました");
                });
                return true;
            }
        }
        p.sendMessage("§e/job : 自分のジョブを確認します");
        p.sendMessage("§e/job list : ジョブリストを確認します");
        p.sendMessage("§e/job change [ジョブID]: 別のジョブに転職します");
        return true;
    }
}
