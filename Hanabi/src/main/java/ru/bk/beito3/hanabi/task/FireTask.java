package ru.bk.beito3.hanabi.task;

import cn.nukkit.level.Position;
import cn.nukkit.scheduler.PluginTask;
import ru.bk.beito3.hanabi.HanaMap;
import ru.bk.beito3.hanabi.MainClass;

public class FireTask extends PluginTask<MainClass> {

    private HanaMap map;
    private Position pos;
    private int r;
    private boolean sound;

    public FireTask(MainClass plugin, HanaMap map, Position pos, int r, boolean sound) {
        super(plugin);

        this.map = map;
        this.pos = pos;
        this.r = r;
        this.sound = sound;
    }

    public void onRun(int tick) {
        this.getOwner().fire(this.map, this.pos, this.r, this.sound);
    }
}
