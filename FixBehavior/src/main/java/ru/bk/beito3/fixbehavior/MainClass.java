package ru.bk.beito3.fixbehavior;

import cn.nukkit.block.Block;
import cn.nukkit.event.Listener;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemPotato;
import cn.nukkit.plugin.PluginBase;

public class MainClass extends PluginBase implements Listener {

    @Override
    public void onEnable() {
        Item.list[Item.POTATO] = ItemPotato.class;
        Item.list[Item.POTATO_BLOCK] = Block.list[Item.POTATO_BLOCK];
    }
}
