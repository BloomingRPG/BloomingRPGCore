package jp.mkserver.bloom.bloomingrpgcore.playerjobs.jobs;

import org.bukkit.configuration.file.FileConfiguration;

public class Job {

    private FileConfiguration file;

    private String jobname;
    private String job_ViewName;

    private int job_maxsp;
    private String job_spName;

    private double attack;
    private double defense;
    private double speed;
    private double addhp;
    private int sphealsecond;
    private int sphealvalue;
    private int hphealsecond;
    private int hphealvalue;

    private double attacklvup;
    private double defenselvup;
    private double speedlvup;
    private double addhplvup;
    private int maxsplvup;
    private int sphealsecondlvup;
    private int sphealvaluelvup;
    private int hphealsecondlvup;
    private int hphealvaluelvup;


    private JobsCore core;

    public Job(JobsCore core, FileConfiguration file, String jobname, String job_ViewName, int job_maxsp, String job_spName,
               double attack, double defense, double speed, double addhp, int sphealsecond, int sphealvalue,
               int hphealsecond, int hphealvalue, double attacklvup, double defenselvup, double speedlvup,
               double addhplvup, int maxsplvup, int sphealsecondlvup, int sphealvaluelvup,
               int hphealsecondlvup, int hphealvaluelvup){
        this.file = file;
        this.core = core;
        this.jobname = jobname;
        this.job_ViewName = job_ViewName;
        this.job_maxsp = job_maxsp;
        this.job_spName = job_spName;

        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.addhp = addhp;
        this.sphealsecond = sphealsecond;
        this.sphealvalue = sphealvalue;
        this.hphealsecond = hphealsecond;
        this.hphealvalue = hphealvalue;

        this.attacklvup = attacklvup;
        this.defenselvup = defenselvup;
        this.speedlvup = speedlvup;
        this.addhplvup = addhplvup;
        this.maxsplvup = maxsplvup;
        this.sphealsecondlvup = sphealsecondlvup;
        this.sphealvaluelvup = sphealvaluelvup;
        this.hphealsecondlvup = hphealsecondlvup;
        this.hphealvaluelvup = hphealvaluelvup;
    }


    public double getAttackPlus(int level){
        double stats = (attacklvup*(level-1));
        for(int i = 2;i<=level;i++){
            if(file.contains("levelup."+i+".attack")){
                stats = stats + file.getDouble("levelup."+i+".attack");
            }
        }
        return stats;
    }

    public double getDefensePlus(int level){
        double stats = (defenselvup*(level-1));
        for(int i = 2;i<=level;i++){
            if(file.contains("levelup."+i+".defense")){
                stats = stats + file.getDouble("levelup."+i+".defense");
            }
        }
        return stats;
    }

    public double getSpeedPlus(int level){
        double stats = (speedlvup*(level-1));
        for(int i = 2;i<=level;i++){
            if(file.contains("levelup."+i+".speed")){
                stats = stats + file.getDouble("levelup."+i+".speed");
            }
        }
        return stats;
    }

    public double getAddHPPlus(int level){
        double stats = (addhplvup*(level-1));
        for(int i = 2;i<=level;i++){
            if(file.contains("levelup."+i+".addhp")){
                stats = stats + file.getDouble("levelup."+i+".addhp");
            }
        }
        return stats;
    }

    public int getSPhealsecPlus(int level){
        int stats = (sphealsecondlvup*(level-1));
        for(int i = 2;i<=level;i++){
            if(file.contains("levelup."+i+".sphealsec")){
                stats = stats + file.getInt("levelup."+i+".sphealsec");
            }
        }
        return stats;
    }

    public int getSPhealsvalPlus(int level){
        int stats = (sphealvaluelvup*(level-1));
        for(int i = 2;i<=level;i++){
            if(file.contains("levelup."+i+".sphealval")){
                stats = stats + file.getInt("levelup."+i+".sphealval");
            }
        }
        return stats;
    }

    public int getHPhealsecPlus(int level){
        int stats = (hphealsecondlvup*(level-1));
        for(int i = 2;i<=level;i++){
            if(file.contains("levelup."+i+".hphealsec")){
                stats = stats + file.getInt("levelup."+i+".hphealsec");
            }
        }
        return stats;
    }

    public int getHPhealsvalPlus(int level){
        int stats = (hphealvaluelvup*(level-1));
        for(int i = 2;i<=level;i++){
            if(file.contains("levelup."+i+".hphealval")){
                stats = stats + file.getInt("levelup."+i+".hphealval");
            }
        }
        return stats;
    }

    public int getMaxSPPlus(int level){
        int stats = (maxsplvup*(level-1));
        for(int i = 2;i<=level;i++){
            if(file.contains("levelup."+i+".sp")){
                stats = stats + file.getInt("levelup."+i+".sp");
            }
        }
        return stats;
    }



    public String getJobname() {
        return jobname;
    }

    public String getJob_ViewName() {
        return job_ViewName;
    }

    public String getJob_spName() {
        return job_spName;
    }

    public int getJob_skillpoint(int level) {
        return job_maxsp + getMaxSPPlus(level);
    }

    public double getAttack(int level) {
        level = level-1;
        return attack + getAttackPlus(level);
    }

    public double getDefense(int level) {
        level = level-1;
        return defense + getDefensePlus(level);
    }

    public double getSpeed(int level) {
        level = level-1;
        return speed + getSpeedPlus(level);
    }

    public double getAddhp(int level) {
        level = level-1;
        return addhp + getAddHPPlus(level);
    }

    public int getSphealsecond(int level) {
        level = level-1;
        return sphealsecond + getSPhealsecPlus(level);
    }

    public int getSphealvalue(int level) {
        level = level-1;
        return sphealvalue + getSPhealsvalPlus(level);
    }

    public int getHphealsecond(int level) {
        level = level-1;
        return hphealsecond + getHPhealsecPlus(level);
    }

    public int getHphealvalue(int level) {
        level = level-1;
        return hphealvalue + getHPhealsvalPlus(level);
    }
}
