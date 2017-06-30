package ru.bk.beito3.simpleotu.event;

import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.Plugin;
import ru.bk.beito3.simpleotu.OtuEntry;

public class OturSetEvent extends OtuPluginEvent {

    private OtuEntry entry;

    private CommandSender sender;

    public OturSetEvent(Plugin plugin, OtuEntry entry) {
        this(plugin, entry, null);
    }

    public OturSetEvent(Plugin plugin, OtuEntry entry, CommandSender sender) {
        super(plugin);

        this.entry = entry;
        this.sender = sender;
    }


    public OtuEntry getEntry() {
        return this.entry;
    }

    public void setEntry(OtuEntry entry) {
        this.entry = entry;
    }

    public String getReason() {
        return this.entry.getReason();
    }

    public CommandSender getSender() {
        return this.sender;
    }
}
