package ru.bk.beito3.hanabi.util;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinecraftUtils {

    private static final Pattern relReg = Pattern.compile("^~*(\\d+)$");

    public static Position str2pos(String str) {//Format: x, y, z, world
        Position pos = null;

        String[] s = str.replaceAll("\\s", "").split(",", 4);

        if (s.length >= 3) {
            Double x = Utils.str2double(s[0]);
            Double y = Utils.str2double(s[1]);
            Double z = Utils.str2double(s[2]);

            if (x == null || y == null || z == null) {
                return null;
            }

            Level level = Server.getInstance().getLevelByName(s[3]);//world
            if (level == null) {
                return null;
            }

            pos = new Position(x, y, z, level);
        }

        return pos;
    }

    public static String pos2str(Position pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ", " + pos.getLevel().getFolderName();
    }

    public static Vector3 toAbsVector3(Vector3 pos, String x, String y, String z) {
        Double ax = rel2abs(pos.x, x);
        Double ay = rel2abs(pos.y, y);
        Double az = rel2abs(pos.z, z);
        if (ax == null || ay == null || az == null) {
            return null;
        }

        return new Vector3(ax, ay, az);
    }

    public static Double rel2abs(double a, String str) {
        if (str.equals("~")) {
            return a;
        }

        Matcher m = relReg.matcher(str);
        if (m.find()) {
            Double d = Double.parseDouble(m.group(1));
            if (str.contains("~")) {
                return a + d;
            } else {
                return d;
            }
        }

        return null;
    }


    public static CompoundTag getPlayerCompoundTags(File file) {
        FileInputStream stream;
        try {
            stream = new FileInputStream(file);
        } catch (IOException e) {
            return null;
        }

        return getPlayerCompoundTags(stream);
    }

    public static CompoundTag getPlayerCompoundTags(FileInputStream stream) {
        CompoundTag tags;
        try {
            tags = NBTIO.readCompressed(stream);
        } catch (IOException e) {
            return null;
        }

        return tags;
    }


    public static BufferedImage getSkin(File file) {
        CompoundTag tags = MinecraftUtils.getPlayerCompoundTags(file);
        if (tags == null) {
            return null;
        }

        return MinecraftUtils.getSkin(tags);
    }

    public static BufferedImage getSkin(CompoundTag tags) {
        if (!tags.exist("Skin")) {
            return null;
        }

        CompoundTag stags = tags.getCompound("Skin");

        byte[] data;
        if (stags.exist("Data")) {
            data = stags.getByteArray("Data");
        } else if (stags.exist("skinData")) {
            data = stags.getByteArray("skinData");
        } else {
            return null;
        }

        return getSkin(data);
    }

    //Thanks: https://gist.github.com/sekjun9878/762dbcef367dd01e2b8e
    public static BufferedImage getSkin(byte[] data) {
        int height;
        int width;

        if (data.length == (64 * 32 * 4)) {
            height = 32;
            width = 64;
        } else if (data.length == (64 * 64 * 4)) {
            height = 64;
            width = 64;
        } else {
            return null;
        }

        int offset = 0;
        int r, g, b, a;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                r = data[offset++] & 0xFF;
                g = data[offset++] & 0xFF;
                b = data[offset++] & 0xFF;
                a = data[offset++] & 0xFF;

                //debug
                /*System.out.println("skinConvert:  y." + y + " x." + x
                    + " r." + r + " g." + g + " b." + b + " a." + a
                    + " c." + (a << 24 | r << 16 | g << 8 | b));*/

                image.setRGB(x, y, (a << 24 | r << 16 | g << 8 | b));
            }
        }

        return image;
    }

    public static BufferedImage getSkinFace(BufferedImage image) {
        if (image.getWidth() < 16 && image.getHeight() < 16) {
            return null;
        }

        return image.getSubimage(8, 8, 8, 8);
    }
}
