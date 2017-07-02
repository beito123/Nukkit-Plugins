package ru.bk.beito3.simpleotu.event;

/*
 * SimpleOtu
 *
 * Copyright (c) 2017 beito
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
*/

import cn.nukkit.plugin.Plugin;
import ru.bk.beito3.simpleotu.OtuEntry;

import java.time.OffsetDateTime;

public class OtuAddEvent extends OtuEvent {

    public OtuAddEvent(Plugin plugin, OtuEntry entry) {
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
