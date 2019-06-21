package jp.mkserver.bloom.bloomingrpgcore.buff;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Random;
import java.util.UUID;

public class Avoidance extends AbstractBuff {

    public Avoidance(BuffCore core, UUID player) {
        super(core, BuffCore.BuffType.AVOIDANCE, player, "§a§l回避の目","AVOIDANCE", false,0,-1);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if (e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
            if(p==null||!p.isOnline()||!getPlayer().isOnline()||!p.getUniqueId().equals(getPlayer().getUniqueId())){
                return;
            }
            Random rnd = new Random();
            int i = rnd.nextInt(5)+1;
            if(i==5){
                e.setDamage(0);
                p.sendMessage("§a§l見えたｯ！");
                p.sendMessage("§b風のように避けてダメージを回避した！");
                p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), 50, 0, 0, 0);
                for(Player pp : Bukkit.getOnlinePlayers()){
                    pp.playSound(p.getLocation(), Sound.ITEM_ELYTRA_FLYING,1.0f,0.8f);
                }
                unRegister();
            }
        }
    }
}
