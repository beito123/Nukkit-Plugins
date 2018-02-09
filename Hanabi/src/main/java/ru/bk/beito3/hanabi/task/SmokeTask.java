package ru.bk.beito3.hanabi.task;

import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.ExplodeParticle;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.scheduler.PluginTask;
import ru.bk.beito3.hanabi.MainClass;

public class SmokeTask extends PluginTask<MainClass> {

    private Position pos;
    private int count;
    private NukkitRandom random;

    public SmokeTask(MainClass plugin, Position pos, int count, NukkitRandom random) {
        super(plugin);

        this.pos = pos;
        this.count = count;
        this.random = random;
    }

    public void onRun(int tick) {
        Level level = pos.level;

        int xd = 3 * 100;
        int yd = 3 * 100;
        int zd = 3 * 100;

        for (int i = 0; i < count; i++) {
            /*level.addParticle(new DustParticle(pos.add(
                random.nextSignedFloat() * xd,
                random.nextSignedFloat() * yd,
                random.nextSignedFloat() * zd
            ), BlockColor.BLACK_BLOCK_COLOR));*/
            level.addParticle(new ExplodeParticle(pos));
        }
    }
}
