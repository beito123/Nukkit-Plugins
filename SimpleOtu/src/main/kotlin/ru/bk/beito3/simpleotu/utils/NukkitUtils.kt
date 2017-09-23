package ru.bk.beito3.simpleotu.utils

import cn.nukkit.Server
import cn.nukkit.level.Level
import cn.nukkit.level.Position

object NukkitUtils {

    fun str2pos(str: String): Position? {
        var pos = Position()

        var s = str.split(",")
        if (s.size >= 3) {
            try {
                pos.setComponents(
                        s[0].toDouble(),
                        s[1].toDouble(),
                        s[2].toDouble()
                )
            } catch (e: NumberFormatException) {
                return null
            }

            var level: Level = Server.getInstance().defaultLevel
            if (s.size >= 4) {
                level = Server.getInstance().getLevelByName(s[4])
            }

            pos.setLevel(level)
        }

        return pos
    }
}