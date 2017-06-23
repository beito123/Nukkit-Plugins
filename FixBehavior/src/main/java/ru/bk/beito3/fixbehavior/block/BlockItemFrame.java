package ru.bk.beito3.fixbehavior.block;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockFlowable;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;

public class BlockItemFrame extends cn.nukkit.block.BlockItemFrame {//Black hack :P

    public BlockItemFrame() {
        this(0);
    }

    public BlockItemFrame(int meta) {
        super(meta);
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        return super.place(item, block, new TargetBlock(target), face, fx, fy, fz, player);
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (this.getSide(getFacing()) instanceof BlockFlowable) {
                this.level.useBreakOn(this);
                return type;
            }
        }

        return 0;
    }

    public Item toItem() {
        return new ru.bk.beito3.fixbehavior.item.ItemItemFrame();
    }
}


