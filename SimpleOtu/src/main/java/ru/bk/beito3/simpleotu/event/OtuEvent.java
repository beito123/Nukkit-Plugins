package ru.bk.beito3.simpleotu.event;

/*
 * SimpleOtu
 *
 * Copyright (c) 2017 beito
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
*/

import cn.nukkit.event.Cancellable;
import cn.nukkit.plugin.Plugin;
import ru.bk.beito3.simpleotu.OtuEntry;

public class OtuEvent extends OtuPluginEvent implements Cancellable {

    protected OtuEntry entry;

    public OtuEvent(Plugin plugin, OtuEntry entry) {
        super(plugin);

        this.entry = entry;
    }

    public OtuEntry getEntry() {
        return this.entry;
    }

    public void setEntry(OtuEntry entry) {
        this.entry = entry;
    }

    public int getMode() {
        return this.entry.getMode();
    }
}
