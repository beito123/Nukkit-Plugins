package ru.bk.beito3.fixbehavior.item;

import ru.bk.beito3.fixbehavior.block.BlockItemFrame;

public class ItemItemFrame extends cn.nukkit.item.ItemItemFrame {

    public ItemItemFrame() {
        this(0, 1);
    }


    public ItemItemFrame(Integer meta, int count) {
        super(meta, count);
        this.block = new BlockItemFrame();
    }
}
