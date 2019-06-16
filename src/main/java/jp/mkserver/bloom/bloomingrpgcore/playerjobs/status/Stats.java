package jp.mkserver.bloom.bloomingrpgcore.playerjobs.status;

import java.util.UUID;

public class Stats {

    private UUID uuid;

    private int sp;
    private int maxsp;

    public Stats(UUID uuid,int maxsp){
        this.uuid = uuid;
        this.maxsp = sp = maxsp;
    }

    public int getSp(){
        return sp;
    }

    public int getMaxsp(){
        return maxsp;
    }

    public void setMaxsp(int maxsp) {
        this.maxsp = maxsp;
        if(this.sp > maxsp){
            this.sp = maxsp;
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public void addSP(int sp){
        this.sp += sp;
        if(this.sp > maxsp){
            this.sp = maxsp;
        }
    }

    public boolean useSP(int sp){
        int asp = getSp() - sp;
        if(asp < 0){
            return false;
        }
        this.sp -= sp;
        return true;
    }



}
