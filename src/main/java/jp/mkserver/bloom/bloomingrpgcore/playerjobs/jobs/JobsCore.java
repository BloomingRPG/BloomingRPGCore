package jp.mkserver.bloom.bloomingrpgcore.playerjobs.jobs;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import jp.mkserver.bloom.bloomingrpgcore.MySQLManagerV2;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class JobsCore implements Listener {

    public BloomingRPGCore plugin;
    HashMap<String,AbstractJob> jobs = new HashMap<>();

    public JobsCore(BloomingRPGCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
        loadJobs();
    }

    private void loadJobs(){
        jobs.clear();
        SwordMaster swordMaster = new SwordMaster(this);
        jobs.put(swordMaster.getJobname(),swordMaster);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(!isUserDataAlive(e.getPlayer())){
            userDataSave(e.getPlayer(),jobs.get("swordmaster"),1);
        }
    }



    public void userDataSave(Player p,AbstractJob job,int level){
        if(!isUserDataAlive(p)){
            plugin.mysql.execute("INSERT INTO jobs (player,uuid,job,level)  VALUES ('"+p.getName()+"','"+p.getUniqueId().toString()+"','"+job.getJobname()+"',"+level+");");
            return;
        }
        plugin.mysql.execute("UPDATE jobs SET level = "+level+" WHERE uuid = '"+p.getUniqueId().toString()+"';");
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

    public String getUserjobName(Player p){
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

    public AbstractJob getUserJob(Player p){
        String name = getUserjobName(p);
        if(name!=null){
            return jobs.get(name);
        }
        return null;
    }

    public AbstractJob getJob(String str){
        return jobs.get(str);
    }

}
