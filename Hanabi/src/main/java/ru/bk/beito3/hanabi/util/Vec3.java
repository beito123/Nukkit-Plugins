package ru.bk.beito3.hanabi.util;

public class Vec3 {
    public int x, y, z;

    public Vec3() {
    }

    public Vec3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vec3)) return false;
        Vec3 v = (Vec3) o;
        return x == v.x && y == v.y && z == v.z;
    }

    public int hashCode() {
        return x ^ y * 134 ^ z * 52497;
    }
}