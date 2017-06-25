package ru.bk.beito3.fixbehavior.itemframe;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.ItemFrameDropItemPacket;

public class EventListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDataPacket(DataPacketReceiveEvent event) {
        if (event.getPacket().pid() == 0x47) {//ItemFrameDropPacket
            if (!(event.getPacket() instanceof ItemFrameDropItemPacket)) {
                return;
            }

            ItemFrameDropItemPacket pk = (ItemFrameDropItemPacket) event.getPacket();
            Player player = event.getPlayer();

            Vector3 pos = new Vector3(pk.x, pk.y, pk.z);

            Level level = player.getLevel();

            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof BlockEntityItemFrame) {
                BlockEntityItemFrame itemframe = (BlockEntityItemFrame) tile;

                if (itemframe.getItem().getId() == Item.AIR) {//Break the ItemFrame
                    level.useBreakOn(pos, player.getInventory().getItemInHand(), player, true);
                }
            }
        }
    }
}
