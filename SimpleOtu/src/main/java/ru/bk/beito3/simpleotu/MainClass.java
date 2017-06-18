package ru.bk.beito3.simpleotu;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import ru.bk.beito3.simpleotu.event.*;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.zone.ZoneRulesException;
import java.util.*;
import java.util.regex.Pattern;

public class MainClass extends PluginBase {

    private static int CUSTOM_MESSAGES_VERSION = 11;

    private static String DEFAULT_TIMEZONE = "Asia/Tokyo";

    private Map<String, String> messages = new HashMap<>();

    private Position jailPos;
    private boolean autoRelease = false;
    private boolean noticeAdd = false;
    private boolean noticeRemove = false;
    private boolean enableActivePlayers = false;
    private boolean enableUnotu = false;
    private String timeZone = DEFAULT_TIMEZONE;


    private OtuList otulist;

    private List<String> activePlayers = new ArrayList<>();//for event target in server

    public OtuList getOtuList() {
        return this.otulist;
    }

    public boolean isOtu(String name) {
        return this.getOtuList().exists(name) && this.getOtuList().isOtu(name);

    }

    public boolean isRuna(String name) {
        return this.getOtuList().exists(name) && this.getOtuList().isRuna(name);

    }

    public void addOtu(String name) {
        this.addOtu(name, null);
    }

    public void addOtu(String name, String sender) {
        this.getOtuList().addOtu(name, OtuList.MODE_OTU, OffsetDateTime.now(), sender);
    }

    public void removeOtu(String name) {
        this.getOtuList().remove(name);
    }

    public void setRuna(String name, boolean b) {
        this.setRuna(name, b, null);
    }

    public void setRuna(String name, boolean b, String sender) {
        if (!b) {//remove
            if (this.getOtuList().getMode(name) == OtuList.MODE_OTU_AND_RUNA){
                this.getOtuList().setMode(name, OtuList.MODE_OTU);
            } else {
                this.getOtuList().remove(name);
            }
        } else {//add
            this.getOtuList().addOtu(name, OtuList.MODE_RUNA, OffsetDateTime.now(), sender);
        }
    }

    public List<String> getActivePlayers() {
        return this.activePlayers;
    }

    public void addActivePlayer(String name) {
        if(!this.isActivePlayer(name)) {
            this.activePlayers.add(name.toLowerCase());
        }
    }

    public void removeActivePlayer(String name) {
        if(this.isActivePlayer(name)) {
            this.activePlayers.remove(name.toLowerCase());
        }
    }

    public void clearActivePlayers() {
        this.activePlayers.clear();
    }

    public boolean isActivePlayer(Player player) {
        return this.isActivePlayer(player.getName());
    }

    public boolean isActivePlayer(String name) {
        return !this.isEnableActivePlayers() || this.activePlayers.contains(name.toLowerCase());
    }

    public void updateActivePlayers() {
        this.clearActivePlayers();

        Map<UUID, Player> players = Server.getInstance().getOnlinePlayers();
        for (Map.Entry<UUID, Player> entry : players.entrySet()) {
            String name = entry.getValue().getName();
            if (this.isOtu(name) || this.isRuna(name)) {
                this.addActivePlayer(name);
            }
        }
    }


    public Position getJailPos() {
        return this.jailPos;
    }

