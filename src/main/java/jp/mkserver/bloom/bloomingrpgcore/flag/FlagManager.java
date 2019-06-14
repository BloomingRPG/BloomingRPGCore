package jp.mkserver.bloom.bloomingrpgcore.flag;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import jp.mkserver.bloom.bloomingrpgcore.MySQLManagerV2;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FlagManager {

    BloomingRPGCore plugin;

    public FlagManager(BloomingRPGCore plugin){
        this.plugin = plugin;
    }

    public boolean isPlayerHasFlag(Player p, String name){
        MySQLManagerV2.Query query = plugin.mysql.query("SELECT * FROM flagdata WHERE uuid = '"+p.getUniqueId().toString()+"' AND flag = '"+name+"';");
        ResultSet rs = query.getRs();
        if(rs==null){
            query.close();
            return false;
        }
        try {
            if(rs.next()){
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        query.close();
        return false;
    }

    public void addPlayerFlag(Player p, String name){
        if(isPlayerHasFlag(p,name)){
            return;
        }
        plugin.mysql.execute("INSERT INTO flagdata (player,uuid,flag)  VALUES ('"+p.getName()+"','"+p.getUniqueId().toString()+"','"+name+"');");
    }

    public void removePlayerFlag(Player p, String name){
        plugin.mysql.execute("DELETE FROM flagdata WHERE uuid = '" + p.getUniqueId().toString() + "' AND flag = '"+name+"';");
    }
}
