package com.besuper.npc.be.listeners;

import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerInteractNPCEvent extends Event {

    private final int id;
    private final PacketPlayInUseEntity.EnumEntityUseAction action;
    private final String name;
    private final Player player;

    public PlayerInteractNPCEvent(int id, PacketPlayInUseEntity.EnumEntityUseAction action, String name, Player p) {
        this.id = id;
        this.action = action;
        this.name = name;
        this.player = p;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Player getPlayer() {
        return player;
    }

    public PacketPlayInUseEntity.EnumEntityUseAction getAction() {
        return action;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
