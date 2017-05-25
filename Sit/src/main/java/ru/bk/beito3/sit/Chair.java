package ru.bk.beito3.sit;

/*
 * Copyright (c) 2017 beito
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
*/

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.projectile.EntityEgg;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.SetEntityLinkPacket;

import java.util.Map;

public class Chair extends Entity {

    private static final byte SITTING_ACTION_ID = 2;
    private static final byte STAND_ACTION_ID = 3;

    private EntityMetadata DefaultProperties = new EntityMetadata()
            .putLong(DATA_FLAGS,
                    1 << DATA_FLAG_NO_AI |
                            1 << DATA_FLAG_INVISIBLE)
            .putString(DATA_NAMETAG, "");


    public Chair(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public float getEyeHeight() {
        return 0;
    }

    @Override
    public int getNetworkId() {
        return -1;
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        if (namedTag.exist("remove")) {
            close();
        }
    }

    @Override
    public void spawnTo(Player player) {
        AddEntityPacket pk = new AddEntityPacket();
        pk.type = EntityEgg.NETWORK_ID;//
        pk.entityUniqueId = this.getId();
        pk.entityRuntimeId = this.getId();
        pk.x = (float) this.x;
        pk.y = (float) this.y;
        pk.z = (float) this.z;
        pk.speedX = (float) this.motionX;
        pk.speedY = (float) this.motionY;
        pk.speedZ = (float) this.motionZ;
        pk.metadata = DefaultProperties;

        player.dataPacket(pk);

        if (this.hasSat() && player != this.getSittingEntity()) {
            this.sendLinkPacket(player, SITTING_ACTION_ID);
        }

        super.spawnTo(player);

    }

    @Override
    public void close() {
        if (!this.closed) {
            if (this.getSittingEntity() != null) {
                this.standupSittingEntity();
            }
        }

        super.close();
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        namedTag.putByte("remove", 1);
    }


    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        boolean hasUpdate = super.onUpdate(currentTick);

        if (this.age > 200) {//10s
            if (!hasSat()) {
                this.kill();
                hasUpdate = true;
            }
        }

        return hasUpdate;
    }

    //

    public Entity getSittingEntity() {
        return this.linkedEntity;
    }

    public boolean sitEntity(Entity entity) {
        if (getSittingEntity() != null) {
            return false;
        }

        setLinkedEntity(entity);

        sendLinkPacketToAll(SITTING_ACTION_ID);

        if (getSittingEntity() instanceof Player) {
            sendLinkPacketToSittingPlayer(SITTING_ACTION_ID);
        }

        return true;
    }

    public boolean standupSittingEntity() {
        if (getSittingEntity() == null) {
            return false;
        }

        setLinkedEntity(null);

        sendLinkPacketToAll(STAND_ACTION_ID);

        if (getSittingEntity() instanceof Player) {
            sendLinkPacketToSittingPlayer(STAND_ACTION_ID);
        }

        return true;
    }

    public boolean hasSat() {
        Entity entity = this.getSittingEntity();
        return entity != null && entity.isAlive() && !entity.closed;
    }

    public boolean sendLinkPacket(Player player, byte type) {
        if (getSittingEntity() == null) {
            return false;
        }

        SetEntityLinkPacket pk = new SetEntityLinkPacket();
        pk.rider = this.getId();
        pk.riding = getSittingEntity().getId();
        pk.type = type;

        player.dataPacket(pk);

        return true;
    }

    public boolean sendLinkPacketToSittingPlayer(byte type) {
        if (getSittingEntity() == null || !(getSittingEntity() instanceof Player)) {
            return false;
        }

        Player player = (Player) getSittingEntity();

        SetEntityLinkPacket pk = new SetEntityLinkPacket();
        pk.rider = this.getId();
        pk.riding = 0;
        pk.type = type;

        player.dataPacket(pk);

        player.setDataFlag(DATA_FLAGS, DATA_FLAG_RIDING, true);

        return true;
    }

    public boolean sendLinkPacketToAll(byte type) {
        if (getSittingEntity() == null) {
            return false;
        }

        Map<Long, Player> players = getLevel().getPlayers();

        for (Map.Entry<Long, Player> entry : players.entrySet()) {
            sendLinkPacket(entry.getValue(), type);
        }

        return true;
    }
}
