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
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.data.Vector3fEntityData;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.SetEntityLinkPacket;

import java.util.Map;

public class Chair extends Entity implements EntityRideable {

    private static final byte SITTING_ACTION_ID = 2;
    private static final byte STAND_ACTION_ID = 3;

    private EntityMetadata defaultProperties = new EntityMetadata()
            .putLong(DATA_FLAGS,
                    (1L << DATA_FLAG_NO_AI) |
                            (1L << DATA_FLAG_INVISIBLE))
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
            this.close();
        }
    }

    @Override
    public void spawnTo(Player player) {
        AddEntityPacket pk = new AddEntityPacket();
        //pk.type = EntityEgg.NETWORK_ID;//specification in my server
        pk.type = EntityItem.NETWORK_ID;//invisible
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

        if (this.hasSat() && player != this.getRider()) {
            this.sendLinkPacket(player, SITTING_ACTION_ID);
        }

        super.spawnTo(player);

    }

    @Override
    public void close() {
        if (!this.closed) {
            if (this.getRider() != null) {
                this.standupSittingEntity();
            }
        }

        super.close();
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        namedTag.putByte("remove", 1);//Remove Flag
    }

    //

    public Entity getRider() {
        return this.linkedEntity;
    }
    public boolean sitEntity(Entity entity) {
        return this.sitEntity(entity, null);
    }

    public boolean sitEntity(Entity entity, Vector3f offset) {
        if (this.getRider() != null) {
            return false;
        }

        entity.setLinkedEntity(this);
        this.setLinkedEntity(entity);

        this.sendLinkPacketToAll(SITTING_ACTION_ID);

        if (this.getRider() instanceof Player) {
            this.sendLinkPacketToRider(SITTING_ACTION_ID, offset);
        }

        return true;
    }

    public boolean standupSittingEntity() {
        if (this.getRider() == null) {
            return false;
        }

        this.getRider().setLinkedEntity(null);
        this.setLinkedEntity(null);

        this.sendLinkPacketToAll(STAND_ACTION_ID);

        if (this.getRider() instanceof Player) {
            this.sendLinkPacketToRider(STAND_ACTION_ID, new Vector3f(0, 0.5F, 0));
        }

        return true;
    }

    public boolean hasSat() {
        Entity entity = this.getRider();
        return entity != null && entity.isAlive() && !entity.closed;
    }

    public boolean sendLinkPacket(Player player, byte type) {
        if (this.getRider() == null) {
            return false;
        }

        SetEntityLinkPacket pk = new SetEntityLinkPacket();
        pk.rider = this.getId();
        pk.riding = this.getRider().getId();
        pk.type = type;

        player.dataPacket(pk);

        return true;
    }

    public boolean sendLinkPacketToRider(byte type) {
        return sendLinkPacketToRider(type, null);
    }

    public boolean sendLinkPacketToRider(byte type, Vector3f offset) {
        if (!(this.getRider() instanceof Player)) {
            return false;
        }

        Player player = (Player) this.getRider();

        SetEntityLinkPacket pk = new SetEntityLinkPacket();
        pk.rider = this.getId();
        pk.riding = 0;
        pk.type = type;

        player.dataPacket(pk);

        this.setDataFlag(DATA_FLAGS, DATA_FLAG_SADDLED, true);
        player.setDataFlag(DATA_FLAGS, DATA_FLAG_RIDING, true);

        if (offset != null) {
            player.setDataProperty(new Vector3fEntityData(Entity.DATA_RIDER_SEAT_POSITION, offset));
        } else {
            player.setDataProperty(new Vector3fEntityData(Entity.DATA_RIDER_SEAT_POSITION, 0, 1F, 0));
        }

        return true;
    }

    public boolean sendLinkPacketToAll(byte type) {
        if (this.getRider() == null) {
            return false;
        }

        Map<Long, Player> players = this.getLevel().getPlayers();


        for (Map.Entry<Long, Player> entry : players.entrySet()) {
            sendLinkPacket(entry.getValue(), type);
        }

        return true;
    }
}
