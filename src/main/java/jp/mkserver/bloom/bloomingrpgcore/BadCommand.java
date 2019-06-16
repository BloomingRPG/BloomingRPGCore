package jp.mkserver.bloom.bloomingrpgcore;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.List;

public class BadCommand implements Listener {

    List<String> badCommandList = new ArrayList<>();

    private BloomingRPGCore plugin;

    public BadCommand(BloomingRPGCore plugin){
        this.plugin = plugin;
        initBadCommand();
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }

    //このメソッドで禁止コマンドを指定する
    //指定されたコマンドはタブ保管も不可能になる
    private void initBadCommand(){
        badCommandList.add("/pl");
        badCommandList.add("/plugins");
        badCommandList.add("/bukkit:pl");
        badCommandList.add("/bukkit:plugins");
        badCommandList.add("/?");
        badCommandList.add("/bukkit:?");
        badCommandList.add("/help");
        badCommandList.add("/bukkit:help");
        badCommandList.add("/ver");
        badCommandList.add("/version");
        badCommandList.add("/bukkit:version");
        badCommandList.add("/bukkit:ver");
    }


    @EventHandler
    public void onCommandExecute(PlayerCommandPreprocessEvent e){
        if(badCommandList.contains(e.getMessage())){
            if(e.getPlayer().hasPermission("brpg.core.bypass")){
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (!event.getBuffer().startsWith("/")) {
            return; // not a command, or console entered command. ignore.
        }
        CommandSender sender = event.getSender();
        if (sender.hasPermission("brpg.core.bypass")){
            return; // has bypass. ignore
        }
        for(String bad : badCommandList){
            if(event.getBuffer().startsWith(bad)){
                event.setCompletions(new ArrayList<>());
                return;
            }
        }
        List<String> comple = event.getCompletions();
        comple.removeAll(badCommandList);
        event.setCompletions(comple);
    }
}
