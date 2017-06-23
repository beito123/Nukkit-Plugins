package ru.bk.beito3.fixbehavior.itemframe.block;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockFlowable;

public class TargetBlock extends Block {//Black hack :P

    private Block block = new BlockAir();

    protected TargetBlock(Integer meta, Block block) {
        this(block);
    }

    protected TargetBlock(Block block) {
        super(0);
        this.block = block;
        this.meta = block.getDamage();
    }

    @Override
    public String getName() {
        return "Target Block";
    }

    @Override
    public int getId() {
        return this.block.getId();
    }

    public Block getBlock() {
        return this.block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }


    public boolean isTransparent() {
        return this.block instanceof BlockFlowable;//TODO: improves judgment
    }
}
