package jp.mkserver.bloom.bloomingrpgcore;

import org.bukkit.plugin.java.JavaPlugin;

public final class BloomingRPGCore extends JavaPlugin {

    String prefix = "§e§l[§c§lB§6§lRPG§e§l]§§r";

    private BadCommand badCommand;

    @Override
    public void onEnable() {
        // Plugin startup logic
        badCommand = new BadCommand(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
