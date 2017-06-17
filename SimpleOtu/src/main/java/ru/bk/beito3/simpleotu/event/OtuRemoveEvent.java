package ru.bk.beito3.simpleotu.event;

import cn.nukkit.plugin.Plugin;
import ru.bk.beito3.simpleotu.OtuEntry;

public class OtuRemoveEvent extends OtuEvent {
    public OtuRemoveEvent(Plugin plugin, OtuEntry entry) {
        super(plugin, entry);
    }

    public String getName() {
        return this.entry.getName();
    }
}
