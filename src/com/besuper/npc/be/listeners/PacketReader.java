package com.besuper.npc.be.listeners;

import com.besuper.npc.be.NPC;
import com.besuper.npc.be.entity.NPCEntity;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class PacketReader {

    private final Player player;
    private Channel channel;

    public PacketReader(Player player) {
        this.player = player;
    }

    public void inject() {
        channel = ((CraftPlayer) this.player).getHandle().playerConnection.networkManager.channel;

        channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<Packet<?>>() {

            @Override
            protected void decode(ChannelHandlerContext arg0, Packet<?> packet, List<Object> arg2) throws Exception {
                arg2.add(packet);
                readPacket(packet);
            }

        });
    }

    public void uninject() {
        if (channel.pipeline().get("PacketInjector") != null) {
            channel.pipeline().remove("PacketInjector");
        }
    }

    public void readPacket(Packet<?> packet) {
        if (packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInUseEntity")) {

            final int id = ((PacketPlayInUseEntity) packet).a;

            final PacketPlayInUseEntity.EnumEntityUseAction action = ((PacketPlayInUseEntity) packet).action;

            for (NPCEntity npc : NPC.npcs.values()) {
                if (npc.getEntityID() == id && npc.getAuthorized_interact().contains(action)) {
                    Bukkit.getServer().getPluginManager().callEvent(new PlayerInteractNPCEvent(id, action, npc.getName(), player));
                    break;
                }
            }

        }
    }

}
