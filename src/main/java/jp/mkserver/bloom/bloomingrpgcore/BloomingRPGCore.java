package jp.mkserver.bloom.bloomingrpgcore;

import jp.mkserver.bloom.bloomingrpgcore.api.VaultAPI;
import jp.mkserver.bloom.bloomingrpgcore.buff.BuffCore;
import jp.mkserver.bloom.bloomingrpgcore.flag.FlagManager;
import jp.mkserver.bloom.bloomingrpgcore.npc.NPCScript;
import jp.mkserver.bloom.bloomingrpgcore.party.PartyCore;
import jp.mkserver.bloom.bloomingrpgcore.playerjobs.jobs.JobsCore;
import jp.mkserver.bloom.bloomingrpgcore.playerjobs.skill.SkillCore;
import jp.mkserver.bloom.bloomingrpgcore.playerjobs.status.StatsCore;
import jp.mkserver.bloom.bloomingrpgcore.extra.BadCommand;
import jp.mkserver.bloom.bloomingrpgcore.extra.LoginBonus;
import jp.mkserver.bloom.bloomingrpgcore.extra.Spawn;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.UUID;

public final class BloomingRPGCore extends JavaPlugin {

    public String prefix = "§e§l[§c§lB§6§lRPG§e§l]§r";

    public MySQLManagerV2 mysql;
    private BadCommand badCommand;
    private LoginBonus loginBonus;
    private PartyCore partyCore;
    private NPCScript npc;

    public Spawn spawn;

    public JobsCore job;
    public SkillCore skill;
    public StatsCore stats;
    public BuffCore buff;

    public FlagManager flag;

    public FileConfiguration config;
    public VaultAPI vault;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        mysql = new MySQLManagerV2(this,"BRPG-Core");
        vault = new VaultAPI(this);
        config = getConfig();
        flag = new FlagManager(this);
        badCommand = new BadCommand(this);
        loginBonus = new LoginBonus(this);
        partyCore = new PartyCore(this);
        npc = new NPCScript(this);
        spawn = new Spawn(this);

        buff = new BuffCore(this);
        stats = new StatsCore(this);
        job = new JobsCore(this);
        skill = new SkillCore(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }



    //簡単作成不可壊アイテム
    public static ItemStack createUnbitem(String name, String[] lore, Material item, int dura, boolean pikapika){
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
    public static ItemStack createItem(String name, String[] lore, Material item, int dura, boolean pikapika){
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
    public static ItemStack createSkullitem(String name, String[] lore, UUID playeruuid, boolean pikapika){
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

    ////////////////////////////////////////////////////////////////////////////////////////////
    //  マインクラフトチャットに、ホバーテキストや、クリックコマンドを設定する関数
    // [例1] sendHoverText(player,"ここをクリック",null,"/say おはまん");
    // [例2] sendHoverText(player,"カーソルをあわせて","ヘルプメッセージとか",null);
    // [例3] sendHoverText(player,"カーソルをあわせてクリック","ヘルプメッセージとか","/say おはまん");
    public void sendHoverText(Player p,String text,String hoverText,String command){
        //////////////////////////////////////////
        //      ホバーテキストとイベントを作成する
        HoverEvent hoverEvent = null;
        if(hoverText != null){
            BaseComponent[] hover = new ComponentBuilder(hoverText).create();
            hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover);
        }

        //////////////////////////////////////////
        //   クリックイベントを作成する
        ClickEvent clickEvent = null;
        if(command != null){
            clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,command);
        }

        BaseComponent[] message = new ComponentBuilder(text).event(hoverEvent).event(clickEvent). create();
        p.spigot().sendMessage(message);
    }

    //  マインクラフトチャットに、ホバーテキストや、クリックコマンドサジェストを設定する
    public void sendSuggestCommand(Player p, String text, String hoverText, String command){

        //////////////////////////////////////////
        //      ホバーテキストとイベントを作成する
        HoverEvent hoverEvent = null;
        if(hoverText != null){
            BaseComponent[] hover = new ComponentBuilder(hoverText).create();
            hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover);
        }

        //////////////////////////////////////////
        //   クリックイベントを作成する
        ClickEvent clickEvent = null;
        if(command != null){
            clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND ,command);
        }

        BaseComponent[] message = new ComponentBuilder(text). event(hoverEvent).event(clickEvent). create();
        p.spigot().sendMessage(message);
    }

}
