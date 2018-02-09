package ru.bk.beito3.hanabi.task;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.sound.LevelEventSound;
import cn.nukkit.scheduler.PluginTask;
import ru.bk.beito3.hanabi.MainClass;

import java.util.Map;

public class SoundTestTask extends PluginTask<MainClass> {

    private int id;

    public SoundTestTask(MainClass plugin, int id) {
        super(plugin);

        this.id = id;
    }

    public void onRun(int tick) {
        Level level = Server.getInstance().getDefaultLevel();
        for (Map.Entry<Long, Player> e : level.getPlayers().entrySet()) {
            level.addSound(new LevelEventSound(e.getValue(), this.id));
        }

        this.owner.getLogger().notice("SoundTest: " + this.id);
    }
}
