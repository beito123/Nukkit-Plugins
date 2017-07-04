package ru.bk.beito3.sit;

/*
 * Sit
 *
 * Copyright (c) 2017 beito
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
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
import cn.nukkit.event.player.*;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainClass extends PluginBase implements Listener {

    private Map<String, Chair> usingChairs = new HashMap<>();

    private Map<String, Long> tempTap = new HashMap<>();

    private boolean decodeMode = false;

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
    public void onActionPacket(DataPacketReceiveEvent event) {
        if (event.getPacket().pid() == ProtocolInfo.PLAYER_ACTION_PACKET) {
            PlayerActionPacket pk = (PlayerActionPacket) event.getPacket();
            Player player = event.getPlayer();
            Server.getInstance().getLogger().debug("jagajaga: action:" + pk.action);

            if (pk.action == PlayerActionPacket.ACTION_JUMP) {
                if (player.getLinkedEntity() instanceof Chair) {
                    this.closeChair(player);
                }
            }
        }
    }

    @EventHandler
    public void onInvalidMove(PlayerInvalidMoveEvent event) {
        if(event.getPlayer().getLinkedEntity() instanceof Chair) {
            event.setCancelled();
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

                //y += 1.2;

                this.sitPlayer(player, new Vector3(x, y, z));

                player.sendTip(TextFormat.GOLD + "Jump to stand up" + TextFormat.RESET);//ummm...english...

                break;
        }

        return true;
    }

    private void sitPlayer(Player player, Vector3 pos) {
        sitPlayer(player, pos, null);
    }

    private void sitPlayer(Player player, Vector3 pos, Vector3f offset) {
        if (!player.isAlive() || player.closed) {
            return;
        }

        closeChair(player);

        if(player.isSleeping()) {
            player.stopSleep();
        } else if(player.isSneaking()) {
            player.setSneaking(false);
        }

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

        entity.sitEntity(player, offset);

        usingChairs.put(player.getName(), entity);
    }

    private void sitPlayerOnBlock(Player player, Block block) {
        Double x = block.getX() + 0.5;
        Double y = block.getY();
        Double z = block.getZ() + 0.5;

        if (block instanceof BlockStairs) {
            y += 1.56;
        } else {
            y += 1.16;
        }

        Vector3 pos = new Vector3(x, y, z);

        this.sitPlayer(player, pos, new Vector3f(0, 0.5F, 0));
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
