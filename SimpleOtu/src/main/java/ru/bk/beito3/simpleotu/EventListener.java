package ru.bk.beito3.simpleotu;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.*;

public class EventListener implements Listener {

    private MainClass plugin;

    public EventListener(MainClass plugin) {
        this.plugin = plugin;
    }

    public MainClass getPlugin() {
        return this.plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.isOtued(player.getName()) | this.plugin.isRunaed(player.getName())) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.isOtued(player.getName()) | this.plugin.isRunaed(player.getName())){
            event.setCancelled();
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.isOtued(player.getName()) | this.plugin.isRunaed(player.getName())){
            event.setCancelled();
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.isRunaed(player.getName())){
            event.setCancelled();
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageEvent event) {
        if(event instanceof EntityDamageByEntityEvent) {
            Entity entity = ((EntityDamageByEntityEvent) event).getDamager();
            if(entity instanceof Player && this.plugin.isRunaed(entity.getName())){
                event.setCancelled();
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.isOtued(player.getName())){
            event.setRespawnPosition(this.plugin.getJailPos());
        }
    }

    /*
    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if(this.isOtued(player.getName()) | this.isRunaed(player.getName())){
            event.setCancelled();
        }
    }
    */

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if (player.isBanned() && this.plugin.isAutoRelease()) {
            if(this.plugin.isOtued(player) || this.plugin.isRunaed(player)) {
                this.plugin.removeOtu(player);
                this.plugin.removeRuna(player);
                this.plugin.saveList();
                this.plugin.broadcastCustomMessage("autorelease.notice", player.getName());
            }
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.isOtued(player.getName()) | this.plugin.isRunaed(player.getName())) {
            String msg = event.getMessage().toLowerCase();
            if(msg.charAt(0) == '/') {
                switch(msg.split(" ")[0]) {
                    case "/otu":
                    case "/runa":
                    case "/register":
                    case "/login":
                        break;
                    default:
                        this.plugin.sendCustomMessage(player, "otu.limit.command");
                        event.setCancelled();
                }
            }
        }
    }
}
