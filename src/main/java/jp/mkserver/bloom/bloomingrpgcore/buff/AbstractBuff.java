package jp.mkserver.bloom.bloomingrpgcore.buff;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;

public abstract class AbstractBuff implements Listener {

    private UUID player;
    private String buffid;
    private BuffCore.BuffType type;

    private boolean iscallRun;

    private int runtimesec;
    private int runtimecount;

    private int effecttimesec;

    private BuffCore core;

    public AbstractBuff(BuffCore core, BuffCore.BuffType type, UUID player, String buffid, boolean iscallRun, int runtimesec, int effecttime){
        this.player = player;
        this.core = core;
        this.type = type;
        core.plugin.getServer().getPluginManager().registerEvents(this,core.plugin);
        this.buffid = buffid;
        this.iscallRun = iscallRun;
        this.runtimesec = runtimesec;
        this.effecttimesec = effecttime;
    }

    public final synchronized void timeTick(){
        if(effecttimesec<=-1){
            return;
        }
        effecttimesec--;
        if(effecttimesec==0){
            core.removeBuff(getPlayer(),type);
        }
    }

    private void running(Player p){}

    public final void run(){
        timeTick();
        if(iscallRun){
            runtimecount++;
            if(runtimecount<=runtimesec){
                runtimecount = 0;
                running(getPlayer());
            }
        }
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
}
