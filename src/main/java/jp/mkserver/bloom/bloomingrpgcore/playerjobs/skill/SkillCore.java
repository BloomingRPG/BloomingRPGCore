package jp.mkserver.bloom.bloomingrpgcore.playerjobs.skill;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import jp.mkserver.bloom.bloomingrpgcore.api.CrackShotAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.io.File;
import java.util.HashMap;

public class SkillCore implements Listener, CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            return true;
        }
        Player p = (Player)sender;
        if(args.length==1){
            if(CrackShotAPI.isNameWeaponContain(args[0])){
                CrackShotAPI.fire(p,args[0],true);
            }
        }
        return true;
    }

    BloomingRPGCore plugin;
    public HashMap<String, Skill> skills = new HashMap<>();

    public SkillCore(BloomingRPGCore plugin) {
        this.plugin = plugin;
        plugin.getCommand("skill").setExecutor(this);
        plugin.getCommand("sk").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
        loadFiles();
        plugin.stats.reloadPlayerStats();
        plugin.job.reloadPlayerStats();
    }

    /*
    /plugins/BloomingRPGCore/skills/スキル名.yml
    PlaceHolder(PH)一覧:
    <player>: プレイヤー名
    <skillname>: スキル名
    <usepoint>: 使用MP
    <need_level>: 必要レベル
    <need_job>: 必要職業名
    <mp_name>: プレイヤーの職業のMP名

    #書き方例

    viewname: '突き上げ切り' #PHのスキル名で出てくるもの
    cooltime: -1 #クールタイム。tick単位。-1は未指定。
    needlevel: -1 #必要レベル。-1は未指定
    usepoint: 3 #使用MP。
    needjob: 'none' #使用に必要な職業。noneで未指定
    cs_name: 'weapon' #スキルのcrackshotファイルのID。
    no_point_msg: '§c<mp_name>が足りません！ 必要<mp_name>: <usepoint>' #MPがない場合のメッセージ PH使用可能
    no_needjob_msg: '§cあなたの職業ではこのスキルを使用できません！ 必要職業: <need_job>' #必要職業ではない場合のメッセージ PH使用可能
    no_needlevel_msg: '§cあなたのレベルではこのスキルを使用できません！ 必要レベル: <need_level>' #必要レベルが足りない場合のメッセージ PH使用可能
    cooltime_msg: '§c現在クールタイム中です！' #クールタイム中のメッセージ noneで未指定 PH使用可能
     */

    public void loadFiles(){
        File folder = new File(plugin.getDataFolder(), File.separator + "skills");
        if(!folder.exists()){
            folder.mkdir();
        }
        for(String s: folder.list()){
            File f = new File(folder, File.separator + s);
            FileConfiguration data = YamlConfiguration.loadConfiguration(f);

            if (f.exists()) {
                String skillname = s.replaceFirst(".yml","").split("_",2)[0];
                Skill skill = new Skill(plugin,plugin.job,skillname,data.getString("viewname"),data.getInt("cooltime"),data.getInt("needlevel"),data.getInt("usepoint")
                ,data.getString("needjob"),data.getString("cs_name","none"),data.getString("no_point_msg"),data.getString("no_needjob_msg"),
                        data.getString("no_needlevel_msg"),data.getString("cooltime_msg"));
                skills.put(skillname, skill);
            }
        }
    }

    public void reloadPlayerStatus(){

    }

    public Skill getSkill(String skillname){
        return skills.get(skillname);
    }


    // Fキー(またはそれに準ずるキー)を押したとき
    @EventHandler
    public void onPlayerItemSwap(PlayerSwapHandItemsEvent e){
        e.setCancelled(true);


    }
}
