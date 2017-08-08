package ru.bk.beito3.simpleotu.event;

/**
 * SimpleOtu
 * <p>
 * Copyright (c) 2017 beito
 * <p>
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

import cn.nukkit.command.CommandSender;
import cn.nukkit.event.Cancellable;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.Plugin;

public class OtupChangeEvent extends OtuPluginEvent implements Cancellable {

    private CommandSender sender;
    private Position from;
    private Position to;

    public OtupChangeEvent(Plugin plugin, Position from, Position to, CommandSender sender) {
        super(plugin);

        this.from = from;
        this.to = to;
        this.sender = sender;
    }

    public CommandSender getSender() {
        return this.sender;
    }

    public void setSender(CommandSender sender) {
        this.sender = sender;
    }

    public Position getFrom() {
        return this.from;
    }

    public void setFrom(Position from) {
        this.from = from;
    }

    public Position getTo() {
        return this.to;
    }

    public void setTo(Position to) {
        this.to = to;
    }
}
