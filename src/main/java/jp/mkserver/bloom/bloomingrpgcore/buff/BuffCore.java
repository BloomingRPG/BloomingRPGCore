package jp.mkserver.bloom.bloomingrpgcore.buff;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import jp.mkserver.bloom.bloomingrpgcore.api.BossBarAPIPlus;
import jp.mkserver.bloom.bloomingrpgcore.api.PlayerList;
import jp.mkserver.bloom.bloomingrpgcore.api.VaultAPI;
import jp.mkserver.bloom.bloomingrpgcore.buff.data.Avoidance;
import jp.mkserver.bloom.bloomingrpgcore.buff.data.Blessing;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class BuffCore implements CommandExecutor {

    public BloomingRPGCore plugin;
    HashMap<UUID, CopyOnWriteArrayList<AbstractBuff>> playerbuffs = new HashMap<>();

    public BuffCore(BloomingRPGCore plugin){
        this.plugin = plugin;
        this.board = Bukkit.getScoreboardManager().getNewScoreboard();
        plugin.getCommand("cbuff").setExecutor(this);
        plugin.getCommand("custombuff").setExecutor(this);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,()->{
            gettitlecount++;
            for(Player p : Bukkit.getOnlinePlayers()){
                String prefix = VaultAPI.chat.getPlayerPrefix(p);
                String suffix = VaultAPI.chat.getPlayerSuffix(p);
                if(prefix==null&&suffix==null){
                    return;
                }
                Team team = board.getTeam(p.getName());
                if(team==null){
                    board.registerNewTeam(p.getName());
                    team = board.getTeam(p.getName());
                }
                if(prefix!=null) {
                    team.setPrefix(ChatColor.translateAlternateColorCodes('&',prefix));
                }
                if(suffix!=null) {
                    team.setSuffix(ChatColor.translateAlternateColorCodes('&',suffix));
                }
                if(!team.hasEntry(p.getName())){
                    team.addEntry(p.getName());
                }
                p.setScoreboard(board);
                plugin.buff.updatePlayerEffectViewer(p);
            }
        },20,20);
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
                        plugin.getLogger().info("§e"+target.getName()+"§cに"+BuffType.fromString(args[1]).name()+"を与えました");
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
                        plugin.getLogger().info("§e"+target.getName()+"§cの"+BuffType.fromString(args[1]).name()+"を消しました");
                        target.sendMessage("§c"+BuffType.fromString(args[1]).name()+"が消されました");
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
                    p.sendMessage("§a"+BuffType.fromString(args[1]).name()+"を与えました");
                }else{
                    p.sendMessage("§c存在しないバフ名です");
                }
                return true;
            }
            
            if(args[0].equalsIgnoreCase("take")) {
                if(removeBuff(p,args[1])){
                    p.sendMessage("§c"+BuffType.fromString(args[1]).name()+"を消しました");
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
                    p.sendMessage("§e"+target.getName()+"§cに"+BuffType.fromString(args[1]).name()+"を与えました");
                    target.sendMessage("§a"+BuffType.fromString(args[1]).name()+"が与えられました");
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
                    p.sendMessage("§e"+target.getName()+"§cの"+BuffType.fromString(args[1]).name()+"を消しました");
                    target.sendMessage("§c"+BuffType.fromString(args[1]).name()+"が消されました");
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

        BLESSING,AVOIDANCE;

        public static BuffType fromString(String param) {
            String toUpper = param.toUpperCase();
            try {
                return valueOf(toUpper);
            }catch (Exception e){
                return null;
            }
        }

        public static List<String> getBuffTypeNames() {
            CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
            for(BuffType type:BuffType.values()){
                list.add(type.name());
            }
            return list;
        }
    }

    public AbstractBuff createBuff(Player p,BuffType type){
        if(type==BuffType.BLESSING) {
            return new Blessing(this, p.getUniqueId()); //神の祝福
        }else if(type==BuffType.AVOIDANCE){
            return new Avoidance(this, p.getUniqueId()); //回避の目
        }else{
            return null;
        }
    }

    private void addBuff(Player p,BuffType buff){
        addBuff(p,createBuff(p,buff));
    }

    public void clearBuff(Player p){
        CopyOnWriteArrayList<AbstractBuff> buffs = new CopyOnWriteArrayList<>();
        playerbuffs.put(p.getUniqueId(),buffs);
        updatePlayerEffectViewer(p);
    }

    private void addBuff(Player p,AbstractBuff buff){
        if(playerbuffs.containsKey(p.getUniqueId())){
            CopyOnWriteArrayList<AbstractBuff> buffs = playerbuffs.get(p.getUniqueId());
            buffs.add(buff);
            playerbuffs.put(p.getUniqueId(),buffs);
        }else {
            CopyOnWriteArrayList<AbstractBuff> buffs = new CopyOnWriteArrayList<>();
            buffs.add(buff);
            playerbuffs.put(p.getUniqueId(), buffs);
        }
        updatePlayerEffectViewer(p);
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

    public void removeBuff(Player p,BuffType bufftype){
        if(playerbuffs.containsKey(p.getUniqueId())){
            CopyOnWriteArrayList<AbstractBuff> buffs = playerbuffs.get(p.getUniqueId());
            for(AbstractBuff buff:buffs){
                if(buff.getBuffid().equalsIgnoreCase(bufftype.name())){
                    buff.endTask();
                    buffs.remove(buff);
                }
            }
            playerbuffs.put(p.getUniqueId(),buffs);
        }else {
            CopyOnWriteArrayList<AbstractBuff> buffs = new CopyOnWriteArrayList<>();
            playerbuffs.put(p.getUniqueId(), buffs);
        }
        updatePlayerEffectViewer(p);
    }

    public void removeBuff(UUID uuid,BuffType bufftype){
        if(playerbuffs.containsKey(uuid)){
            CopyOnWriteArrayList<AbstractBuff> buffs = playerbuffs.get(uuid);
            for(AbstractBuff buff:buffs){
                if(buff.getBuffid().equalsIgnoreCase(bufftype.name())){
                    buff.endTask();
                    buffs.remove(buff);
                }
            }
            playerbuffs.put(uuid,buffs);
        }else {
            CopyOnWriteArrayList<AbstractBuff> buffs = new CopyOnWriteArrayList<>();
            playerbuffs.put(uuid, buffs);
        }
        Player p =Bukkit.getPlayer(uuid);
        if(p!=null&&p.isOnline()){
            updatePlayerEffectViewer(p);
        }
    }

    HashMap<UUID, BossBarAPIPlus> bossbars = new HashMap<>();

    private int gettitlecount = 0;

    public String getRPGTitle(){
        if(gettitlecount>=7){
            gettitlecount = 0;
        }
        if(gettitlecount==0){
            return "§6§lWelCome to §c§lBlooming§e§lRPG";
        }else if(gettitlecount==1){
            return "§e§lWel§6§lCome to §c§lBloomingRPG";
        }else if(gettitlecount==2){
            return "§6§lWel§e§lCome §6§lto §c§lBloomingRPG";
        }else if(gettitlecount==3){
            return "§6§lWelCome §e§lto §c§lBloomingRPG";
        }else if(gettitlecount==4){
            return "§6§lWelCome to §e§lBloom§c§lingRPG";
        }else if(gettitlecount==5){
            return "§6§lWelCome to §c§lBloom§e§ling§c§lRPG";
        }else{
            return "§6§lWelCome to §c§lBloomingRPG";
        }
    }

    Scoreboard board;

    public void updatePlayerEffectViewer(Player p){
        PlayerList list = PlayerList.getPlayerList(p);
        int i = 0;
        for(Player pp : Bukkit.getOnlinePlayers()){
            list.updateSlot(i,pp.getName(),true);
            i++;
        }
        String result = "§e"+Bukkit.getOnlinePlayers().size()+"§d/§e"+Bukkit.getMaxPlayers();
        if(playerbuffs.containsKey(p.getUniqueId())&&playerbuffs.get(p.getUniqueId()).size()!=0) {
            CopyOnWriteArrayList<AbstractBuff> buffs = playerbuffs.get(p.getUniqueId());
            for (AbstractBuff buff : buffs) {
                if (!buff.getEffectTime().equalsIgnoreCase("0秒")) {
                    result = result + "\n" +(buff.getViewname() + "§f:§e" + buff.getEffectTime());
                } else {
                    result = result + "\n" +(buff.getViewname());
                }
            }
        }

        list.setHeaderFooter(getRPGTitle(), result);

        /* Old version
        BossBarAPIPlus bar;
        if(bossbars.containsKey(p.getUniqueId())){
            bar = bossbars.get(p.getUniqueId());
            bar.stopMovingTitle();
        }else{
            bar = new BossBarAPIPlus(this,"Creating now", BarColor.YELLOW, BarStyle.SOLID);
        }

        List<String> list = new ArrayList<>();
        if(playerbuffs.containsKey(p.getUniqueId())&&playerbuffs.get(p.getUniqueId()).size()!=0) {
            CopyOnWriteArrayList<AbstractBuff> buffs = playerbuffs.get(p.getUniqueId());
            for (AbstractBuff buff : buffs) {
                if(!buff.getEffectTime().equalsIgnoreCase("0秒")){
                    list.add(buff.getViewname()+"§f:§e"+buff.getEffectTime());
                }else{
                    list.add(buff.getViewname());
                }
            }
            bar.createMovingTitle(list,3);
            bossbars.put(p.getUniqueId(),bar);
            bar.showPlayer(p);
        }else{
            bar.unVisiblePlayer(p);
            bossbars.remove(p.getUniqueId());
        } */
    }


}
