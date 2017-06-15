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
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.isOtu(player.getName()) || this.plugin.isRuna(player.getName())) {
            this.plugin.addActivePlayer(player.getName());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.isActivePlayer(player)) {
            this.plugin.removeActivePlayer(player.getName());
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.isActivePlayer(player)) {
            if(this.plugin.isOtu(player.getName()) || this.plugin.isRuna(player.getName())) {
                event.setCancelled();
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.isActivePlayer(player)) {
            if(this.plugin.isOtu(player.getName()) || this.plugin.isRuna(player.getName())) {
                event.setCancelled();
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.isActivePlayer(player)) {
            if(this.plugin.isOtu(player.getName()) || this.plugin.isRuna(player.getName())) {
                event.setCancelled();
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.isActivePlayer(player)) {
            if(this.plugin.isRuna(player.getName())) {
                event.setCancelled();
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageEvent event) {
        if(event instanceof EntityDamageByEntityEvent) {
            Entity entity = ((EntityDamageByEntityEvent) event).getDamager();
            if(!(entity instanceof Player)){
                return;
            }

            Player player = (Player) entity;
            if(this.plugin.isActivePlayer(player) && this.plugin.isRuna(player.getName())) {
                event.setCancelled();
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.isActivePlayer(player)) {
            if (this.plugin.isOtu(player.getName())) {
                event.setRespawnPosition(this.plugin.getJailPos());
            }
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if (player.isBanned() && this.plugin.isAutoRelease()) {
            if(this.plugin.isActivePlayer(player)) {
                if (this.plugin.isOtu(player.getName()) || this.plugin.isRuna(player.getName())) {
                    this.plugin.removeOtu(player.getName());
                    this.plugin.setRuna(player.getName(), false);
                    this.plugin.saveList();
                    this.plugin.broadcastCustomMessage("autorelease.notice", player.getName());
                }
            }
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.isActivePlayer(player)) {
            if(this.plugin.isOtu(player.getName()) || this.plugin.isRuna(player.getName())) {
                String msg = event.getMessage().toLowerCase();
                if(msg.charAt(0) == '/') {
                    switch(msg.split(" ")[0]) {
                        case "/otu":
                        case "/unotu":
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
}
