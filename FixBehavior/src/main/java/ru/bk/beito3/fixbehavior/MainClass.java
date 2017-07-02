package ru.bk.beito3.fixbehavior;

/*
 * FixBehavior
 *
 * Copyright (c) 2017 beito
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
*/

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.event.Listener;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemPotato;
import cn.nukkit.plugin.PluginBase;
import ru.bk.beito3.fixbehavior.itemframe.block.BlockItemFrame;
import ru.bk.beito3.fixbehavior.itemframe.item.ItemItemFrame;

import java.lang.reflect.Constructor;

public class MainClass extends PluginBase implements Listener {

    private final static boolean ENABLE_ITEMFRAME = true;
    private final static boolean ENABLE_POTATO = true;

    @Override
    public void onEnable() {
        if (ENABLE_POTATO) {
            this.fixPotatoBug();
        }

        if (ENABLE_ITEMFRAME) {
            this.registerItem(new ItemItemFrame(), new BlockItemFrame(), true);
            this.registerEvents(new ru.bk.beito3.fixbehavior.itemframe.EventListener());
        }
    }

    private void fixPotatoBug() {
        if (Item.list[Item.POTATO] == null) {
            this.registerItem(new ItemPotato(), null, true);
        }
    }

    private void registerItem(Item item, Block block, boolean force) {
        if (Item.list[item.getId()] != null && !force) {
            return;
        }

        Item.list[item.getId()] = item.getClass();

        if (block != null) {
            Item.list[block.getId()] = block.getClass();
            Block.list[block.getId()] = block.getClass();

            Block.solid[block.getId()] = block.isSolid();
            Block.transparent[block.getId()] = block.isTransparent();
            Block.hardness[block.getId()] = block.getHardness();
            Block.light[block.getId()] = block.getLightLevel();
            Block.lightFilter[block.getId()] = 1;//


            Class<? extends Block> c = block.getClass();
            try {
                Constructor constructor = c.getDeclaredConstructor(int.class);
                constructor.setAccessible(true);
                for (int i = 0; i < 16; i++) {
                    Block.fullList[(Block.ITEM_FRAME_BLOCK << 4) | i] = (Block) constructor.newInstance(i);
                }
            } catch (Exception e) {
                this.getLogger().error("Could not register a block to nukkit.block.Block", e);
            }
        }
    }

    private void registerEvents(Listener listener) {
        Server.getInstance().getPluginManager().registerEvents(listener, this);
    }
}
