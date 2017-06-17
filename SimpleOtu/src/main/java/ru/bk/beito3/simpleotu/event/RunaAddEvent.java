package ru.bk.beito3.simpleotu.event;

import cn.nukkit.plugin.Plugin;
import ru.bk.beito3.simpleotu.OtuEntry;

import java.time.OffsetDateTime;

public class RunaAddEvent extends RunaEvent {

    public RunaAddEvent(Plugin plugin, OtuEntry entry) {
        super(plugin, entry);
    }

    public String getName() {
        return this.entry.getName();
    }

    public String getSource() {
        return this.entry.getSource();
    }

    public OffsetDateTime getCreationDate() {
        return this.entry.getCreationDate();
    }
}
