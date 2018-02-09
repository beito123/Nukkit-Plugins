package ru.bk.beito3.hanabi.util;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.ConfigSection;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomMessage {//Simple Custom Message

    private Map<String, String> messages;

    private String prefix;

    public CustomMessage() {
        this.messages = new LinkedHashMap<>();
    }

    public CustomMessage(ConfigSection section) {//for config
        Map<String, String> messages = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : section.entrySet()) {
            messages.put(e.getKey(), e.getValue().toString());
        }

        this.messages = messages;
    }

    public CustomMessage(Map<String, String> messages) {
        this.messages = messages;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String get(String key) {
        if (!this.exists(key)) {
            return null;
        }

        return this.messages.get(key);
    }

    public void set(String key, String message) {
        this.messages.put(key, message);
    }

    public void remove(String key) {
        if (this.exists(key)) {
            this.messages.remove(key);
        }
    }

    public boolean exists(String key) {
        return this.messages.containsKey(key);
    }

    public String getMessage(String key, Object... args) {
        return this.getMessage(key, false, args);
    }

    public String getMessage(String key, boolean noPrefix, Object... args) {
        if (!this.exists(key)) {
            return "NULL:" + key;
        }

        String msg = this.get(key);

        for (int i = 0; i < args.length; i++) {
            msg = msg.replace("{%" + i + "}", args[i].toString());
        }

        String prefix = this.messages.get("command.prefix");
        if (prefix != null && prefix.length() > 0 && !noPrefix) {
            msg = prefix + " " + msg;
        }

        return msg;
    }

    public void sendMessage(CommandSender sender, String key, Object... args) {
        sender.sendMessage(this.getMessage(key, false, args));
    }

    public void broadcastMessage(String key, Object... args) {
        Server.getInstance().broadcastMessage(this.getMessage(key, args));
    }

    public void broadcastMessage(CommandSender[] targets, String key, Object... args) {
        Server.getInstance().broadcastMessage(this.getMessage(key, args), targets);
    }

    public void broadcastMessage(Collection<CommandSender> targets, String key, Object... args) {
        Server.getInstance().broadcastMessage(this.getMessage(key, args), targets);
    }
}
