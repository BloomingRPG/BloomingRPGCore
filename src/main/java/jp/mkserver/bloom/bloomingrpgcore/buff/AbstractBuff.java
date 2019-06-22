package jp.mkserver.bloom.bloomingrpgcore.buff;

import jp.mkserver.bloom.bloomingrpgcore.buff.BuffCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.UUID;

public abstract class AbstractBuff implements Listener {

    private UUID player;
    private String buffid;
    private String viewname;
    private BuffCore.BuffType type;

    private boolean iscallRun;

    private int runtimesec;
    private int runtimecount;

    private int effecttimesec;

    private BuffCore core;

    private int taskid = -1;

    public AbstractBuff(BuffCore core, BuffCore.BuffType type, UUID player,String viewname, String buffid, boolean iscallRun, int runtimesec, int effecttime){
        this.player = player;
        this.core = core;
        this.type = type;
        core.plugin.getServer().getPluginManager().registerEvents(this,core.plugin);
        this.buffid = buffid;
        this.iscallRun = iscallRun;
        this.viewname = viewname;
        this.runtimesec = runtimesec;
        this.effecttimesec = effecttime;
        if(isIscallRun()){
           taskid = Bukkit.getScheduler().runTaskTimerAsynchronously(core.plugin, this::run,0,20).getTaskId();
        }
    }

    public final void endTask(){
        Bukkit.getScheduler().cancelTask(taskid);
        HandlerList.unregisterAll(this);
    }

    public final void unRegister(){
        getCore().removeBuff(player, type);
    }

    public final synchronized boolean timeTick(){
        if(effecttimesec<=-1){
            return false;
        }
        effecttimesec--;
        if(effecttimesec==0){
            core.removeBuff(getPlayer(),type);
            return true;
        }
        return false;
    }

    private void running(){}

    public final void run(){
        if(timeTick()){
            return;
        }
        if(iscallRun){
            runtimecount++;
            if(runtimecount<=runtimesec){
                runtimecount = 0;
                running();
            }
        }
    }

    private int getEffecttimesec(){
        return effecttimesec;
    }

    public String getEffectTime(){
        String result = "";
        int sec = getEffecttimesec();
        int min, hour;

        hour = sec / 3600;
        min = (sec%3600) / 60;
        sec = sec % 60;

        if(hour>=1){
            return result + hour +"時間";
        }

        if(min>=1){
            return result + min +"分";
        }

        if(sec>=1){
            return result + sec +"秒";
        }
        return "0秒";
    }

    public String getBuffid() {
        return buffid;
    }

    public boolean isIscallRun() {
        return iscallRun;
    }

    public Player getPlayer(){
        return Bukkit.getPlayer(player);
    }

    public BuffCore getCore() {
        return core;
    }

    public String getViewname() {
        return viewname;
    }
}
