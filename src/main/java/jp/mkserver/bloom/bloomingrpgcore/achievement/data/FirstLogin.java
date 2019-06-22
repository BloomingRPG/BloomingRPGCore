package jp.mkserver.bloom.bloomingrpgcore.achievement.data;

import jp.mkserver.bloom.bloomingrpgcore.BloomingRPGCore;
import jp.mkserver.bloom.bloomingrpgcore.achievement.AbstractAchieve;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FirstLogin extends AbstractAchieve {
    //test
    public FirstLogin() {
        super(1,
                BloomingRPGCore.createUnbitem("§a§l?め?のロ??ン",new String[]{"初めて○○○○する"}, Material.OBSIDIAN,0,false),
                BloomingRPGCore.createUnbitem("§a§l初めてのログイン",new String[]{"初めてログインする"}, Material.APPLE,0,false));
    }

    @Override
    public void rewardGive(Player p) {

    }
}
