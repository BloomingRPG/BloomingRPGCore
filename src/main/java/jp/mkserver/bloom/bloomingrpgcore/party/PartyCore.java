package jp.mkserver.bloom.bloomingrpgcore.party;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import jp.mkserver.bloom.bloomingrpgcore.japanizer.JapanizeType;
import jp.mkserver.bloom.bloomingrpgcore.japanizer.Japanizer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PartyCore {

    BloomingRPGCore plugin;
    private HashMap<String,Party> partys;
    private PartyCommand pcommand;

    public PartyCore(BloomingRPGCore plugin){
        this.plugin = plugin;
        partys = new HashMap<>();
        pcommand = new PartyCommand(this);
    }


    public void buildParty(Player p,String partyname){
        if(isPlayerPartyJoin(p)){
            p.sendMessage(plugin.prefix+"§cあなたはすでに他のパーティに参加しています");
            return;
        }
        if(partys.containsKey(partyname)){
            p.sendMessage(plugin.prefix+"§cそのパーティ名は既に使用されています");
            return;
        }
        Party party = new Party(partyname,p);
        plugin.getServer().getPluginManager().registerEvents(party,plugin);
        partys.put(partyname,party);
        p.sendMessage(plugin.prefix+"§aパーティ「§e"+partyname+"§a」を作成しました!");
    }

    public void joinParty(Player p,String party){
        if(isPlayerPartyJoin(p)){
            p.sendMessage(plugin.prefix+"§cあなたはすでに他のパーティに参加しています");
            return;
        }
        Party pa = getParty(party);
        if(pa == null){
            p.sendMessage(plugin.prefix+"§cそのパーティは存在しません");
            return;
        }
        if(!pa.addPlayer(p)){
            p.sendMessage(plugin.prefix+"§cそのパーティは満員のため参加できませんでした");
        }
    }

    public void leaveParty(Player p,String party){
        if(!isPlayerPartyJoin(p)){
            p.sendMessage(plugin.prefix+"§cあなたはパーティに参加していません");
            return;
        }
        Party pa = getParty(party);
        if(pa == null){
            p.sendMessage(plugin.prefix+"§cそのパーティは存在しません");
            return;
        }
        if(!pa.getPlayerlist().contains(p.getUniqueId())){
            p.sendMessage(plugin.prefix+"§cそのパーティに参加していません");
            return;
        }
        pa.leavePlayer(p);
        p.sendMessage(plugin.prefix+"§aパーティから離脱しました");
    }

    public boolean isPlayerPartyJoin(Player p){
        for(Party party : partys.values()){
            if(party.getPlayerlist().contains(p.getUniqueId())){
                return true;
            }
        }
        return false;
    }

    public Party getParty(String party){
        return partys.getOrDefault(party, null);
    }

    public String getPartyPlayer(Player p){
        for(Party party : partys.values()){
            if(party.getPlayerlist().contains(p.getUniqueId())){
                return party.name;
            }
        }
        return null;
    }

    public void breakOutParty(String party,Player p){
        Party pa = getParty(party);
        if(pa == null){
            p.sendMessage(plugin.prefix+"§cそのパーティは存在しません");
            return;
        }
        if(pa.owner!=p.getUniqueId()){
            p.sendMessage(plugin.prefix+"§cそのパーティのオーナーではありません");
            return;
        }
        pa.partyBreakUp();
        partys.remove(party);
        p.sendMessage(plugin.prefix+"§cパーティ「§e"+party+"§c」を解散しました。");
    }

    public void playerKickParty(String party,Player p,String name){
        Party pa = getParty(party);
        if(pa == null){
            p.sendMessage(plugin.prefix+"§cパーティは存在しません");
            return;
        }
        if(pa.owner!=p.getUniqueId()){
            p.sendMessage(plugin.prefix+"§cこのパーティのオーナーではありません");
            return;
        }

        //招待
        Player target = Bukkit.getPlayer(name);

        if(name.equalsIgnoreCase(p.getName())){
            p.sendMessage(plugin.prefix+"§c自分自身をKickすることはできません");
            return;
        }

        if(target==null){
            p.sendMessage(plugin.prefix+"§cそのプレイヤーはオフラインです");
            return;
        }

        if(!pa.getPlayerlist().contains(target.getUniqueId())){
            p.sendMessage(plugin.prefix+"§cそのプレイヤーはこのパーティに参加していません");
            return;
        }

        pa.kickPlayer(target);
        for(UUID uuid:pa.getPlayerlist()){
            Bukkit.getPlayer(uuid).sendMessage(plugin.prefix+"§6"+target.getName()+"§eはパーティを追放された");
        }

        target.sendMessage(plugin.prefix+"§cあなたはパーティを追放されました");
    }

    public class Party implements Listener {
        private List<UUID> playerlist;
        UUID owner;
        String name;

        public Party(String name,Player p){
            this.name = name;
            owner = p.getUniqueId();
            playerlist = new ArrayList<>();
            playerlist.add(p.getUniqueId());
        }

        public List<UUID> getPlayerlist(){
            return playerlist;
        }

        public boolean addPlayer(Player p){
            if(playerlist.size()>=4){
                return false;
            }
            playerlist.add(p.getUniqueId());
            for(UUID uuid:playerlist){
                Bukkit.getPlayer(uuid).sendMessage(plugin.prefix+"§6"+p.getName()+"§eさんがパーティに参加しました！");
            }
            return true;
        }

        public void kickPlayer(Player p){
            playerlist.remove(p.getUniqueId());
        }

        public void leavePlayer(Player p){
            playerlist.remove(p.getUniqueId());
            for(UUID uuid:playerlist){
                Bukkit.getPlayer(uuid).sendMessage(plugin.prefix+"§6"+p.getName()+"§eはパーティを離れた");
            }
        }

        public boolean isBreakUp(){
            return owner == null;
        }

        public void partyBreakUp(){
            owner = null;
            for(UUID uuid:playerlist){
                Bukkit.getPlayer(uuid).sendMessage(plugin.prefix+"§cパーティが解散された");
            }
            playerlist.clear();
        }

        @EventHandler
        public void onExit(PlayerQuitEvent e){
            if(playerlist.contains(e.getPlayer().getUniqueId())){
                if(owner==e.getPlayer().getUniqueId()){
                    for(UUID uuid:playerlist){
                        Bukkit.getPlayer(uuid).sendMessage(plugin.prefix+"§6"+e.getPlayer().getName()+"§eがログアウトしたためパーティは解散された");
                    }
                    breakOutParty(name,e.getPlayer());
                    return;
                }
                kickPlayer(e.getPlayer());
                for(UUID uuid:playerlist){
                    Bukkit.getPlayer(uuid).sendMessage(plugin.prefix+"§6"+e.getPlayer().getName()+"§eはログアウトしたためパーティを抜けた");
                }
            }
        }


        @EventHandler
        public void onChat(AsyncPlayerChatEvent e){
            if(playerlist.contains(e.getPlayer().getUniqueId())){
                if(e.getMessage().startsWith("%")){
                    e.setMessage(e.getMessage().replaceFirst("%",""));
                    return;
                }
                e.setCancelled(true);
                String msg = e.getMessage();
                String msgs = Japanizer.japanize(msg, JapanizeType.GOOGLE_IME);
                if(!msgs.equalsIgnoreCase("")){
                    msg = msg +" §6("+msgs+")";
                }
                for(UUID uuid:playerlist){
                    Bukkit.getPlayer(uuid).sendMessage("§e[Party]§f"+e.getPlayer().getName()+"§a: §r"+ ChatColor.translateAlternateColorCodes('&',msg));
                }
            }
        }

    }
}
