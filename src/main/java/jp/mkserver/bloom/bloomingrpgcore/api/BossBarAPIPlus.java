package jp.mkserver.bloom.bloomingrpgcore.api;

import jp.mkserver.bloom.bloomingrpgcore.buff.BuffCore;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BossBarAPIPlus {

    private String name;

    private BossBar bar;

    private BuffCore plugin;

    /*
    BarStyle.SEGMENTED_20 : 20分割
    BarStyle.SEGMENTED_12 : 12分割
    BarStyle.SEGMENTED_10 : 10分割
    BarStyle.SEGMENTED_6 : 6分割
    BarStyle.SOLID : 分割なし
     */

    public BossBarAPIPlus(BuffCore plugin,String name,BarColor color,BarStyle style){
        this.plugin = plugin;
        this.name = name;
        bar = Bukkit.createBossBar(name,color,style);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        bar.setTitle(name);
    }

    public void setColor(BarColor color){
        bar.setColor(color);
    }

    public void setStyle(BarStyle style){
        bar.setStyle(style);
    }

    public void setProgress(double d){
        bar.setProgress(d);
    }

    public double getProgress(){
        return bar.getProgress();
    }

    public void setVisible(boolean b){
        bar.setVisible(b);
    }

    public void showPlayer(Player p){
        bar.addPlayer(p);
    }

    public void unVisiblePlayer(Player p){
        bar.removePlayer(p);
    }

    class MovingTitle{
        List<String> showlist;
        int staytime;

        int taskid = -1;

        public MovingTitle(List<String> showlist,int staytime){
            this.showlist = showlist;
            this.staytime = staytime*4;
        }

        public void moveStart(BossBar bar){
            AtomicInteger i = new AtomicInteger(staytime);
            AtomicInteger ii = new AtomicInteger(0);
            AtomicBoolean isMoving = new AtomicBoolean(false);
            taskid = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin.plugin,()->{
                if(isMoving.get()){
                    if(showlist.size()==1){
                        isMoving.set(false);
                        bar.setTitle(showlist.get(0));
                        return;
                    }
                    ii.getAndIncrement();
                    if(ii.get()>=showlist.size()){
                        ii.set(0);
                    }
                    bar.setTitle(showlist.get(ii.get()));
                    isMoving.set(false);
                }
                i.getAndIncrement();
                if(i.get()>=staytime){
                    i.set(0);
                    isMoving.set(true);
                }
            },0,5).getTaskId();
        }

        public void moveStop(){
            if(taskid!=-1){
                Bukkit.getScheduler().cancelTask(taskid);
            }
        }
    }


    MovingTitle movingTitle;

    public void createMovingTitle(List<String> title,int speedtick){
       movingTitle = new MovingTitle(title,speedtick);
       movingTitle.moveStart(bar);
    }

    public void stopMovingTitle(){
        movingTitle.moveStop();
        bar.setTitle(name);
    }
}
