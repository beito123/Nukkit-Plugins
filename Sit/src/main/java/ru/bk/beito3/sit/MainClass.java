package ru.bk.beito3.sit;

/*
 * Copyright (c) 2017 beito
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
*/

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockStairs;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDespawnEvent;
import cn.nukkit.event.player.PlayerBedEnterEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.InteractPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainClass extends PluginBase implements Listener {

    private Map<String, Chair> usingChairs = new HashMap<>();

    private Map<String, Long> tempTap = new HashMap<>();

    @Override
    public void onEnable() {
        //Fix description and parameters of the command
        Command sitCommand = Server.getInstance().getCommandMap().getCommand("sit");
        sitCommand.setCommandParameters(new LinkedHashMap<String, CommandParameter[]>() {
        });
        sitCommand.setDescription("その場に座ります。");//and translate to jpn

        //Register a entity
        Entity.registerEntity("Chair", Chair.class, true);

        //Register event listener
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        for (Map.Entry<String, Chair> entry : usingChairs.entrySet()) {
            entry.getValue().close();
        }
    }

    @EventHandler
    public void onDespawn(EntityDespawnEvent event) {
        if (event.getEntity() instanceof Player) {
            closeChair((Player) event.getEntity());
        }
    }

    @EventHandler
    public void onDead(PlayerDeathEvent event) {
        closeChair(event.getEntity());
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        closeChair(event.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        closeChair(event.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (this.usingChairs.containsKey(event.getPlayer().getName())) {
            Player player = event.getPlayer();

            if (this.tempTap.containsKey(player.getName())) {//check tempTap
                long diff = System.currentTimeMillis() - this.tempTap.get(player.getName());
                if (diff <= 1000 * 0.8) {//0.8s
                    Block block = event.getBlock();
                    if (block instanceof BlockStairs && (block.getDamage() & 0x04) == 0) {
                        this.sitPlayerOnBlock(player, block);

                        player.sendTip(TextFormat.GOLD + "Jump to stand up" + TextFormat.RESET);//ummm...english...
                    }
                }
                this.tempTap.remove(player.getName());
            } else {
                tempTap.put(player.getName(), System.currentTimeMillis());//adds time to tempTap
            }
        }
    }

    @EventHandler
    public void onInteractPacket(DataPacketReceiveEvent event) {
        if (event.getPacket().pid() == ProtocolInfo.INTERACT_PACKET) {
            InteractPacket pk = (InteractPacket) event.getPacket();
            Player player = event.getPlayer();

            Entity target = player.getLevel().getEntity(pk.target);
            if (target instanceof Chair) {
                byte action = pk.action;
                if (action == InteractPacket.ACTION_LEFT_CLICK || action == InteractPacket.ACTION_VEHICLE_EXIT) {
                    this.closeChair(player);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName()) {
            case "sit":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(TextFormat.RED + "Please run the command in-game." + TextFormat.RESET);
                    return true;
                }

                Player player = (Player) sender;

                double x = player.getX();
                double y = player.getY();
                double z = player.getZ();

                y += 1.2;

                this.sitPlayer(player, new Vector3(x, y, z));

                player.sendTip(TextFormat.GOLD + "Jump to stand up" + TextFormat.RESET);//ummm...english...

                break;
        }

        return true;
    }

    private void sitPlayer(Player player, Vector3 pos) {
        if (!player.isAlive() || player.closed) {
            return;
        }

        closeChair(player);

        CompoundTag nbt = new CompoundTag()
                .putList(new ListTag<DoubleTag>("Pos")
                        .add(new DoubleTag("", pos.getX()))
                        .add(new DoubleTag("", pos.getY()))
                        .add(new DoubleTag("", pos.getZ())))
                .putList(new ListTag<DoubleTag>("Motion")
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0)))
                .putList(new ListTag<FloatTag>("Rotation")
                        .add(new FloatTag("", 0))
                        .add(new FloatTag("", 0)));

        Chair entity = (Chair) Entity.createEntity("Chair", player.chunk, nbt);

        entity.spawnToAll();

        entity.sitEntity(player);

        usingChairs.put(player.getName(), entity);
    }

    private void sitPlayerOnBlock(Player player, Block block) {
        Double x = block.getX() + 0.5;
        Double y = block.getY();
        Double z = block.getZ() + 0.5;

        if (block instanceof BlockStairs) {
            y += 1.65;
        } else {
            y += 1.2;
        }

        Vector3 pos = new Vector3(x, y, z);

        this.sitPlayer(player, pos);
    }

    private void closeChair(Player player) {
        closeChair(player.getName());
    }

    private void closeChair(String name) {
        if (usingChairs.containsKey(name)) {
            usingChairs.get(name).close();
            usingChairs.remove(name);
        }
        if (this.tempTap.containsKey(name)) {
            this.tempTap.remove(name);
        }
    }
}
