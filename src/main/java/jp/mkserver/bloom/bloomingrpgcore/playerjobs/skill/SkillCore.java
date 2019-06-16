package jp.mkserver.bloom.bloomingrpgcore.playerjobs.skill;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;

public class SkillCore {

    BloomingRPGCore plugin;
    public HashMap<String, SkillData> skills = new HashMap<>();

    public SkillCore(BloomingRPGCore plugin) {
        this.plugin = plugin;
        loadFiles();
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
    needlevel: -1 #必要レベル。-1は未指定
    usepoint: 3 #使用MP。
    needjob: 'none' #使用に必要な職業。noneで未指定
    cs_name: 'weapon' #スキルのcrackshotファイルのID。
    no_point_msg: '§c<mp_name>が足りません！ 必要<mp_name>: <usepoint>' #MPがない場合のメッセージ PH使用可能
    no_needjob_msg: '§cあなたの職業ではこのスキルを使用できません！ 必要職業: <need_job>' #必要職業ではない場合のメッセージ PH使用可能
    no_needlevel_msg: '§cあなたのレベルではこのスキルを使用できません！ 必要レベル: <need_level>' #必要レベルが足りない場合のメッセージ PH使用可能

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
                SkillData skillData = new SkillData(plugin,plugin.job,skillname,data.getString("viewname"),data.getInt("needlevel"),data.getInt("usepoint")
                ,data.getString("needjob"),data.getString("cs_name"),data.getString("no_point_msg"),data.getString("no_needjob_msg"),data.getString("no_needlevel_msg"));
                skills.put(skillname,skillData);
            }
        }
    }
}