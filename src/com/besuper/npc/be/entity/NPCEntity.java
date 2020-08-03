package com.besuper.npc.be.entity;

import com.besuper.npc.be.NPC;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class NPCEntity {

    // NPC informations
    private final int entityID;
    private GameProfile gameprofile;
    private final Location location = null;
    private final String name;

    // Skin
    private final Boolean use_player_skin;
    private final String value;
    private final String signature;

    // Name displayer
    private final List<String> display_name;
    private final ArrayList<Integer> amorstand_ids = new ArrayList<>();

    // Authorized interaction
    private final HashSet<PacketPlayInUseEntity.EnumEntityUseAction> authorized_interact = new HashSet<>();

    // NPC variables
    private final HashMap<String, Object> variables = new HashMap<>();

    // Packets
    private final PacketPlayOutNamedEntitySpawn spawnEntityPacket = new PacketPlayOutNamedEntitySpawn();
    ;
    private final PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam();
    private PacketPlayOutEntity.PacketPlayOutEntityLook entityLookPacket;
    private final PacketPlayOutEntityHeadRotation headRotationPacket = new PacketPlayOutEntityHeadRotation();
    private final List<PacketPlayOutEntityEquipment> equipementPacket = new ArrayList<>();

    //Players view distance
    private final List<String> hidden_players = new ArrayList();

    public NPCEntity(final String path) {
        final FileConfiguration config = YamlConfiguration.loadConfiguration(new File(path));

        this.entityID = (int) Math.ceil(Math.random() * 1000) + 2000;
        this.name = config.getString("name");
        this.value = config.getString("value");
        this.signature = config.getString("signature");

        /*this.location = new Location(config.getString("location.world"), config.getDouble("location.x"),
                config.getDouble("location.y"), config.getDouble("location.z"),
                Float.parseFloat("" + config.get("location.yaw")), Float.parseFloat("" + config.get("location.pitch")));*/

        this.display_name = config.getStringList("display_name");
        this.use_player_skin = config.getBoolean("use-player-skin");

        this.gameprofile = new GameProfile(UUID.randomUUID(), "§c ");

        if(!this.use_player_skin){
            this.gameprofile.getProperties().put("textures", new Property("textures", this.value, this.signature));
        }

        Collections.reverse(this.display_name);

        for (final String alls : config.getStringList("authorize-interact")) {
            authorized_interact.add(PacketPlayInUseEntity.EnumEntityUseAction.valueOf(alls));
        }

        setupPackets(config);

        //NPC tick
        Bukkit.getScheduler().runTaskTimer(NPC.plugin, () -> {

            for(final Player players : Bukkit.getOnlinePlayers()){

                final double distance = players.getLocation().distance(this.location);
                final String player_name = players.getName();

                if(distance >= 60 && !hidden_players.contains(player_name)){
                    this.destroy(players);
                    hidden_players.add(player_name);
                    System.out.println("Unload NPC");
                }else if(distance <= 59 && hidden_players.contains(player_name)){
                    hidden_players.remove(player_name);

                    this.spawn(players);
                    System.out.println("New load the NPC");
                }

            }

        },40,40);
    }

    public void setVariable(final String key, Object value) {
        if (variables.containsKey(key)) {
            variables.remove(key);
        }
        variables.put(key, value);
    }

    //Setup and spawn Amorstand to make the display name (Make sure amorstand are dead)
    private void display_name(final Player player) {

        if (this.display_name.size() == 0) {
            return;
        }

        if(this.display_name.get(0).length() <= 0){
            return;
        }

        if (this.display_name.size() == 1) {
            this.gameprofile.name = ChatColor.translateAlternateColorCodes('&', this.display_name.get(0));
            return;
        }

        double height = -0.15;

        for (String text : this.display_name) {

            if (text.startsWith("$")) {
                text = player.getMessage(text.replace("$", ""));
            } else {
                text = ChatColor.translateAlternateColorCodes('&', text);
            }

            if (text.contains("%")) {
                for (String keys : variables.keySet()) {
                    final String to_search = "%" + keys + "%";

                    if (text.contains(to_search)) {
                        text = text.replace(to_search, "" + variables.get(keys));
                    }
                }

                if (text.contains("%")) {
                    text = text.replaceAll("\\%(.*?)\\%", "N/A");
                }
            }

            final Location new_loc = this.location.clone();

            final EntityArmorStand stand = new EntityArmorStand(((CraftWorld) new_loc.getWorld()).getHandle());

            new_loc.add(0.0, height, 0.0);
            stand.setLocation(new_loc.getX(), new_loc.getY(), new_loc.getZ(), new_loc.getYaw(), new_loc.getPitch());
            stand.setCustomNameVisible(true);
            stand.setCustomName(text);
            stand.setGravity(false);
            stand.setSmall(false);
            stand.setBasePlate(false);
            stand.setHealth(1.0f);
            stand.setInvisible(true);

            player.sendPacket(new PacketPlayOutSpawnEntityLiving(stand));

            amorstand_ids.add(stand.getId());

            height += 0.30;
        }
    }

    private void addToTablist(final Player player) {
        final PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
        final PacketPlayOutPlayerInfo.PlayerInfoData data = packet.new PlayerInfoData(gameprofile, 1, EnumGamemode.CREATIVE, CraftChatMessage.fromString(gameprofile.getName())[0]);
        final List<PacketPlayOutPlayerInfo.PlayerInfoData> players = packet.b;
        players.add(data);

        packet.a = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER;
        packet.b = players;

        player.sendPacket(packet);

    }

    private void removeFromTablist(final Player player) {
        final PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
        final PacketPlayOutPlayerInfo.PlayerInfoData data = packet.new PlayerInfoData(gameprofile, 1, EnumGamemode.CREATIVE, CraftChatMessage.fromString(gameprofile.getName())[0]);
        final List<PacketPlayOutPlayerInfo.PlayerInfoData> players = packet.b;
        players.add(data);

        packet.a = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER;
        packet.b = players;

        player.sendPacket(packet);
    }

    private void setupPackets(final FileConfiguration config) {

        // Register team packet
        packetPlayOutScoreboardTeam.h = 0;
        packetPlayOutScoreboardTeam.b = name;
        packetPlayOutScoreboardTeam.a = name;
        packetPlayOutScoreboardTeam.e = "never";
        packetPlayOutScoreboardTeam.i = 1;
        packetPlayOutScoreboardTeam.c = ChatColor.BLUE + "[NPC] ";
        packetPlayOutScoreboardTeam.g.add(name);

        // Spawning Entity living packet
        spawnEntityPacket.a = entityID;
        spawnEntityPacket.b = gameprofile.getId();
        spawnEntityPacket.c = MathHelper.floor(location.getX() * 32.00D);
        spawnEntityPacket.d = MathHelper.floor(location.getY() * 32.00D);
        spawnEntityPacket.e = MathHelper.floor(location.getZ() * 32.00D);
        spawnEntityPacket.f = getFixRotation(location.getYaw());
        spawnEntityPacket.g = getFixRotation(location.getPitch());
        spawnEntityPacket.h = 0;

        final DataWatcher w = new DataWatcher(null);
        w.a(6, (float) 20);
        w.a(10, (byte) 127);

        spawnEntityPacket.i = w;

        //fix head rotation
        entityLookPacket = new PacketPlayOutEntityLook(entityID, getFixRotation(location.getYaw()), getFixRotation(location.getPitch()), true);

        headRotationPacket.a = entityID;
        headRotationPacket.b = getFixRotation(location.getYaw());

        //Equipement packet setup

        final List<String> equipements = config.getStringList("equipement");

        if (equipements != null && equipements.size() >= 1) {
            for (String s : equipements) {

                if (!s.contains(";")) {
                    continue;
                }

                final String[] splitted = s.split(";");
                final int slot = Integer.parseInt(splitted[0]);
                final String material = splitted[1];

                equipementPacket.add(new PacketPlayOutEntityEquipment(entityID, slot, CraftItemStack.asNMSCopy(new ItemStack(Material.valueOf(material)))));
            }
        }
    }

    public void spawn(final Player player) {

        if(this.use_player_skin){

            final Property property = ((CraftPlayer)player).getHandle().getProfile().getProperties().get("textures").iterator().next();
            this.gameprofile.getProperties().clear();
            this.gameprofile.getProperties().put("textures", new Property("textures", property.getValue(), property.getSignature()));

        }

        //Spawn display name
        display_name(player);

        //Add to ScoreboardTeam
        player.sendPacket(packetPlayOutScoreboardTeam);

        //Add NPC to the tablist
        addToTablist(player);

        //Spawn the living entity (NPC)
        player.sendPacket(spawnEntityPacket);

        //Fix head rotation
        player.sendPacket(entityLookPacket);
        player.sendPacket(headRotationPacket);


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

    public void updateHologram() {

        for (final Player player : Bukkit.getOnlinePlayers()) {
            for (final int i : amorstand_ids) {
                player.sendPacket(new PacketPlayOutEntityDestroy(i));
            }

            display_name(player);
        }
    }

    public void destroy() {

        for (final Player player : Bukkit.getOnlinePlayers()) {
            final PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entityID);

            removeFromTablist(player);
            player.sendPacket(packet);

            for (final int i : amorstand_ids) {
                player.sendPacket(new PacketPlayOutEntityDestroy(i));
            }
        }

    }

    public void destroy(final Player player) {

        final PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entityID);

        removeFromTablist(player);
        player.sendPacket(packet);

        for (final int i : amorstand_ids) {
            player.sendPacket(new PacketPlayOutEntityDestroy(i));
        }

    }

    public String getName() {
        return name;
    }

    public HashSet<PacketPlayInUseEntity.EnumEntityUseAction> getAuthorized_interact() {
        return authorized_interact;
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
