package jp.mkserver.bloom.bloomingrpgcore.buff.data;

import jp.mkserver.bloom.bloomingrpgcore.buff.BuffCore;
import jp.mkserver.bloom.bloomingrpgcore.buff.AbstractBuff;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;

public class Blessing extends AbstractBuff {

    public Blessing(BuffCore core, UUID player) {
        super(core, BuffCore.BuffType.BLESSING, player, "§6§l神の§f§l祝福","BLESSING", false,0,-1);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if (e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
            if(p==null||!p.isOnline()||!getPlayer().isOnline()||!p.getUniqueId().equals(getPlayer().getUniqueId())){
                return;
            }
            double damage = e.getDamage();
            if(damage >= p.getHealth()){
                e.setDamage(p.getHealth()-0.5);
                p.sendMessage("§e???: そんなところで死にたくないじゃろう？");
                p.sendMessage("§e§l謎の力で死を回避した！");
                p.getWorld().spawnParticle(Particle.TOTEM, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), 50, 0, 0, 0);
                for(Player pp : Bukkit.getOnlinePlayers()){
                    pp.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE,1.0f,0.8f);
                }
                unRegister();
            }
        }
    }
}
