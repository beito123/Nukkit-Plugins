package ru.bk.beito3.fixbehavior.itemframe;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockItemFrame;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.ItemFrameDropItemPacket;

public class EventListener implements Listener {

    @EventHandler
    public void onDataPacket(DataPacketReceiveEvent event) {
        if (event.getPacket().pid() == 0x47) {//ItemFrameDropPacket
            if (!(event.getPacket() instanceof ItemFrameDropItemPacket)) {
                return;
            }

            ItemFrameDropItemPacket pk = (ItemFrameDropItemPacket) event.getPacket();
            Player player = event.getPlayer();

            Vector3 pos = new Vector3(pk.x, pk.y, pk.z);

            Level level = player.getLevel();

            Block block = level.getBlock(pos);
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof BlockEntityItemFrame && block instanceof BlockItemFrame) {
                BlockEntityItemFrame itemframe = (BlockEntityItemFrame) tile;

                if (itemframe.getItem().getId() == Item.AIR) {//Break the ItemFrame
                    level.useBreakOn(pos, player.getInventory().getItemInHand(), player, true);
                    return;
                }

                NukkitRandom random = new NukkitRandom();
                if ((random.nextRange(1, 100) / 100) <= itemframe.getItemDropChance()) {
                    BlockFace face = ((BlockItemFrame) block).getFacing();

                    Vector3 motion = null;
                    if (face != null) {
                        motion = new Vector3(
                                ((double) -face.getXOffset() / 10) + ((double) random.nextRange(-10, 10) / 100),
                                0.15,
                                ((double) -face.getZOffset() / 10) + ((double) random.nextRange(-10, 10) / 100)
                        );
                    }
                    level.dropItem(pos.add(0.5, 0.3, 0.5), itemframe.getItem(), motion);
                }

                itemframe.setItem(Item.get(Item.AIR));
                itemframe.setItemRotation(0);
            }
        }
    }
}
