package ru.bk.beito3.fixbehavior;

import cn.nukkit.block.Block;
import cn.nukkit.event.Listener;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemPotato;
import cn.nukkit.plugin.PluginBase;
import ru.bk.beito3.fixbehavior.block.BlockItemFrame;
import ru.bk.beito3.fixbehavior.item.ItemItemFrame;

import java.lang.reflect.Constructor;

public class MainClass extends PluginBase implements Listener {

    @Override
    public void onEnable() {
        this.fixPotatoBug();

        this.registerItem(new ItemItemFrame(), new BlockItemFrame(), true);
    }

    public void fixPotatoBug() {
        this.registerItem(new ItemPotato(), null, true);
    }

    public void registerItem(Item item, Block block, boolean force) {
        if (Item.list[item.getId()] != null && !force) {
            return;
        }

        Item.list[item.getId()] = item.getClass();

        if (block != null) {
            Block.list[block.getId()] = block.getClass();
            Item.list[block.getId()] = block.getClass();

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
}
