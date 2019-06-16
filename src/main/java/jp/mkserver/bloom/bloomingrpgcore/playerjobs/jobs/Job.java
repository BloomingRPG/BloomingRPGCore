package jp.mkserver.bloom.bloomingrpgcore.playerjobs.jobs;

public class Job {

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

    public Job(JobsCore core, String jobname, String job_ViewName, int job_maxsp, String job_spName,
               double attack,double defense,double speed,double addhp,int sphealsecond,int sphealvalue,
               int hphealsecond,int hphealvalue,   double attacklvup,double defenselvup,double speedlvup,
               double addhplvup,int maxsplvup,int sphealsecondlvup,int sphealvaluelvup,
               int hphealsecondlvup,int hphealvaluelvup){
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


    public String getJobname() {
        return jobname;
    }

    public String getJob_ViewName() {
        return job_ViewName;
    }

    public int getJob_skillpoint(int level) {
        return job_maxsp + (maxsplvup * (level-1));
    }

    public String getJob_spName() {
        return job_spName;
    }

    public double getAttack(int level) {
        level = level-1;
        return attack + (attacklvup*(level-1));
    }

    public double getDefense(int level) {
        level = level-1;
        return defense + (defenselvup*(level-1));
    }

    public double getSpeed(int level) {
        level = level-1;
        return speed + (speedlvup*(level-1));
    }

    public double getAddhp(int level) {
        level = level-1;
        return addhp + (addhplvup*(level-1));
    }

    public int getSphealsecond(int level) {
        level = level-1;
        return sphealsecond + (sphealsecondlvup*(level-1));
    }

    public int getSphealvalue(int level) {
        level = level-1;
        return sphealvalue + (sphealvaluelvup*(level-1));
    }

    public int getHphealsecond(int level) {
        level = level-1;
        return hphealsecond + (hphealsecondlvup*(level-1));
    }

    public int getHphealvalue(int level) {
        level = level-1;
        return hphealvalue + (hphealvaluelvup*(level-1));
    }
}
