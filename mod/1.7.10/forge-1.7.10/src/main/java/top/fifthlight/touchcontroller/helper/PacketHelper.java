package top.fifthlight.touchcontroller.helper;

import net.minecraft.network.Packet;

import java.lang.reflect.Constructor;

public class PacketHelper {
    private static Constructor<?> playerLookPacketConstructor;

    static {
        try {
            Class<?> clazz = Class.forName("net.minecraft.network.play.client.C03PacketPlayer$C05PacketPlayerLook");
            playerLookPacketConstructor = clazz.getConstructor(float.class, float.class, boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Packet createPlayerLookPacket(float yaw, float pitch, boolean onGround) {
        try {
            return (Packet) playerLookPacketConstructor.newInstance(yaw, pitch, onGround);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
