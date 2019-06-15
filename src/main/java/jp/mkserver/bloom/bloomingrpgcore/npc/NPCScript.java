package jp.mkserver.bloom.bloomingrpgcore.npc;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.util.*;

public class NPCScript implements Listener {

    BloomingRPGCore plugin;

    HashMap<String,FileConfiguration> scripts = new HashMap<>();

    public NPCScript(BloomingRPGCore plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
        loadNPCFile();
    }

    /*
    NPCScriptはNPCと分岐ありのトークができたりコマンドを実行させられたりできるものです。

    PlaceHolder(PH)一覧
    <player>: プレイヤー名

    スクリプト仕様
    ・上から順に実行されます。
    ・一部、実行名にできない単語があるようです。(yes,noなど)

    スクリプト一覧
    say ○○: チャットをクリックしたプレイヤーに送信します。PH使用可能。
    delay 数字: 1につき1tick遅延します。
    select セレクト不可時メッセージ 選択肢1:選んだ場合の実行するスクリプト名 選択肢2:選んだ場合の実行するスクリプト名 選択肢3:選んだ場合の実行するスクリプト名 …
    =>条件分岐を作成できます。
    command ○○ △△: コマンドをコンソールから実行できます。PH使用可能。
    hasflag:フラグ名 持っていた場合の実行するスクリプト名   => フラグが立っていた場合スクリプトを中断して別のスクリプトを起動します
    addflag フラグ名: フラグ名のフラグを立てます
    removeflag フラグ名: フラグ名のフラグを下げます
    randomexecute 実行スクリプト名1 実行スクリプト名2 ...
    => ランダムでスクリプトを実行します。
    execute 実行スクリプト名: スクリプトを実行します

    重要
    unlockrenda: 連打対策を無効化します。通常は最後まで話を聞くまで再度話しかけられません。

    使用例

    main:
    - 'say 知ってますか？'
    - 'delay 10'
    - 'select 他の人と話してるようですね。 はい:hai いいえ:wakarann'

    hai:
    - 'say やはり、そうでしたか。'

    wakarann:
    - 'say それを しらないなんて とんでもない！'
    - 'delay 20'
    - 'command kill <player>'
     */


    public void loadNPCFile(){
        scripts.clear();
        File folder = new File(plugin.getDataFolder(), File.separator + "npc");
        if(!folder.exists()){
            folder.mkdir();
        }
        for(String s: folder.list()){
            File f = new File(folder, File.separator + s);
            FileConfiguration data = YamlConfiguration.loadConfiguration(f);

            if (f.exists()) {
                scripts.put(s.replaceFirst(".yml",""),data);
            }
        }
    }

    public FileConfiguration getNPCFile(String name){
        if(scripts.containsKey(name)){
            return scripts.get(name);
        }
        return null;
    }

    public List<String> getNPCScript(FileConfiguration config, String id){
        return config.getStringList(id);
    }

    HashMap<String,String> waitingNPCTalk = new HashMap<>();

