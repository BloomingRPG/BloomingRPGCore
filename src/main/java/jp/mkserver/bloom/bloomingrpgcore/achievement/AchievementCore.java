package jp.mkserver.bloom.bloomingrpgcore.achievement;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import jp.mkserver.bloom.bloomingrpgcore.MySQLManagerV2;
import jp.mkserver.bloom.bloomingrpgcore.achievement.data.FirstLogin;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AchievementCore {

    BloomingRPGCore plugin;
    List<AbstractAchieve> achieves = new ArrayList<>();

    public AchievementCore(BloomingRPGCore plugin){
        this.plugin = plugin;
        loadAchievements();
    }

    //アチーブメントを作成する
    public void loadAchievements(){
        achieves.add(new FirstLogin());
    }



    public void openAchievement(Player p,int id){
        if(isOpenAchievement(p,id)){
            return;
        }
        plugin.mysql.execute("INSERT INTO achievement (uuid,player,get_reward,achievement_id) VALUES ('"+p.getUniqueId().toString()+"','"+p.getName()+"',"+false+","+id+");");
    }

    public boolean isRewardGet(Player p,int id){
        MySQLManagerV2.Query query = plugin.mysql.query("SELECT * FROM achievement WHERE uuid = '"+p.getUniqueId().toString()+"' AND achievement_id = "+id+";");
        ResultSet rs = query.getRs();
        if(rs==null){
            query.close();
            return false;
        }
        try {
            if(rs.next()){
                boolean result = rs.getBoolean("get_reward");
                query.close();
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        query.close();
        return false;
    }

    public boolean isOpenAchievement(Player p,int id){
        MySQLManagerV2.Query query = plugin.mysql.query("SELECT * FROM achievement WHERE uuid = '"+p.getUniqueId().toString()+"' AND achievement_id = "+id+";");
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

}
