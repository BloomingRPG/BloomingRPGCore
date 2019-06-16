package jp.mkserver.bloom.bloomingrpgcore.playerjobs.jobs;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractJob {

    private String jobname;
    private String job_ViewName;

    private int job_maxsp;
    private String job_spName;

    private JobsCore core;

    public AbstractJob(JobsCore core, String jobname, String job_ViewName, int job_maxsp, String job_spName){
        this.core = core;
        this.jobname = jobname;
        this.job_ViewName = job_ViewName;
        this.job_maxsp = job_maxsp;
        this.job_spName = job_spName;
    }


    public String getJobname() {
        return jobname;
    }

    public String getJob_ViewName() {
        return job_ViewName;
    }

    public int getJob_skillpoint() {
        return job_maxsp;
    }

    public String getJob_spName() {
        return job_spName;
    }
}
