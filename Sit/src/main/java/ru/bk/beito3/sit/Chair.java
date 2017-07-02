package ru.bk.beito3.sit;

/*
 * Sit
 *
 * Copyright (c) 2017 beito
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
*/

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.SetEntityLinkPacket;

import java.util.Map;

public class Chair extends Entity {

    private static final byte SITTING_ACTION_ID = 2;
    private static final byte STAND_ACTION_ID = 3;

    private EntityMetadata defaultProperties = new EntityMetadata()
            .putLong(DATA_FLAGS,
                    1L << DATA_FLAG_NO_AI |
                            1L << DATA_FLAG_INVISIBLE)
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
        //pk.type = EntityEgg.NETWORK_ID;//
        pk.type = 64;//
        pk.entityUniqueId = this.getId();
        pk.entityRuntimeId = this.getId();
        pk.x = (float) this.x;
        pk.y = (float) this.y;
        pk.z = (float) this.z;
        pk.speedX = (float) this.motionX;
        pk.speedY = (float) this.motionY;
        pk.speedZ = (float) this.motionZ;
        pk.metadata = defaultProperties;

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

    //

    public Entity getSittingEntity() {
        return this.linkedEntity;
    }

    public boolean sitEntity(Entity entity) {
        if (getSittingEntity() != null) {
            return false;
        }

        entity.setLinkedEntity(this);
        setLinkedEntity(entity);

        sendLinkPacketToAll(SITTING_ACTION_ID);

        if (this.getSittingEntity() instanceof Player) {
            sendLinkPacketToSittingPlayer(SITTING_ACTION_ID);
        }

        return true;
    }

    public boolean standupSittingEntity() {
        if (getSittingEntity() == null) {
            return false;
        }

        this.getSittingEntity().setLinkedEntity(null);
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
