package com.besuper.npc.be.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.besuper.npc.be.NPC;
import com.besuper.npc.be.entity.NPCEntity;

public class PlayerConnectionListeners implements Listener{

	@EventHandler
	public void join(PlayerJoinEvent e) {
		
		final Player p = e.getPlayer();
		
		final PacketReader pr = new PacketReader(p);
        pr.inject();

		Bukkit.getScheduler().scheduleSyncDelayedTask(NPC.plugin, ()-> {
			for(NPCEntity npc : NPC.npcs.values()) {
				npc.spawn(p);
			}
		}, 10L);
	}
	
}
