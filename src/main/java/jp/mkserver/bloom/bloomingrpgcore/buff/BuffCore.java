package jp.mkserver.bloom.bloomingrpgcore.buff;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BuffCore implements CommandExecutor {

    BloomingRPGCore plugin;
    HashMap<UUID, List<AbstractBuff>> playerbuffs = new HashMap<>();

    public BuffCore(BloomingRPGCore plugin){
        this.plugin = plugin;
        plugin.getCommand("cbuff").setExecutor(this);
        plugin.getCommand("custombuff").setExecutor(this);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,()->{
            for(Player p : Bukkit.getOnlinePlayers()){
                playerBuffsec(p);
            }
        },0,20);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            if(args.length == 1){
                if(args[0].equalsIgnoreCase("list")){
                    plugin.getLogger().info("§a§lバフリスト");
                    for(String str:BuffType.getBuffTypeNames()){
                        plugin.getLogger().info("§e"+str);
                    }
                    return true;
                }

            }

            if(args.length == 2){

                if(args[0].equalsIgnoreCase("clear")){
                    Player target = Bukkit.getPlayer(args[1]);
                    if(target==null||!target.isOnline()){
                        plugin.getLogger().info("§cプレイヤーが存在しません");
                        return true;
                    }

                    plugin.getLogger().info("§e"+target.getName()+"§cのカスタムバフをすべて消しました");
                    target.sendMessage("§cカスタムバフがすべて消されました");
                    clearBuff(target);
                    return true;
                }

            }

            if(args.length == 3){
                if(args[0].equalsIgnoreCase("give")){

                    Player target = Bukkit.getPlayer(args[2]);
                    if(target==null||!target.isOnline()){
                        plugin.getLogger().info("§cプレイヤーが存在しません");
                        return true;
                    }

                    if(!addBuff(target,args[1])){
                        plugin.getLogger().info("§e"+target.getName()+"§cにカスタムバフを与えました");
                        target.sendMessage("§cカスタムバフが与えられました");
                    }else{
                        plugin.getLogger().info("§c存在しないバフ名です");
                    }
                    return true;
                }

                if(args[0].equalsIgnoreCase("take")) {

                    Player target = Bukkit.getPlayer(args[2]);
                    if(target==null||!target.isOnline()){
                        plugin.getLogger().info("§cプレイヤーが存在しません");
                        return true;
                    }

                    if(!removeBuff(target,args[1])){
                        plugin.getLogger().info("§e"+target.getName()+"§cにカスタムバフを消しました");
                        target.sendMessage("§cカスタムバフが消されました");
                    }else{
                        plugin.getLogger().info("§c存在しないバフ名です");
                    }
                }
            }
            return true;
        }
        Player p = (Player) sender;
        
        if(args.length == 1){
            
            if(args[0].equalsIgnoreCase("list")){
                p.sendMessage("§a§lバフリスト");
                for(String str:BuffType.getBuffTypeNames()){
                    p.sendMessage("§e"+str);
                }
                return true;
            }

            if(args[0].equalsIgnoreCase("clear")){
                p.sendMessage("§cカスタムバフをすべて消しました");
                clearBuff(p);
                return true;
            }
            
        }

        if(args.length == 2){

            if(args[0].equalsIgnoreCase("clear")){
                Player target = Bukkit.getPlayer(args[1]);
                if(target==null||!target.isOnline()){
                    p.sendMessage("§cプレイヤーが存在しません");
                    return true;
                }
                
                p.sendMessage("§e"+target.getName()+"§cのカスタムバフをすべて消しました");
                target.sendMessage("§cカスタムバフがすべて消されました");
                clearBuff(target);
                return true;
            }

            if(args[0].equalsIgnoreCase("give")){
                if(addBuff(p,args[1])){
                    p.sendMessage("§aカスタムバフを与えました");
                }else{
                    p.sendMessage("§c存在しないバフ名です");
                }
                return true;
            }
            
            if(args[0].equalsIgnoreCase("take")) {
                if(removeBuff(p,args[1])){
                    p.sendMessage("§aカスタムバフを消しました");
                }else{
                    p.sendMessage("§c存在しないバフ名です");
                }
            }

        }
        
        if(args.length == 3){
            if(args[0].equalsIgnoreCase("give")){

                Player target = Bukkit.getPlayer(args[2]);
                if(target==null||!target.isOnline()){
                    p.sendMessage("§cプレイヤーが存在しません");
                    return true;
                }
                
                if(addBuff(target,args[1])){
                    p.sendMessage("§e"+target.getName()+"§cにカスタムバフを与えました");
                    target.sendMessage("§cカスタムバフが与えられました");
                }else{
                    p.sendMessage("§c存在しないバフ名です");
                }
                return true;
            }

            if(args[0].equalsIgnoreCase("take")) {

                Player target = Bukkit.getPlayer(args[2]);
                if(target==null||!target.isOnline()){
                    p.sendMessage("§cプレイヤーが存在しません");
                    return true;
                }
                
                if(removeBuff(target,args[1])){
                    p.sendMessage("§e"+target.getName()+"§cにカスタムバフを消しました");
                    target.sendMessage("§cカスタムバフが消されました");
                }else{
                    p.sendMessage("§c存在しないバフ名です");
                }
            }
        }

        p.sendMessage("§a/cbuff give [名前] (player名) : バフを付与します");
        p.sendMessage("§a/cbuff take [名前] (player名) : バフを消します");
        p.sendMessage("§a/cbuff clear (player名) : バフをすべて消します");
        p.sendMessage("§a/cbuff list : バフのリストを表示します");
        return true;
    }

    // バフリストに追加したいバフ名を追加する
    public enum BuffType {

        BLESSING;

        public static BuffType fromString(String param) {
            String toUpper = param.toUpperCase();
            try {
                return valueOf(toUpper);
            }catch (Exception e){
                return null;
            }
        }

        public static List<String> getBuffTypeNames() {
            List<String> list = new ArrayList<>();
            for(BuffType type:BuffType.values()){
                list.add(type.name());
            }
            return list;
        }
    }

    public AbstractBuff createBuff(Player p,BuffType type){
        if(type==BuffType.BLESSING){
            return new Blessing(this,p.getUniqueId()); //神の祝福
        }else{
            return null;
        }
    }

    private void addBuff(Player p,BuffType buff){
        addBuff(p,createBuff(p,buff));
    }

    public void clearBuff(Player p){
        List<AbstractBuff> buffs = new ArrayList<>();
        playerbuffs.put(p.getUniqueId(),buffs);
    }

    private void addBuff(Player p,AbstractBuff buff){
        if(playerbuffs.containsKey(p.getUniqueId())){
            List<AbstractBuff> buffs = playerbuffs.get(p.getUniqueId());
            buffs.add(buff);
            playerbuffs.put(p.getUniqueId(),buffs);
            return;
        }
        List<AbstractBuff> buffs = new ArrayList<>();
        buffs.add(buff);
        playerbuffs.put(p.getUniqueId(),buffs);
    }

    public boolean addBuff(Player p,String buffname){
        BuffType type = BuffType.fromString(buffname);
        if(type==null){
            return false;
        }else{
            addBuff(p,type);
            return true;
        }
    }
    
    public boolean removeBuff(Player p,String buffname){
        BuffType type = BuffType.fromString(buffname);
        if(type==null){
            return false;
        }else{
            removeBuff(p,type);
            return true;
        }
    }

    void removeBuff(Player p,BuffType bufftype){
        if(playerbuffs.containsKey(p.getUniqueId())){
            List<AbstractBuff> buffs = playerbuffs.get(p.getUniqueId());
            for(AbstractBuff buff:buffs){
                if(buff.getBuffid().equalsIgnoreCase(bufftype.name())){
                    buffs.remove(buff);
                }
            }
            playerbuffs.put(p.getUniqueId(),buffs);
            return;
        }
        List<AbstractBuff> buffs = new ArrayList<>();
        playerbuffs.put(p.getUniqueId(),buffs);
    }

    public void playerBuffsec(Player p){
        if(playerbuffs.containsKey(p.getUniqueId())){
            List<AbstractBuff> buffs = playerbuffs.get(p.getUniqueId());
            for(AbstractBuff buff:buffs){
                if(buff.isIscallRun()){
                   buff.run();
                }
            }
        }
    }


}