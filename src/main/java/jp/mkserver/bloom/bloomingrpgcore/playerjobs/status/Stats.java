package jp.mkserver.bloom.bloomingrpgcore.playerjobs.status;

import java.util.UUID;

public class Stats {

    private UUID uuid;

    private int sp;

    private double attack;
    private double defense;
    private double speed;
    private int maxsp;
    private int stats_sp;
    private int statspoint;

    public Stats(UUID uuid,double attack,double defense,double speed,int stats_sp,int maxsp,int statspoint){
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.uuid = uuid;
        this.stats_sp = stats_sp;
        this.maxsp = maxsp;
        this.statspoint = statspoint;
        this.sp = getMaxsp();
    }

    public int getSp(){
        return sp;
    }

    public int getMaxsp(){
        return maxsp+stats_sp;
    }

    public void setMaxsp(int maxsp) {
        this.maxsp = maxsp;
        if(this.sp > getMaxsp()){
            this.sp = getMaxsp();
        }
    }

    public void setStats_sp(int stats_sp){
        this.stats_sp = stats_sp;
    }

    public int getStats_sp() {
        return stats_sp;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void addSP(int sp){
        this.sp += sp;
        if(this.sp > getMaxsp()){
            this.sp = getMaxsp();
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

    public double getAttack() {
        return attack;
    }

    public void setAttack(double attack) {
        this.attack = attack;
    }

    public double getDefense() {
        return defense;
    }

    public void setDefense(double defense) {
        this.defense = defense;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getStatspoint() {
        return statspoint;
    }

    public void setStatspoint(int statspoint) {
        this.statspoint = statspoint;
    }
}
