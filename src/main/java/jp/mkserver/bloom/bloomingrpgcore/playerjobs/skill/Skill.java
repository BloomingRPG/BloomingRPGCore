package jp.mkserver.bloom.bloomingrpgcore.playerjobs.skill;

import jp.mkserver.bloom.bloomingrpgcore.api.CrackShotAPI;
import jp.mkserver.bloom.bloomingrpgcore.playerjobs.jobs.Job;
import jp.mkserver.bloom.bloomingrpgcore.playerjobs.jobs.JobsCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Skill{

    private String skillname;
    private String skill_ViewName;

    private int usepoint;
    private int need_level;

    private String private_sklil;

    private String no_point_message;
    private String private_message;
    private String no_need_level_message;
    private String cooltime_message;

    private String cs_name;

    private JobsCore core;

    private int cooldown;
    
    private JavaPlugin plugin;


    public Skill(JavaPlugin plugin, JobsCore core, String skillname, String skill_ViewName, int cooldown, int need_level, int usepoint, String private_sklil,
                 String cs_name, String no_point_message, String private_message, String no_need_level_message, String cooltime_message){
        this.plugin = plugin;
        this.core = core;
        this.cooldown = cooldown;
        this.skillname = skillname;
        this.skill_ViewName = skill_ViewName;
        this.need_level = need_level;
        this.usepoint = usepoint;
        this.private_sklil = private_sklil;
        this.cs_name = cs_name;
        this.no_point_message = no_point_message;
        this.private_message = private_message;
        this.no_need_level_message = no_need_level_message;
        this.cooltime_message = cooltime_message;
    }

    public String getNo_point_message(){
        return no_point_message;
    }

    public String getCs_name() {
        return cs_name;
    }

    public int getUsepoint() {
        return usepoint;
    }

    public String getSkill_ViewName() {
        return skill_ViewName;
    }

    public String getSkillname() {
        return skillname;
    }

    List<UUID> cooltime = new ArrayList<>();

    public void onSkillUse(Player p){

        Job job = core.plugin.job.getUserJob(p);

        if(cooltime.contains(p.getUniqueId())){
            if(!getCooltime_message().equalsIgnoreCase("none")){
                p.sendMessage(getCooltime_message().replace("<player>",p.getName()).replace("<skillname>",skill_ViewName).replace("<usepoint>",usepoint+"")
                        .replace("<need_level>",need_level+"").replace("<need_job>",job.getJob_ViewName()).replace("<mp_name>",job.getJob_spName()));
            }
            return;
        }

        if(!private_sklil.equalsIgnoreCase("none")){
            if(job==null||!job.getJobname().equalsIgnoreCase(private_sklil)){
                String needjobname = "None";
                String spname = "None";
                Job needjob = core.plugin.job.getJob(private_sklil);
                if(needjob!=null){
                    needjobname = needjob.getJobname();
                    spname = needjob.getJob_spName();
                }
                p.sendMessage(getPrivate_message().replace("<player>",p.getName()).replace("<skillname>",skill_ViewName).replace("<usepoint>",usepoint+"")
                        .replace("<need_level>",need_level+"").replace("<need_job>",needjobname).replace("<mp_name>",spname));
                return;
            }
        }

        if(need_level!=-1){
            if(core.plugin.job.getUserLevel(p)<need_level){
                p.sendMessage(getNo_need_level_message().replace("<player>",p.getName()).replace("<skillname>",skill_ViewName).replace("<usepoint>",usepoint+"")
                        .replace("<need_level>",need_level+"").replace("<need_job>",core.plugin.job.getJob(private_sklil).getJob_ViewName()).replace("<mp_name>",core.plugin.job.getJob(private_sklil).getJob_spName()));
                return;
            }
        }

        if(!core.plugin.stats.playerSPuse(p,usepoint)){
            p.sendMessage(getNo_point_message().replace("<player>",p.getName()).replace("<skillname>",skill_ViewName).replace("<usepoint>",usepoint+"")
                    .replace("<need_level>",need_level+"").replace("<need_job>",job.getJob_ViewName()).replace("<mp_name>",job.getJob_spName()));
        }

        //ここからスキル効果

        //CSの名前がnoneではない場合 銃を無から強制発射する
        if(!getCs_name().equalsIgnoreCase("none")){
            CrackShotAPI.fire(p,getCs_name(),false);
        }

        //


        //ここまでスキル効果

        if(cooldown>0) {
            cooltime.add(p.getUniqueId());
            Bukkit.getScheduler().runTaskLaterAsynchronously(core.plugin, () -> {
                cooltime.remove(p.getUniqueId());
            }, cooldown);
        }
    }

    public String getPrivate_message() {
        return private_message;
    }

    public String getNo_need_level_message() {
        return no_need_level_message;
    }

    public String getCooltime_message() {
        return cooltime_message;
    }
}
