package org.webtrade.mapminecraft.client.data;

/** Describes one stored world (a server or a singleplayer save). */
public class WorldMeta {
    public static final String SERVER = "server";
    public static final String LOCAL = "local";

    public String id = "";
    public String name = "";
    public String type = LOCAL;
    public String address = "";
    public long lastPlayed;
    public String lastDim = "minecraft:overworld";
    public double lastX;
    public double lastZ;

    public boolean isServer() {
        return SERVER.equals(type);
    }
}
