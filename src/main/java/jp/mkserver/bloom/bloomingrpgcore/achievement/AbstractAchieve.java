package jp.mkserver.bloom.bloomingrpgcore.achievement;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractAchieve {

    private int id;
    private ItemStack getmae;
    private ItemStack getgo;

    public AbstractAchieve(int id,ItemStack mae,ItemStack go){
        this.id = id;
        this.getmae = mae;
        this.getgo = go;
    }

    public int getId() {
        return id;
    }

    public ItemStack getGetmae() {
        return getmae;
    }

    public ItemStack getGetgo() {
        return getgo;
    }

    public abstract void rewardGive(Player p);
}
