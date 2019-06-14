package jp.mkserver.bloom.bloomingrpgcore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.UUID;

public final class BloomingRPGCore extends JavaPlugin {

    String prefix = "§e§l[§c§lB§6§lRPG§e§l]§§r";

    MySQLManagerV2 mysql;
    private BadCommand badCommand;
    private LoginBonus loginBonus;

    @Override
    public void onEnable() {
        // Plugin startup logic
        mysql = new MySQLManagerV2(this,"BRPG-Core");
        badCommand = new BadCommand(this);
        loginBonus = new LoginBonus(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }



    //簡単作成不可壊アイテム
    public ItemStack createUnbitem(String name, String[] lore, Material item, int dura, boolean pikapika){
        ItemStack items = new ItemStack(item,1,(short)dura);
        ItemMeta meta = items.getItemMeta();
        meta.setLore(Arrays.asList(lore));
        meta.setDisplayName(name);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if(pikapika){
            meta.addEnchant(Enchantment.ARROW_FIRE,1,true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.setUnbreakable(true);
        items.setItemMeta(meta);
        return items;
    }

    //簡単作成アイテム
    public ItemStack createItem(String name, String[] lore, Material item, int dura, boolean pikapika){
        ItemStack items = new ItemStack(item,1,(short)dura);
        ItemMeta meta = items.getItemMeta();
        meta.setLore(Arrays.asList(lore));
        meta.setDisplayName(name);
        if(pikapika){
            meta.addEnchant(Enchantment.ARROW_FIRE,1,true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        items.setItemMeta(meta);
        return items;
    }

    //簡単作成プレイヤーヘッド
    public ItemStack createSkullitem(String name, String[] lore, UUID playeruuid, boolean pikapika){
        ItemStack items = new ItemStack(Material.SKULL_ITEM,1,(short)3);
        SkullMeta meta = (SkullMeta) items.getItemMeta();
        meta.setLore(Arrays.asList(lore));
        meta.setDisplayName(name);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if(pikapika){
            meta.addEnchant(Enchantment.ARROW_FIRE,1,true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.setUnbreakable(true);
        meta.setOwningPlayer(Bukkit.getPlayer(playeruuid));
        items.setItemMeta(meta);
        return items;
    }

}
