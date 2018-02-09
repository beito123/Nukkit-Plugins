package ru.bk.beito3.hanabi;

import cn.nukkit.level.particle.Particle;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LevelEventPacket;
import cn.nukkit.utils.BlockColor;

public class TestParticle extends Particle {
    protected final int data;
    protected int id = 0;

    public TestParticle(Vector3 pos, int id) {
        this(pos, id, 0);
    }

    public TestParticle(Vector3 pos, int id, int data) {
        super(pos.x, pos.y, pos.z);
        this.id = id;
        this.data = data;
    }

    public TestParticle(Vector3 pos, BlockColor color) {
        super(pos.x, pos.y, pos.z);
        this.id = TYPE_SPLASH;
        this.data = (0xff << 24) | ((color.getRed() & 0xff) << 16) | ((color.getGreen() & 0xff) << 8) | (color.getBlue() & 0xff);
    }

    @Override
    public DataPacket[] encode() {
        LevelEventPacket pk = new LevelEventPacket();
        pk.evid = (short) (LevelEventPacket.EVENT_ADD_PARTICLE_MASK | TYPE_MOB_SPELL);
        pk.x = (float) this.x;
        pk.y = (float) this.y;
        pk.z = (float) this.z;
        pk.data = this.data;

        return new DataPacket[]{pk};
    }
}
