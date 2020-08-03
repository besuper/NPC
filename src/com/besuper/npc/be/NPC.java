package com.besuper.npc.be;

import com.besuper.npc.be.entity.NPCEntity;
import com.besuper.npc.be.listeners.PlayerConnectionListeners;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;

public class NPC extends JavaPlugin {

    public static final HashMap<String, NPCEntity> npcs = new HashMap<>();
    public static NPC plugin;

    @Override
    public void onEnable() {
        plugin = this;

        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListeners(), this);

        final File search_folder = new File("plugins/NPC/");
        search_folder.mkdirs();

        for (File fs : search_folder.listFiles()) {
            if (fs.isFile()) {
                if (fs.exists() && fs.getName().contains(".yml")) {
                    final NPCEntity en = new NPCEntity(fs.getAbsolutePath());
                    npcs.put(en.getName(), en);
                }
            }
        }

        System.out.println("[NPC] " + npcs.size() + " npc loaded!");
    }

    @Override
    public void onDisable() {

        //Reload support
        for (NPCEntity npc : npcs.values()) {
            npc.destroy();
        }
    }

}
