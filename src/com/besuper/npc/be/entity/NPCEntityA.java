package com.besuper.npc.be.entity;

import com.besuper.npc.be.NPC;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

import java.util.*;

public class NPCEntityA {

    // NPC informations
    private final int entityID;
    private GameProfile gameprofile;
    private final Location location;
    private final String name;

    // Skin
    private final String value;
    private final String signature;

    // Packets
    private final PacketPlayOutNamedEntitySpawn spawnEntityPacket = new PacketPlayOutNamedEntitySpawn();
    private final PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam();
    private PacketPlayOutEntity.PacketPlayOutEntityLook entityLookPacket;
    private final List<PacketPlayOutEntityEquipment> equipementPacket = new ArrayList<>();

    public NPCEntityA(final Location loc) {

        this.entityID = (int) Math.ceil(Math.random() * 1000) + 2000;
        this.name = "Jean";
        this.value = "";
        this.signature = "";

        this.location = loc;

        this.gameprofile = new GameProfile(UUID.randomUUID(), "§c ");
        this.gameprofile.getProperties().put("textures", new Property("textures", this.value, this.signature));

        setupPackets();

    }

    private void addToTablist(final Player player) {
        final PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
        final PacketPlayOutPlayerInfo.PlayerInfoData data = new PacketPlayOutPlayerInfo.PlayerInfoData(gameprofile, 1, EnumGamemode.CREATIVE, CraftChatMessage.fromString(gameprofile.getName())[0]);
        final List<PacketPlayOutPlayerInfo.PlayerInfoData> players = packet.b;
        players.add(data);

        packet.a = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER;
        packet.b = players;

        player.sendPacket(packet);

    }

    private void removeFromTablist(final Player player) {
        final PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
        final PacketPlayOutPlayerInfo.PlayerInfoData data = new PacketPlayOutPlayerInfo.PlayerInfoData(gameprofile, 1, EnumGamemode.CREATIVE, CraftChatMessage.fromString(gameprofile.getName())[0]);
        final List<PacketPlayOutPlayerInfo.PlayerInfoData> players = packet.b;
        players.add(data);

        packet.a = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER;
        packet.b = players;

        player.sendPacket(packet);
    }

    private void setupPackets() {

        // Register team packet
        /*packetPlayOutScoreboardTeam.h = 0;
        packetPlayOutScoreboardTeam.b = name;
        packetPlayOutScoreboardTeam.a = name;
        packetPlayOutScoreboardTeam.e = "never";
        packetPlayOutScoreboardTeam.i = 1;
        packetPlayOutScoreboardTeam.c = ChatColor.BLUE + "[NPC] ";
        packetPlayOutScoreboardTeam.g.add(name);*/

        // Spawning Entity living packet
        spawnEntityPacket.a = entityID;
        spawnEntityPacket.b = gameprofile.getId();
        spawnEntityPacket.c = MathHelper.floor(location.getX() * 32.00D);
        spawnEntityPacket.d = MathHelper.floor(location.getY() * 32.00D);
        spawnEntityPacket.e = MathHelper.floor(location.getZ() * 32.00D);
        spawnEntityPacket.f = getFixRotation(location.getYaw());
        spawnEntityPacket.g = getFixRotation(location.getPitch());
        //spawnEntityPacket.h = 0;

        /*final DataWatcher w = new DataWatcher(null);
        w.a(6, (float) 20);
        w.a(10, (byte) 127);

        spawnEntityPacket.i = w;*/

        //fix head rotation
        /*entityLookPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(entityID, getFixRotation(location.getYaw()), getFixRotation(location.getPitch()), true);

        headRotationPacket.a = entityID;
        headRotationPacket.b = getFixRotation(location.getYaw());*/
    }

    public void spawn(final Player player) {

        //Add to ScoreboardTeam
        //player.sendPacket(packetPlayOutScoreboardTeam);

        //Add NPC to the tablist
        addToTablist(player);

        //Spawn the living entity (NPC)
        player.sendPacket(spawnEntityPacket);

        //Fix head rotation
        player.sendPacket(entityLookPacket);


        //Setup hand item
        if (equipementPacket.size() >= 1)
            for (final PacketPlayOutEntityEquipment equipementPacket : equipementPacket) {
                player.sendPacket(equipementPacket);
            }

        Bukkit.getScheduler().scheduleSyncDelayedTask(NPC.getPlugin(NPC.class), () -> {
            //Remove NPC from tablist
            removeFromTablist(player);
        }, 10 * 20L);
    }

    public void spawn() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            spawn(player);
        }
    }

    public void destroy() {

        for (final Player player : Bukkit.getOnlinePlayers()) {
            final PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entityID);

            removeFromTablist(player);
            player.sendPacket(packet);
        }

    }

    public void destroy(final Player player) {

        final PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entityID);

        removeFromTablist(player);
        player.sendPacket(packet);

    }

    public String getName() {
        return name;
    }

    public byte getFixRotation(float f) {
        return (byte) (f * 256.0F / 360.0F);
    }

    public int getEntityID() {
        return entityID;
    }

    public Location getLocation() {
        return location;
    }
}