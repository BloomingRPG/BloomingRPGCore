package jp.mkserver.bloom.bloomingrpgcore.playerjobs.status;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class StatsViewer {

    private static final String packageVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

    public static void showActionBar(Player player, String message) {
        try {
            Object chatComponentText = getNMSClass("ChatComponentText").getConstructor(new Class[]{String.class}).newInstance(message);
            Object chatMessageType = getNMSClass("ChatMessageType").getField("GAME_INFO").get(null);
            Object packetPlayOutChat = getNMSClass("PacketPlayOutChat").getConstructor(new Class[]{getNMSClass("IChatBaseComponent"), getNMSClass("ChatMessageType")}).newInstance(chatComponentText, chatMessageType);
            Object getHandle = player.getClass().getMethod("getHandle", new Class[0]).invoke(player);
            Object playerConnection = getHandle.getClass().getField("playerConnection").get(getHandle);

            playerConnection.getClass().getMethod("sendPacket", new Class[]{getNMSClass("Packet")}).invoke(playerConnection, packetPlayOutChat);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static Class<?> getNMSClass(String nmsClassName) {
        try {
            return Class.forName("net.minecraft.server." + packageVersion + "." + nmsClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
