package jp.mkserver.bloom.bloomingrpgcore.extra;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import jp.mkserver.bloom.bloomingrpgcore.MySQLManagerV2;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class LoginBonus implements Listener {

    private BloomingRPGCore plugin;
    private static final String loginMessage = "§e<player> さんがBloomRPGに参加しました"; //通常ログインメッセージ。<player>はプレイヤー名に置き換え
    private static final String firstloginMessage = "§d<player> さんがBloomRPGの世界に降り立ちました"; //初回ログインメッセージ。<player>はプレイヤー名に置き換え

    public LoginBonus(BloomingRPGCore plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }

    //そのログインは 前回から24時間以上経過している/初めて入った/前回から24時間経過していない
    enum LoginType{
        getbonuslogin,firstlogin,defaultlogin
    }

    //そのログインタイプは？
    private LoginType checkLoginType(Player p){
        if(!checkPlayerLogContain(p)){
            return LoginType.firstlogin;
        }
        MySQLManagerV2.Query query = plugin.mysql.query("SELECT * from login_log WHERE uuid = '" + p.getUniqueId().toString() + "';");
        ResultSet rs = query.getRs();
        if(rs==null){
            query.close();
            return LoginType.firstlogin;
        }
        try {
            if(rs.next()){
                Date date = rs.getDate("bonus_date");
                long dlo = date.getTime();
                long nowlo = new Date().getTime();
                long dayDiff = ( nowlo - dlo  ) / (1000 * 60 * 60 * 24 );
                if(dayDiff>=1){
                    query.close();
                    return LoginType.getbonuslogin;
                }else{
                    query.close();
                    return LoginType.defaultlogin;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        query.close();
        return LoginType.firstlogin;
    }

    //そのプレイヤーのデータがログインログに存在する？
    private boolean checkPlayerLogContain(Player p){
        MySQLManagerV2.Query query = plugin.mysql.query("SELECT * from login_log WHERE uuid = '" + p.getUniqueId().toString() + "';");
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

    //DataBaseの情報をアップデート(作成)！
    private void updatePlayerLog(Player p,boolean isbonus){
        if(!checkPlayerLogContain(p)){
            createPlayerLog(p);
            return;
        }
        if(isbonus){
            plugin.mysql.execute("UPDATE login_log SET bonus_date = CURRENT_TIMESTAMP , last_date = CURRENT_TIMESTAMP WHERE uuid = '"+p.getUniqueId().toString()+"';");
        }else{
            plugin.mysql.execute("UPDATE login_log SET last_date = CURRENT_TIMESTAMP WHERE uuid = '"+p.getUniqueId().toString()+"';");
        }
    }

    //DataBaseに情報をinsert(作成)！
    private void createPlayerLog(Player p){
        plugin.mysql.execute("INSERT INTO login_log (player,uuid)  VALUES ('"+p.getName()+"','"+p.getUniqueId().toString()+"');");
    }

    //ログインボーナスで実行されるメソッド
    private void giveLoginBonus(Player p){
        p.sendMessage(plugin.config.getString("loginmsg"));
        for(String str:plugin.config.getStringList("loginbonus")){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),str.replace("<player>",p.getName()));
        }
    }

    //初回ログインボーナスで実行されるメソッド
    private void firstLoginBonus(Player p){
        p.sendMessage(plugin.config.getString("firstloginmsg"));
        for(String str:plugin.config.getStringList("firstloginbonus")){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),str.replace("<player>",p.getName()));
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        LoginType loginType = checkLoginType(e.getPlayer());
        if(loginType==LoginType.getbonuslogin){
            e.setJoinMessage(loginMessage.replace("<player>",e.getPlayer().getName()));
            updatePlayerLog(e.getPlayer(),true);
            giveLoginBonus(e.getPlayer());
        }else if(loginType==LoginType.firstlogin){
            e.setJoinMessage(firstloginMessage.replace("<player>",e.getPlayer().getName()));
            updatePlayerLog(e.getPlayer(),true);
            firstLoginBonus(e.getPlayer());
        }else{
            e.setJoinMessage(loginMessage.replace("<player>",e.getPlayer().getName()));
            updatePlayerLog(e.getPlayer(),false);
        }
    }

}