    public void executeScript(String npcname,List<String> list, Player p){
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
            if(waitingNPCTalk.containsKey(p.getUniqueId().toString()+":"+npcname)){
                String script = waitingNPCTalk.get(p.getUniqueId().toString()+":"+npcname);
                if(!script.equalsIgnoreCase("none")){
                    if(script.startsWith("say ")){
                        script = script.replaceFirst("say ","");
                        p.sendMessage(script.replace("<player>",p.getName()));
                    }else if(script.startsWith("delay ")) {
                        script = script.replaceFirst("delay ", "");
                        int delay = 0;
                        try {
                            delay = Integer.parseInt(script);
                            Thread.sleep(delay * 50);
                        } catch (NumberFormatException e) {
                            plugin.getLogger().warning("NPCScript Error! type: delay is not Number.\n" +
                                    "File: " + npcname + ".yml Line ?「" + script + "」");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else if(script.startsWith("command ")){
                        script = script.replaceFirst("command ","");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),script.replace("<player>",p.getName()));
                    }else if(script.startsWith("addflag ")){
                        script = script.replaceFirst("addflag ","");
                        plugin.flag.addPlayerFlag(p,script);
                    }else if(script.startsWith("removeflag ")){
                        script = script.replaceFirst("removeflag ","");
                        plugin.flag.removePlayerFlag(p,script);
                    }else if(script.startsWith("execute ")){
                        script = script.replaceFirst("execute ","");
                        waitingNPCTalk.remove(p.getUniqueId().toString()+":"+npcname);
                        executeScript(npcname,getNPCScript(getNPCFile(npcname),script),p);
                        return;
                    }else if(script.startsWith("randomexecute ")){
                        script = script.replaceFirst("randomexecute ","");
                        waitingNPCTalk.remove(p.getUniqueId().toString()+":"+npcname);
                        String[] scripts = script.split(" ");
                        Random rnd = new Random();
                        int s = rnd.nextInt(scripts.length);
                        waitingNPCTalk.remove(p.getUniqueId().toString()+":"+npcname);
                        executeScript(npcname,getNPCScript(getNPCFile(npcname),scripts[s]),p);
                        return;
                    }else if(script.startsWith("hasflag:")){
                        script = script.replaceFirst("hasflag:","");
                        String[] arg = script.split(" ",2);
                        if(plugin.flag.isPlayerHasFlag(p,arg[0])){
                            waitingNPCTalk.remove(p.getUniqueId().toString()+":"+npcname);
                            executeScript(npcname,getNPCScript(getNPCFile(npcname),arg[1]),p);
                            return;
                        }
                    }else if(script.startsWith("select ")){
                        script = script.replaceFirst("select ","");
                        String[] args = script.split(" ");
                        List<String> uuids = new ArrayList<>();
                        for(int ii = 1;ii<args.length;ii++){
                            String data = args[ii];
                            String[] datas = data.split(":");
                            data = datas[0];
                            String privateid = UUID.randomUUID().toString();
                            plugin.sendHoverText(p,data,null,"@NPCDataChat: "+privateid);
                            npcFlag.put(privateid,npcname+" "+datas[1]);
                            uuids.add(privateid);
                        }
                        waitingNPCTalk.put(p.getUniqueId().toString()+":"+npcname,script);
                        selectData.put(p.getUniqueId(),uuids);
                        return;
                    }
                }
                return;
            }
            waitingNPCTalk.put(p.getUniqueId().toString()+":"+npcname,"none");
            for(int i = 0;i<list.size();i++){
                String script = list.get(i);
                if(!p.isOnline()){
                    break;
                }

                if(script.equalsIgnoreCase("unlockrenda")){
                    waitingNPCTalk.remove(p.getUniqueId().toString()+":"+npcname);
                }

                if(script.startsWith("say ")){
                    script = script.replaceFirst("say ","");
                    p.sendMessage(script.replace("<player>",p.getName()));
                }else if(script.startsWith("delay ")) {
                    script = script.replaceFirst("delay ", "");
                    int delay = 0;
                    try {
                        delay = Integer.parseInt(script);
                        Thread.sleep(delay * 50);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("NPCScript Error! type: delay is not Number.\n" +
                                "File: " + npcname + ".yml Line " + i + 1 + "「" + list.get(i) + "」");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else if(script.startsWith("command ")){
                    script = script.replaceFirst("command ","");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),script.replace("<player>",p.getName()));
                }else if(script.startsWith("addflag ")){
                    script = script.replaceFirst("addflag ","");
                    plugin.flag.addPlayerFlag(p,script);
                }else if(script.startsWith("removeflag ")){
                    script = script.replaceFirst("removeflag ","");
                    plugin.flag.removePlayerFlag(p,script);
                }else if(script.startsWith("execute ")){
                    script = script.replaceFirst("execute ","");
                    waitingNPCTalk.remove(p.getUniqueId().toString()+":"+npcname);
                    executeScript(npcname,getNPCScript(getNPCFile(npcname),script),p);
                    break;
                }else if(script.startsWith("randomexecute ")){
                    script = script.replaceFirst("randomexecute ","");
                    waitingNPCTalk.remove(p.getUniqueId().toString()+":"+npcname);
                    String[] scripts = script.split(" ");
                    Random rnd = new Random();
                    int s = rnd.nextInt(scripts.length);
                    waitingNPCTalk.remove(p.getUniqueId().toString()+":"+npcname);
                    executeScript(npcname,getNPCScript(getNPCFile(npcname),scripts[s]),p);
                    break;
                }else if(script.startsWith("hasflag:")){
                    script = script.replaceFirst("hasflag:","");
                    String[] arg = script.split(" ",2);
                    if(plugin.flag.isPlayerHasFlag(p,arg[0])){
                        waitingNPCTalk.remove(p.getUniqueId().toString()+":"+npcname);
                        executeScript(npcname,getNPCScript(getNPCFile(npcname),arg[1]),p);
                        break;
                    }
                }else if(script.startsWith("select ")){
                    script = script.replaceFirst("select ","");
                    String[] args = script.split(" ");
                    if(selectData.containsKey(p.getUniqueId())){
                        executeScript(npcname,getNPCScript(getNPCFile(npcname),args[0]),p);
                        break;
                    }
                    List<String> uuids = new ArrayList<>();
                    for(int ii = 1;ii<args.length;ii++){
                        String data = args[ii];
                        String[] datas = data.split(":");
                        data = datas[0];
                        String privateid = UUID.randomUUID().toString();
                        plugin.sendHoverText(p,data,null,"@NPCDataChat: "+privateid);
                        npcFlag.put(privateid,npcname+" "+datas[1]);
                        uuids.add(privateid);
                    }
                    waitingNPCTalk.put(p.getUniqueId().toString()+":"+npcname,list.get(i));
                    selectData.put(p.getUniqueId(),uuids);
                    return;
                }
            }
            waitingNPCTalk.remove(p.getUniqueId().toString()+":"+npcname);
        });
    }


    HashMap<String,String> npcFlag = new HashMap<>();
    HashMap<UUID,List<String>> selectData = new HashMap<>();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        if(e.getMessage().startsWith("@NPCDataChat: ")){
            e.setCancelled(true);
            e.setMessage(e.getMessage().replace("@NPCDataChat: ",""));
            if(npcFlag.containsKey(e.getMessage())){
                String flagdata = npcFlag.get(e.getMessage());
                npcFlag.remove(e.getMessage());
                for(String str: selectData.get(e.getPlayer().getUniqueId())){
                    npcFlag.remove(str);
                }
                selectData.remove(e.getPlayer().getUniqueId());
                String[] flag = flagdata.split(" ",2);
                FileConfiguration file = getNPCFile(flag[0]);
                if(file==null){
                    plugin.getLogger().warning("NPCScript Error! type: not exist file.\n" +
                            "File: "+flag[0]+".yml");
                    return;
                }
                List<String> list = getNPCScript(file,flag[1]);
                if(list==null){
                    plugin.getLogger().warning("NPCScript Error! type: not exist func.\n" +
                            "File: "+flag[0]+".yml 「"+flag[1]+"」");
                    return;
                }
                waitingNPCTalk.remove(e.getPlayer().getUniqueId().toString()+":"+flag[0]);
                executeScript(flag[0],list,e.getPlayer());
            }
        }
    }

    @EventHandler
    public void click(NPCRightClickEvent event){
        //Handle a click on a NPC. The event has a getNPC() method.
        //Be sure to check event.getNPC() == this.getNPC() so you only handle clicks on this NPC!
        FileConfiguration file = getNPCFile(event.getNPC().getId()+"");
        if(file==null){
            return;
        }
        executeScript(event.getNPC().getId()+"",getNPCScript(file,"main"),event.getClicker());
    }
}
