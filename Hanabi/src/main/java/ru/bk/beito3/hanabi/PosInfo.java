package ru.bk.beito3.hanabi;

import cn.nukkit.level.Position;

public class PosInfo {
    public Position pos;
    public int minHeight = 20;
    public int maxHeight = 25;
    public int angle = -1;
    public boolean sound = false;

    public PosInfo() {
    }


    public PosInfo(Position pos) {
        this.pos = pos;
    }

    public PosInfo(Position pos, int height) {
        this.pos = pos;
        this.minHeight = height;
        this.maxHeight = height;
    }

    public PosInfo(Position pos, int minHeight, int maxHeight) {
        this.pos = pos;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

    public PosInfo(Position pos, int minHeight, int maxHeight, int angle) {
        this.pos = pos;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.angle = angle;
    }

    public PosInfo(String name, Position pos, int minHeight, int maxHeight, int angle, boolean sound) {
        this.pos = pos;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.angle = angle;
        this.sound = sound;
    }
}
