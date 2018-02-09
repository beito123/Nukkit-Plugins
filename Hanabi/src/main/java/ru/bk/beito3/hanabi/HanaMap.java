package ru.bk.beito3.hanabi;

import cn.nukkit.Server;
import cn.nukkit.utils.BlockColor;
import ru.bk.beito3.hanabi.util.Vec3;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HanaMap {

    private Map<Vec3, BlockColor> map;

    public HanaMap() {
        map = new HashMap<>();
    }

    public HanaMap(Map<Vec3, BlockColor> map) {
        this.map = map;
    }

    public static HanaMap FromImage(BufferedImage image) {
        Map<Vec3, BlockColor> map = new HashMap<>();

        int r, g, b, a, pixel;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                pixel = image.getRGB(x, y);
                a = (pixel >> 24) & 0xff;//alpha
                r = (pixel >> 16) & 0xff;
                g = (pixel >> 8) & 0xff;
                b = pixel & 0xff;

                if (a > 0) {
                    map.put(new Vec3(x, y, 0), new BlockColor(r, g, b));
                }
            }
        }

        return new HanaMap(map);
    }

    public static HanaMap FromRGBList(List<List<List<Integer>>> list) {
        Map<Vec3, BlockColor> map = new HashMap<>();
        for (int x = 0; x < list.size(); x++) {
            List<List<Integer>> ylist = list.get(x);
            for (int y = 0; y < ylist.size(); y++) {
                List<Integer> zlist = ylist.get(y);
                for (int z = 0; z < zlist.size(); z++) {
                    Integer color = zlist.get(z);
                    if (color == null || ((color >> 24) & 0xff) != 0xff) {
                        continue;
                    }

                    map.put(new Vec3(x, y, z), new BlockColor(color));
                }
            }
        }

        return new HanaMap(map);
    }

    public Map<Vec3, BlockColor> getMap() {
        return this.map;
    }

    public int getMaxX() {
        int x = 0;
        for (Map.Entry<Vec3, BlockColor> e : this.map.entrySet()) {
            int ax = e.getKey().x;
            if (ax > x) {
                x = ax;
            }
        }

        return x;
    }


    public int getMaxY() {
        int y = 0;
        for (Map.Entry<Vec3, BlockColor> e : this.map.entrySet()) {
            int ay = e.getKey().y;
            if (ay > y) {
                y = ay;
            }
        }

        return y;
    }

    public int getMaxZ() {
        int z = 0;
        for (Map.Entry<Vec3, BlockColor> e : this.map.entrySet()) {
            int az = e.getKey().z;
            if (az > z) {
                z = az;
            }
        }

        return z;
    }

    public List<List<List<Integer>>> toRGBList() {
        List<List<List<Integer>>> list = new ArrayList<>();

        Server.getInstance().getLogger().notice("test:" + this.map.toString());

        int maxX = this.getMaxX();
        int maxY = this.getMaxY();
        int maxZ = this.getMaxZ();
        for (int x = 0; x <= maxX; x++) {
            List<List<Integer>> ylist = new ArrayList<>();
            for (int y = 0; y <= maxY; y++) {
                List<Integer> zlist = new ArrayList<>();
                for (int z = 0; z <= maxZ; z++) {
                    Vec3 pos = new Vec3(x, y, z);
                    if (!this.map.containsKey(pos)) {
                        zlist.add(null);
                        continue;
                    }

                    zlist.add(this.map.get(pos).getRGB() | 0xff000000);
                }
                ylist.add(zlist);
            }
            list.add(ylist);
        }

        Server.getInstance().getLogger().notice("test:" + list.toString());

        return list;
    }


    public List<List<List<BlockColor>>> toList() {
        List<List<List<BlockColor>>> list = new ArrayList<>();

        int maxX = this.getMaxX();
        int maxY = this.getMaxY();
        int maxZ = this.getMaxZ();
        for (int x = 0; x <= maxX; x++) {
            List<List<BlockColor>> zlist = new ArrayList<>();
            for (int z = 0; z <= maxZ; z++) {
                List<BlockColor> ylist = new ArrayList<>();
                for (int y = 0; y <= maxY; y++) {
                    Vec3 pos = new Vec3(x, y, z);
                    if (!this.map.containsKey(pos)) {
                        zlist.add(null);
                        continue;
                    }

                    ylist.add(this.map.get(pos));
                }
                zlist.add(ylist);
            }
            list.add(zlist);
        }

        return list;
    }
}
