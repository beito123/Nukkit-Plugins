package ru.bk.beito3.simpleotu

/*
 * SimpleOtu
 *
 * Copyright (c) 2017 beito
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
*/

import cn.nukkit.Server
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.level.Position
import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.Config
import ru.bk.beito3.simpleotu.utils.NukkitUtils
import java.io.File

class MainClass : PluginBase() {

    companion object {
        const val CUSTOM_MESSAGES_VERSION = 15
    }

    val DEFAULT_TIMEZONE = "Asia/Tokyo"

    var jailPos: Position = Position()
    var autoRelease = false
    var noticeAdd = false
    var noticeRemove = false
    var enableEventTarget = false
    var enableUnotu = false
    var timezone = DEFAULT_TIMEZONE


    override fun onEnable() {
        if (this.dataFolder.exists()) {
            this.dataFolder.mkdirs()
        }

        this.saveResource("messages.properties")
        this.saveResource("messages-en.properties")

        //load Config
        var config = Config(File(this.dataFolder, "config.yml"), Config.YAML)
        config.setDefault(linkedMapOf<String, Any>(
                "jail-pos" to "0,0,0,world",
                "auto-release" to true,
                "notice-add" to true,
                "notice-remove" to true,
                "enable-event-target" to true,
                "enable-unotu" to true,
                "timezone" to DEFAULT_TIMEZONE
        ))

        var jail = NukkitUtils.str2pos(config.getString("jail-pos"))
        if (jail !== null) {
            this.jailPos = jail
        } else {
            this.jailPos = Server.getInstance().defaultLevel.safeSpawn
        }

        this.autoRelease = config.getBoolean("auto-release")
        this.noticeAdd = config.getBoolean("notice-add")
        this.noticeRemove = config.getBoolean("notice-remove")
        this.enableEventTarget = config.getBoolean("enable-event-target")
        this.enableUnotu = config.getBoolean("enable-unotu")
        this.timezone = config.getString("timezone")

        Server.getInstance().logger.notice("Hi! Jagajaga!")
    }

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        when (command?.name?.toLowerCase()) {
            "otu" -> {
                //
            }
            "unotu" -> {
                //
            }
        }

        return true
    }
}