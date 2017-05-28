package ru.bk.beito3.simpleotu;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.event.Listener;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class MainClass extends PluginBase implements Listener {

    private static int CUSTOM_MESSAGES_VERSION = 4;

    private Map<String, String> messages = new HashMap<>();

    private Position jailPos;
    private boolean autoRelease = false;
    private boolean noticeAdd = false;
    private boolean noticeRemove = false;


    private List<String> otuPlayers = new ArrayList<>();
    private List<String> runaPlayers = new ArrayList<>();


    public Position getJailPos() {
        return this.jailPos;
    }

    public void setJailPos(Position pos) {
        this.jailPos = pos;
    }

    public List<String> getOtuPlayers() {
        return this.otuPlayers;
    }

    public void addOtu(Player player) {
        otuPlayers.add(player.getName());
    }

    public void addOtu(String name) {
        otuPlayers.add(name);
    }

    public boolean removeOtu(Player player) {
        return otuPlayers.remove(player);
    }

    public boolean removeOtu(String name) {
        return otuPlayers.remove(name);
    }

    public boolean isOtued(Player player) {
        return this.isOtued(player.getName());
    }

    public boolean isOtued(String name) {
        return otuPlayers.contains(name);
    }

    public List<String> getRunaPlayers() {
        return this.runaPlayers;
    }

    public void addRuna(Player player) {
        runaPlayers.add(player.getName());
    }

    public void addRuna(String name) {
        runaPlayers.add(name);
    }

    public boolean removeRuna(Player player) {
        return runaPlayers.remove(player.getName());
    }

    public boolean removeRuna(String name) {
        return runaPlayers.remove(name);
    }

    public boolean isRunaed(Player player) {
        return this.isRunaed(player.getName());
    }

    public boolean isRunaed(String name) {
        return runaPlayers.contains(name);
    }

    public boolean isAutoRelease() {
        return this.autoRelease;
    }

    public boolean isNoticeAdd() {
        return this.noticeAdd;
    }

    public boolean isNoticeRemove() {
        return this.noticeRemove;
    }

    //Enable/D

    @Override
    public void onEnable() {
        if(!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
        }

        this.saveResource("messages.properties");

        //load config
        Config config = new Config(new File(this.getDataFolder(), "config.yml"), Config.YAML);
        config.setDefault(new ConfigSection(new LinkedHashMap<String, Object>() {
            {
                put("jail-pos", "0,0,0,world");
                put("auto-release", true);
                put("notice-add", true);
                put("notice-remove", true);
            }
        }));

        this.jailPos = str2pos(config.getString("jail-pos"));
        this.autoRelease = config.getBoolean("auto-release", false);
        this.noticeAdd = config.getBoolean("notice-add");
        this.noticeRemove = config.getBoolean("notice-remove");

        //load messages
        Config msgList = new Config(new File(this.getDataFolder(), "messages.properties"), Config.PROPERTIES);

        String version = msgList.getString("version");

        if(version == null || Integer.parseInt(version) < CUSTOM_MESSAGES_VERSION) {
            Config newConfig = new Config(Config.PROPERTIES);
            if(!newConfig.load(this.getResource("messages.properties"))) {//failed
                Server.getInstance().getLogger().error("Could not load the new config.");
                Server.getInstance().getPluginManager().disablePlugin(this);
                return;
            }

            msgList.setDefault(newConfig.getRootSection());//fill

            msgList.set("version", CUSTOM_MESSAGES_VERSION);

            this.getLogger().notice("Updated messages.properties");
        }
        msgList.save();//save again

        for(Map.Entry<String, Object> entry : msgList.getAll().entrySet()) {
            if(entry.getValue() instanceof String) {
                messages.put(entry.getKey(), (String) entry.getValue());
            }
        }

        //load list
        Config list = new Config(new File(this.getDataFolder(), "list.yml"), Config.YAML);
        list.setDefault(new ConfigSection(new LinkedHashMap<String, Object>() {
            {

                put("otu", new ArrayList<String>());
                put("runa", new ArrayList<String>());
            }
        }));

        //load list
        this.otuPlayers = list.getStringList("otu");
        this.runaPlayers = list.getStringList("runa");

        //command

        Map<String, String> cmdDes = new LinkedHashMap<String, String>(){{
            put("otu", MainClass.this.getCustomMessage("command.otu.description"));
            put("runa", MainClass.this.getCustomMessage("command.runa.description"));
            put("otup", MainClass.this.getCustomMessage("command.otup.description"));
            put("otulist", MainClass.this.getCustomMessage("command.otulist.description"));
        }};

        Map<String, Map<String, CommandParameter[]>> cmdParams = new LinkedHashMap<String, Map<String, CommandParameter[]>>(){
            {
                put("otu", new LinkedHashMap<String, CommandParameter[]>(){
                    {
                        put("otu", new CommandParameter[]{
                            new CommandParameter("player", CommandParameter.ARG_TYPE_STRING, false)
                        });
                    }
                });
                put("runa", new LinkedHashMap<String, CommandParameter[]>(){
                    {
                        put("runa", new CommandParameter[]{
                            new CommandParameter("player", CommandParameter.ARG_TYPE_STRING, false)
                        });
                    }
                });
                put("otup", new LinkedHashMap<String, CommandParameter[]>(){
                    {
                        put("otup", new CommandParameter[]{
                            new CommandParameter("pos", CommandParameter.ARG_TYPE_BLOCK_POS, true),
                            new CommandParameter("world", CommandParameter.ARG_TYPE_STRING, true)
                        });
                    }
                });
                put("otulist", new LinkedHashMap<String, CommandParameter[]>(){
                    {
                        put("otulist", new CommandParameter[]{
                            new CommandParameter("otu(o)|runa(r)", CommandParameter.ARG_TYPE_RAW_TEXT, true),
                            new CommandParameter("page", CommandParameter.ARG_TYPE_INT, true)
                        });
                    }
                });
            }
        };

        for(Map.Entry<String, String> entry : cmdDes.entrySet()) {
            Command cmd = Server.getInstance().getCommandMap().getCommand(entry.getKey());
            cmd.setDescription(entry.getValue());

            if(cmdParams.containsKey(entry.getKey())) {
                cmd.setCommandParameters(cmdParams.get(entry.getKey()));
            }
        }

        //event

        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
    }

    @Override
    public void onDisable() {
        this.save();
    }

    private void save() {//

        //save config
        Config config = new Config(new File(this.getDataFolder(), "config.yml"), Config.YAML);
        config.set("jail-pos", pos2str(this.jailPos));
        config.save();

        this.saveList();
    }

    public void saveList() {
        Config list = new Config(new File(this.getDataFolder(), "list.yml"), Config.YAML);
        list.set("otu", this.otuPlayers);
        list.set("runa", this.runaPlayers);
        list.save();
    }

    private String pos2str(Position pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ() + "," + pos.getLevel().getFolderName();
    }

    private Position str2pos(String str) {
        Position pos = new Position(0, 0, 0, Server.getInstance().getDefaultLevel());

        String[] p = str.split(",");
        if(p.length >= 4) {
            Level level = Server.getInstance().getLevelByName(p[3]);
            if(level != null) {
                pos = new Position(
                        Double.parseDouble(p[0]),
                        Double.parseDouble(p[1]),
                        Double.parseDouble(p[2]),
                        level);
            }
        }
        return pos;
    }

    //Command

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName()) {
            case "otu":
                if(args.length <= 0) {
                    this.sendCustomMessage(sender, "command.notEnoughParam");
                    return true;
                }

                String name = args[0];

                Player player = Server.getInstance().getPlayer(name);
                if(player != null) {
                    name = player.getName();
                }

                if(!this.isOtued(name)) {
                    this.addOtu(name);

                    this.sendCustomMessage(sender, "otu.add.sender", name, sender.getName());

                    if(player != null) {
                        player.teleport(this.getJailPos());
                        this.sendCustomMessage(player,"otu.add.receiver");
                    }

                    if(this.noticeAdd) {
                        this.broadcastCustomMessage("otu.add.notice", sender.getName(), name);
                    }
                } else {
                    this.removeOtu(name);

                    if(this.isRunaed(name)) {//remove together
                        this.removeRuna(name);
                    }

                    this.sendCustomMessage(sender, "otu.remove.sender", sender.getName(), name);

                    if(player != null) {
                        this.sendCustomMessage(player, "otu.remove.receiver");
                    }

                    if(this.noticeRemove) {
                        this.broadcastCustomMessage("otu.remove.notice", sender.getName(), name);
                    }
                }

                this.saveList();
                break;
            case "runa":
                if(args.length <= 0) {
                    this.sendCustomMessage(sender, "command.notEnoughParam");
                    return true;
                }

                name = args[0];

                player = Server.getInstance().getPlayer(name);
                if(player != null) {
                    name = player.getName();
                }

                if(!this.isRunaed(name)) {
                    this.addRuna(name);

                    this.sendCustomMessage(sender, "runa.add.sender", name, sender.getName());

                    if(player != null) {
                        this.sendCustomMessage(player, "runa.add.receiver");
                    }

                    if(this.noticeAdd) {
                        this.broadcastCustomMessage("runa.add.notice", sender.getName(), name);
                    }
                } else {
                    this.removeRuna(name);

                    this.sendCustomMessage(sender, "runa.remove.sender", name, sender.getName());

                    if(player != null) {
                        this.sendCustomMessage(player, "runa.remove.receiver");
                    }

                    if(this.noticeRemove) {
                        this.broadcastCustomMessage("runa.remove.notice", sender.getName(), name);
                    }
                }

                this.saveList();
                break;
            case "otup":
                Position pos;

                if(sender instanceof ConsoleCommandSender || args.length > 0) {
                    if(sender instanceof ConsoleCommandSender && args.length < 4) {
                        this.sendCustomMessage(sender, "otup.notEnoughParam");
                        return true;
                    }

                    Double x, y, z;
                    try{
                        x = Double.parseDouble(args[0]);
                        y = Double.parseDouble(args[1]);
                        z = Double.parseDouble(args[2]);
                    } catch (NumberFormatException e) {
                        this.sendCustomMessage(sender, "otup.onlyNumber");
                        return true;
                    }

                    Level level = Server.getInstance().getLevelByName(args[3]);
                    if(level == null) {
                        this.sendCustomMessage(sender, "otup.notFountTheWorld");
                        return true;
                    }

                    pos = new Position(x, y, z, level).floor().add(0.5, 0, 0.5);
                } else {
                    pos = ((Player) sender).getPosition().floor().add(0.5, 0, 0.5);
                }

                this.setJailPos(pos);

                this.save();

                this.sendCustomMessage(sender, "otup.set",
                        String.valueOf(pos.getX()),
                        String.valueOf(pos.getY()),
                        String.valueOf(pos.getZ()),
                        pos.getLevel().getFolderName());

                break;
            case "otulist":
                String type = "otu";
                int page = 0;

                if(args.length > 0 && this.isNumber(args[0])) {
                    page = Integer.parseInt(args[0]);
                } else {
                    if(args.length > 0) {
                        type = args[0];
                    }

                    if(args.length > 1 && this.isNumber(args[1])) {
                        page = Integer.parseInt(args[1]);
                    }
                }

                List<String> list;
                if(type.equals("runa") || type.equals("r")) {
                    type = "runa";
                    list = this.getRunaPlayers();
                } else {
                    type ="otu";
                    list = this.getOtuPlayers();
                }


                int max = Math.max(0, (list.size() / 4) - 1);

                page = Math.min(page, max);

                String top = this.getCustomMessage("otulist." + type + ".top", String.valueOf(page + 1), String.valueOf(max + 1), String.valueOf(list.size()));

                StringBuilder msg = new StringBuilder(top + "\n");
                for(int i = page; i < list.size(); i++) {
                    msg.append(list.get(i));
                    if((page + 1) % 20 == 0) {
                        msg.append(".omg");
                        break;
                    }else if(((i + 1) % 4) == 0) {
                        msg.append(",\n");
                    } else {
                        msg.append(", ");
                    }
                }

                sender.sendMessage(msg.toString());

                break;
        }
        return true;
    }

    public String getCustomMessage(String key, String... args) {
        String msg = this.messages.get(key);
        if(msg == null) {
            msg = "NULL:" + key;
        }

        for(int i = 0; i < args.length;i++) {
            msg = msg.replace("{%" + i + "}", args[i]);
        }

        String prefix = this.messages.get("command.prefix");
        if(prefix != null && prefix.length() > 0) {
            msg = prefix + " " + msg;
        }

        return msg;
    }

    public void sendCustomMessage(CommandSender player, String key, String... args) {
        player.sendMessage(this.getCustomMessage(key, args));
    }

    public void broadcastCustomMessage(String key, String... args) {
        Server.getInstance().broadcastMessage(this.getCustomMessage(key, args));
    }

    private boolean isNumber(String s) {
        return Pattern.compile("^-?[0-9]+$").matcher(s).find();
    }
}
