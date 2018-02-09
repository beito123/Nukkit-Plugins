package ru.bk.beito3.hanabi.util;

import java.io.File;

public class Utils {

    public static String getNameWithoutExt(File file) {
        String name = file.getName();
        int i = name.lastIndexOf(".");
        if (i >= 0) {
            return name.substring(0, i);
        }

        return name;
    }

    public static boolean str2bool(String str) {
        boolean bool = false;

        switch (str.toLowerCase()) {
            case "true":
            case "1":
            case "on":
                bool = true;
        }

        return bool;
    }

    public static Integer str2int(String str) {
        Integer result;
        try {
            result = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            result = null;
        }

        return result;
    }

    public static Integer str2uint(String str) {
        Integer result;
        try {
            result = Integer.parseUnsignedInt(str);
        } catch (NumberFormatException e) {
            result = null;
        }

        return result;
    }

    public static Double str2double(String str) {
        Double result;
        try {
            result = Double.parseDouble(str);
        } catch (NumberFormatException e) {
            result = null;
        }

        return result;
    }
}
