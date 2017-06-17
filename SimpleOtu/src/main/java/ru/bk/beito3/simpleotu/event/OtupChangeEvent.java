package ru.bk.beito3.simpleotu.event;

import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.Plugin;

public class OtupChangeEvent extends OtuPluginEvent {

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
        return sender;
    }

    public void setSender(CommandSender sender) {
        this.sender = sender;
    }

    public Position getFrom() {
        return from;
    }

    public void setFrom(Position from) {
        this.from = from;
    }

    public Position getTo() {
        return to;
    }

    public void setTo(Position to) {
        this.to = to;
    }
}