    public void setJailPos(Position pos) {
        this.jailPos = pos;
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

    public boolean isEnableActivePlayers() {
        return this.enableActivePlayers;
    }

    public boolean isEnableUnotu() {
        return this.enableUnotu;
    }

    public ZoneId getTimeZone() {
        ZoneId timezone;
        try {
            timezone = ZoneId.of(this.timeZone);
        } catch (ZoneRulesException e) {
            timezone = ZoneId.systemDefault();
        }

        return timezone;
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
        config.setDefault(new LinkedHashMap<String, Object>() {
            {
                put("jail-pos", "0,0,0,world");
                put("auto-release", true);
                put("notice-add", true);
                put("notice-remove", true);
                put("enable-active-players", true);
                put("enable-unotu", true);
                put("timezone", DEFAULT_TIMEZONE);
            }
        });

        this.jailPos = str2pos(config.getString("jail-pos"));
        this.autoRelease = config.getBoolean("auto-release", false);
        this.noticeAdd = config.getBoolean("notice-add");
        this.noticeRemove = config.getBoolean("notice-remove");
        this.enableActivePlayers = config.getBoolean("enable-active-players");
        this.enableUnotu = config.getBoolean("enable-unotu");
        this.timeZone = config.getString("timezone");

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

        this.otulist = new OtuList(new File(this.getDataFolder(), "list.json"), this.getLogger());
        this.otulist.load();

        //Update otulist for v1.0.6 to new otulist for v2.0.0
        if((new File(this.getDataFolder(), "list.yml")).exists()) {
            updateOtuListTo200();
        }


        //command

        Map<String, String> cmdDes = new LinkedHashMap<String, String>(){{
            put("otu", MainClass.this.getCustomMessage("command.otu.description"));
            put("unotu", MainClass.this.getCustomMessage("command.unotu.description"));
            put("runa", MainClass.this.getCustomMessage("command.runa.description"));
            put("otup", MainClass.this.getCustomMessage("command.otup.description"));
            put("otulist", MainClass.this.getCustomMessage("command.otulist.description"));
            put("otuser", MainClass.this.getCustomMessage("command.otuser.description"));
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
                put("unotu", new LinkedHashMap<String, CommandParameter[]>(){
                    {
                        put("unotu", new CommandParameter[]{
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
                put("otuser", new LinkedHashMap<String, CommandParameter[]>(){
                    {
                        put("otuser", new CommandParameter[]{
                                new CommandParameter("player", CommandParameter.ARG_TYPE_STRING, false)
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
        config.set("auto-release", this.autoRelease);
        config.set("notice-add", this.noticeAdd);
        config.set("notice-remove", this.noticeRemove);
        config.set("enable-active-players", this.enableActivePlayers);
        config.set("enable-unotu", this.enableUnotu);
        config.set("timezone", this.timeZone);

        config.save();

        this.saveList();
    }

    public void saveList() {
        this.getOtuList().save();
    }

    private String pos2str(Position pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ() + "," + pos.getLevel().getFolderName();
    }

    private Position str2pos(String str) {
        Position pos = new Position(0, 0, 0, Server.getInstance().getDefaultLevel());

        String[] p = str.split(",");
        if(p.length >= 4) {
            try {
                pos.setComponents(
                        Double.parseDouble(p[0]),
                        Double.parseDouble(p[1]),
                        Double.parseDouble(p[2])
                );
            } catch(NumberFormatException e) {
                return pos;
            }

            Level level = Server.getInstance().getLevelByName(p[3]);
            if(level != null) {
                pos.setLevel(level);
            }
        }

        return pos;
    }

    public void updateOtuListTo200() {//update otulist from 1.0.6 to 2.0.0

        this.getLogger().notice("Start updating otulist for v1.0.6 to v1.1.0");

        File file = new File(this.getDataFolder(), "list.yml");

        if(!file.exists() || file.isDirectory()) {
            this.getLogger().notice("Could not find old otulist file. path:" + file.getAbsolutePath());
            return;
        }

        Config list = new Config(file, Config.YAML);
        list.setDefault(new ConfigSection(new LinkedHashMap<String, Object>() {
            {
                put("otu", new ArrayList<String>());
                put("runa", new ArrayList<String>());
            }
        }));

        List<String> otulist = list.getStringList("otu");
        List<String> runalist = list.getStringList("runa");

        for (String name : otulist) {
            if (this.getOtuList().exists(name)) {
                break;
            }

            int mode = OtuList.MODE_OTU;

            if (runalist.contains(name)) {
                mode = OtuList.MODE_OTU_AND_RUNA;
            }

            this.getOtuList().addOtu(name, mode);
        }

        for (String name : runalist) {
            if (this.getOtuList().exists(name)) {
                break;
            }

            int mode = OtuList.MODE_RUNA;

            this.getOtuList().addOtu(name, mode);
        }


        list.save(new File(file.getParent(), "list-old.yml"));
        file.delete();

        this.saveList();

        this.getLogger().notice("Updated otulist for v1.0.6 to v2.0.0");
    }

    //Command

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName();
        if (cmd.equals("otu")) {
            if (args.length <= 0) {
                this.sendCustomMessage(sender, "command.notEnoughParam");
                return true;
            }

            String name = args[0];
            if (args.length > 1) {
                StringBuilder nameBuilder = new StringBuilder();
                for (String s : args) {
                    nameBuilder.append(s);
                    nameBuilder.append(" ");
                }

                name = nameBuilder.substring(0, nameBuilder.length() - 1);
            }

            Player player = Server.getInstance().getPlayer(name);
            if (player != null) {
                name = player.getName();
            }

            if (!this.isOtu(name)) {
                OtuAddEvent ev;
                Server.getInstance().getPluginManager().callEvent(ev = new OtuAddEvent(this, new OtuEntry(name, OtuList.MODE_OTU, OffsetDateTime.now(), sender.getName())));
                if (ev.isCancelled()) {
                    this.sendCustomMessage(sender, "command.event.cancel");

                    return true;
                }

                this.getOtuList().add(ev.getEntry());

                this.sendCustomMessage(sender, "otu.add.sender", name, sender.getName());

                if (player != null) {
                    player.teleport(this.getJailPos());
                    this.sendCustomMessage(player, "otu.add.receiver");
                }

                if (this.isNoticeAdd()) {
                    this.broadcastCustomMessage("otu.add.notice", sender.getName(), name);
                }

                this.saveList();
                this.updateActivePlayers();
            } else if (!this.isEnableUnotu()) {
                OtuRemoveEvent ev;
                Server.getInstance().getPluginManager().callEvent(ev = new OtuRemoveEvent(this, this.getOtuList().getEntry(name)));
                if (ev.isCancelled()) {
                    this.sendCustomMessage(sender, "command.event.cancel");

                    return true;
                }
                this.removeOtu(ev.getEntry().getName());

                this.sendCustomMessage(sender, "otu.remove.sender", sender.getName(), name);

                if (player != null) {
                    this.sendCustomMessage(player, "otu.remove.receiver");
                }

                if (this.isNoticeRemove()) {
                    this.broadcastCustomMessage("otu.remove.notice", sender.getName(), name);
                }

                this.saveList();
                this.updateActivePlayers();
            } else {
                this.sendCustomMessage(sender, "otu.already.exists", sender.getName(), name);
            }
        } else if (cmd.equals("unotu")) {

            if (args.length <= 0) {
                this.sendCustomMessage(sender, "command.notEnoughParam");
                return true;
            }

            String name = args[0];
            if (args.length > 1) {
                StringBuilder nameBuilder = new StringBuilder();
                for (String s : args) {
                    nameBuilder.append(s);
                    nameBuilder.append(" ");
                }

                name = nameBuilder.substring(0, nameBuilder.length() - 1);
            }

            Player player = Server.getInstance().getPlayer(name);
            if (player != null) {
                name = player.getName();
            }

            if (this.isOtu(name)) {
                OtuRemoveEvent ev;
                Server.getInstance().getPluginManager().callEvent(ev = new OtuRemoveEvent(this, this.getOtuList().getEntry(name)));
                if (ev.isCancelled()) {
                    this.sendCustomMessage(sender, "command.event.cancel");

                    return true;
                }

                this.removeOtu(ev.getEntry().getName());

                this.sendCustomMessage(sender, "otu.remove.sender", sender.getName(), name);

                if (player != null) {
                    this.sendCustomMessage(player, "otu.remove.receiver");
                }

                if (this.isNoticeRemove()) {
                    this.broadcastCustomMessage("otu.remove.notice", sender.getName(), name);
                }

                this.saveList();
                this.updateActivePlayers();
            } else {
                this.sendCustomMessage(sender, "otu.not.exists", sender.getName(), name);
            }

        } else if (cmd.equals("runa")) {

            if (args.length <= 0) {
                this.sendCustomMessage(sender, "command.notEnoughParam");
                return true;
            }

            String name = args[0];
            if (args.length > 1) {
                StringBuilder nameBuilder = new StringBuilder();
                for (String s : args) {
                    nameBuilder.append(s);
                    nameBuilder.append(" ");
                }

                name = nameBuilder.substring(0, nameBuilder.length() - 1);
            }

            Player player = Server.getInstance().getPlayer(name);
            if (player != null) {
                name = player.getName();
            }

            if (!this.isRuna(name)) {
                OtuEntry entry = this.getOtuList().getEntry(name);
                if (entry != null) {
                    entry = new OtuEntry(name, OtuList.MODE_OTU_AND_RUNA, OffsetDateTime.now(), sender.getName());
                } else {
                    entry = new OtuEntry(name, OtuList.MODE_RUNA, OffsetDateTime.now(), sender.getName());
                }

                RunaAddEvent ev;
                Server.getInstance().getPluginManager().callEvent(ev = new RunaAddEvent(this, entry));
                if (ev.isCancelled()) {
                    this.sendCustomMessage(sender, "command.event.cancel");

                    return true;
                }

                //this.setRuna(name, true, ev.getName());
                this.getOtuList().add(entry);

                this.sendCustomMessage(sender, "runa.add.sender", name, sender.getName());

                if (player != null) {
                    this.sendCustomMessage(player, "runa.add.receiver");
                }

                if (this.isNoticeAdd()) {
                    this.broadcastCustomMessage("runa.add.notice", sender.getName(), name);
                }
            } else {
                RunaRemoveEvent ev;
                Server.getInstance().getPluginManager().callEvent(ev = new RunaRemoveEvent(this, this.getOtuList().getEntry(name)));
                if (ev.isCancelled()) {
                    this.sendCustomMessage(sender, "command.event.cancel");

                    return true;
                }

                this.setRuna(name, false, sender.getName());

                this.sendCustomMessage(sender, "runa.remove.sender", name, sender.getName());

                if (player != null) {
                    this.sendCustomMessage(player, "runa.remove.receiver");
                }

                if (this.isNoticeRemove()) {
                    this.broadcastCustomMessage("runa.remove.notice", sender.getName(), name);
                }
            }

            this.saveList();
            this.updateActivePlayers();
        } else if (cmd.equals("otup")) {
            Position pos;

            if (sender instanceof ConsoleCommandSender || args.length > 0) {
                if (sender instanceof ConsoleCommandSender && args.length < 4) {
                    this.sendCustomMessage(sender, "otup.notEnoughParam");
                    return true;
                }

                Double x, y, z;
                try {
                    x = Double.parseDouble(args[0]);
                    y = Double.parseDouble(args[1]);
                    z = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    this.sendCustomMessage(sender, "otup.onlyNumber");
                    return true;
                }

                Level level = Server.getInstance().getLevelByName(args[3]);
                if (level == null) {
                    this.sendCustomMessage(sender, "otup.notFountTheWorld");
                    return true;
                }

                pos = new Position(x, y, z, level).floor().add(0.5, 0, 0.5);
            } else {
                pos = ((Player) sender).getPosition().floor().add(0.5, 0, 0.5);
            }

            OtupChangeEvent ev;
            Server.getInstance().getPluginManager().callEvent(ev = new OtupChangeEvent(this, this.getJailPos(), pos, sender));
            if (ev.isCancelled()) {
                this.sendCustomMessage(sender, "command.event.cancel");

                return true;
            }

            this.setJailPos(pos);

            this.save();

            this.sendCustomMessage(sender, "otup.set",
                    String.valueOf(pos.getX()),
                    String.valueOf(pos.getY()),
                    String.valueOf(pos.getZ()),
                    pos.getLevel().getFolderName());

        } else if (cmd.equals("otulist")) {

            String type = "otu";
            int page = 0;

            if (args.length > 0) {
                if (this.isNumber(args[0])) {
                    page = Integer.parseInt(args[0]) - 1;
                } else {
                    type = args[0];
                    if (args.length > 1 && this.isNumber(args[1])) {
                        page = Integer.parseInt(args[1]) - 1;
                    }
                }
            }

            List<String> list;
            this.getLogger().debug("test:" + type);
            if (type.equals("runa") || type.equals("r")) {
                type = "runa";
                list = this.getOtuList().getRunaNames();
            } else {
                type = "otu";
                list = this.getOtuList().getOtuNames();
            }


            int max = Math.max(0, (list.size() / 20));

            page = Math.min(page, max);

            String top = this.getCustomMessage("otulist." + type + ".top", String.valueOf(page + 1), String.valueOf(max + 1), String.valueOf(list.size()));

            StringBuilder msg = new StringBuilder(top + "\n");
            for (int i = (page * 20); i < list.size(); i++) {
                msg.append(list.get(i));
                if ((i + 1) % 20 == 0) {//close
                    msg.append(".");
                    break;
                } else if (((i + 1) % 4) == 0) {//next line
                    msg.append(",\n");
                } else {//next
                    msg.append(", ");
                }
            }

            sender.sendMessage(msg.toString());

        } else if (cmd.equals("otuser")) {
            if (args.length <= 0) {
                this.sendCustomMessage(sender, "command.notEnoughParam");
                return true;
            }

            String name = args[0];
            if (args.length > 1) {
                StringBuilder nameBuilder = new StringBuilder();
                for (String s : args) {
                    nameBuilder.append(s);
                    nameBuilder.append(" ");
                }

                name = nameBuilder.substring(0, nameBuilder.length() - 1);
            }

            name = name.toLowerCase();

            List<OtuEntry> list = new ArrayList<>();

            List<OtuEntry> entries = this.getOtuList().getEntryList();

            for (OtuEntry e : entries) {
                if (e.getName().startsWith(name)) {
                    list.add(e);
                }
            }

            if (list.size() <= 0) {
                this.sendCustomMessage(sender, "otuser.notfound", name);
                return true;
            }

            StringBuilder msgs = new StringBuilder();
            msgs.append(this.getCustomMessage("otuser.list.top", name, Integer.toString(list.size())));
            msgs.append("\n");

            for (OtuEntry e : list) {

                String mode;
                switch (e.getMode()) {
                    case OtuList.MODE_RUNA:
                        mode = "runa";
                        break;
                    case OtuList.MODE_OTU_AND_RUNA:
                        mode = "otu & runa";
                        break;
                    case OtuList.MODE_OTU:
                    default:
                        mode = "otu";
                }

                String source = e.getSource();
                if (source == null) {
                    source = "Unknown";
                }

                String creationDate;
                if (e.getCreationDateString() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.getCustomMessage("otuser.list.timeformat", true));
                    //creationDate = e.getCreationDate().toLocalDateTime().atZone(this.getTimeZone()).format(formatter);
                    creationDate = e.getCreationDate().atZoneSameInstant(this.getTimeZone()).format(formatter);
                    //creationDate = e.getCreationDate().atZoneSameInstant(this.getTimeZone()).toString();
                } else {
                    creationDate = "Unknown";
                }

                msgs.append(this.getCustomMessage("otuser.list.item", e.getName(), mode, source, creationDate));
                msgs.append("\n");
            }

            sender.sendMessage(msgs.toString());
        }

        return true;
    }

    public String getCustomMessage(String key, String... args) {
        return this.getCustomMessage(key, false, args);
    }

    public String getCustomMessage(String key, boolean noPrefix, String... args) {
        String msg = this.messages.get(key);
        if(msg == null) {
            msg = "NULL:" + key;
        }

        for (int i = 0; i < args.length; i++) {
            msg = msg.replace("{%" + i + "}", args[i]);
        }

        String prefix = this.messages.get("command.prefix");
        if (prefix != null && prefix.length() > 0 && !noPrefix) {
            msg = prefix + " " + msg;
        }

        return msg;
    }

    public void sendCustomMessage(CommandSender player, String key, String... args) {
        player.sendMessage(this.getCustomMessage(key, false, args));
    }

    public void broadcastCustomMessage(String key, String... args) {
        Server.getInstance().broadcastMessage(this.getCustomMessage(key, false, args));
    }

    private boolean isNumber(String s) {
        return Pattern.compile("^-?[0-9]+$").matcher(s).find();
    }
}
