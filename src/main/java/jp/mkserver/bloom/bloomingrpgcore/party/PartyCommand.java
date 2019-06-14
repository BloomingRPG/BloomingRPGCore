package jp.mkserver.bloom.bloomingrpgcore.party;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PartyCommand implements CommandExecutor {

    PartyCore core;

    HashMap<UUID,String> invite = new HashMap<>();

    public PartyCommand(PartyCore core){
        this.core = core;
        core.plugin.getCommand("party").setExecutor(this);
        core.plugin.getCommand("pt").setExecutor(this);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player p = (Player) sender;

        if(args.length == 1){

            if(args[0].equalsIgnoreCase("accept")){
                if(!invite.containsKey(p.getUniqueId())){
                    p.sendMessage(core.plugin.prefix+"§cあなたにパーティの招待は来ていません。");
                    return true;
                }

                //参加
                String name = invite.get(p.getUniqueId());
                core.joinParty(p,name);
                invite.remove(p.getUniqueId());
                return true;
            }

            if(args[0].equalsIgnoreCase("deny")){
                if(!invite.containsKey(p.getUniqueId())){
                    p.sendMessage(core.plugin.prefix+"§cあなたにパーティの招待は来ていません。");
                    return true;
                }

                //拒否
                invite.remove(p.getUniqueId());
                p.sendMessage(core.plugin.prefix+"§cパーティの招待を拒否しました");
                return true;
            }

            if(args[0].equalsIgnoreCase("leave")){
                if(!core.isPlayerPartyJoin(p)){
                    p.sendMessage(core.plugin.prefix+"§cあなたはパーティに参加していません。");
                    return true;
                }

                String pn = core.getPartyPlayer(p);
                PartyCore.Party pa = core.getParty(pn);

                if(pa.owner==p.getUniqueId()){
                    p.sendMessage(core.plugin.prefix+"§cパーティのオーナーは退出できません");
                    return true;
                }

                //離脱
                core.leaveParty(p,core.getPartyPlayer(p));
                return true;
            }

            if(args[0].equalsIgnoreCase("breakout")){
                if(!core.isPlayerPartyJoin(p)){
                    p.sendMessage(core.plugin.prefix+"§cあなたはパーティに参加していません。");
                    return true;
                }

                //解散
                core.breakOutParty(core.getPartyPlayer(p),p);
                return true;
            }

            if(args[0].equalsIgnoreCase("list")){
                if(!core.isPlayerPartyJoin(p)){
                    p.sendMessage(core.plugin.prefix+"§cあなたはパーティに参加していません。");
                    return true;
                }

                //リスト
                PartyCore.Party party = core.getParty(core.getPartyPlayer(p));
                p.sendMessage(core.plugin.prefix+"§e"+party.name+"§aのメンバーリスト");
                for(UUID uuid:party.getPlayerlist()){
                    Player pp = Bukkit.getPlayer(uuid);
                    if(pp!=null){
                        if(party.owner==uuid){
                            p.sendMessage(core.plugin.prefix+"§6☆"+pp.getName());
                        }else{
                            p.sendMessage(core.plugin.prefix+"§e"+pp.getName());
                        }
                    }
                }
                return true;
            }

        }

        if(args.length == 2){

            if(args[0].equalsIgnoreCase("create")){
                if(core.isPlayerPartyJoin(p)){
                    p.sendMessage(core.plugin.prefix+"§cあなたは他のパーティに参加しています");
                    return true;
                }

                //作成
                core.buildParty(p,args[1]);
                return true;
            }

            if(args[0].equalsIgnoreCase("invite")){
                if(!core.isPlayerPartyJoin(p)){
                    p.sendMessage(core.plugin.prefix+"§cあなたはパーティに参加していません。");
                    return true;
                }

                String pn = core.getPartyPlayer(p);
                PartyCore.Party pa = core.getParty(pn);
                if(pa.owner!=p.getUniqueId()){
                    p.sendMessage(core.plugin.prefix+"§cそのパーティのオーナーではありません");
                    return true;
                }

                if(pa.getPlayerlist().size()>=4){
                    p.sendMessage(core.plugin.prefix+"§cパーティはすでに満員です");
                    return true;
                }

                //招待
                Player target = Bukkit.getPlayer(args[1]);
                if(target==null){
                    p.sendMessage(core.plugin.prefix+"§cそのプレイヤーはオフラインです");
                    return true;
                }

                if(target.getName().equalsIgnoreCase(p.getName())){
                    p.sendMessage(core.plugin.prefix+"§c自分自身を誘うことはできません");
                    return true;
                }

                if(invite.containsKey(target.getUniqueId())){
                    p.sendMessage(core.plugin.prefix+"§cそのプレイヤーは他のプレイヤーから招待されています。また後でお試しください。");
                    return true;
                }

                Bukkit.getScheduler().runTaskLaterAsynchronously(core.plugin,()->{
                    if(!invite.containsKey(target.getUniqueId())){
                        return;
                    }
                    invite.remove(target.getUniqueId());
                    if(p.isOnline()){
                        p.sendMessage(core.plugin.prefix+"§c招待が期限切れで拒否されました");
                    }
                    if(target.isOnline()){
                        target.sendMessage(core.plugin.prefix+"§c招待が期限切れとなりました");
                    }
                },20*180);

                invite.put(target.getUniqueId(),pn);
                target.sendMessage(core.plugin.prefix+"§a"+p.getName()+"§eさんからパーティ§a「"+pn+"」§eの招待を受けました！");
                core.plugin.sendHoverText(target,"§a§l[参加する]§e(クリックして参加)","§eクリックで参加！","/party accept");
                core.plugin.sendHoverText(target,"§c§l[拒否する]§e(クリックして拒否)","§eクリックで拒否","/party deny");

                p.sendMessage(core.plugin.prefix+"§a"+target.getName()+"を招待しました。招待は3分間有効です。");
                return true;
            }

        }

        p.sendMessage("§e/party create [パーティ名] : パーティを作成します。");
        p.sendMessage("§e/party invite [プレイヤー名] : パーティにプレイヤーを招待します。(Ownerのみ)");
        p.sendMessage("§e/party accept/deny : パーティに参加/参加を拒否します");
        p.sendMessage("§e/party leave : パーティから離脱します");
        p.sendMessage("§e/party breakout : パーティを解散します (Ownerのみ)");
        return true;
    }
}
