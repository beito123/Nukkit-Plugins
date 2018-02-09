package ru.bk.beito3.hanabi.util;

public class Vec2 {
    public int x, y;

    public Vec2() {
    }

    public Vec2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vec2)) return false;
        Vec2 v = (Vec2) o;
        return x == v.x && y == v.y;
    }

    public int hashCode() {
        return x ^ 134 ^ y * 52497;
    }
}