package ru.bk.beito3.hanabi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.InstantSpellParticle;
import cn.nukkit.level.sound.LevelEventSound;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.MathHelper;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.LevelEventPacket;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.BlockColor;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import ru.bk.beito3.hanabi.task.FireTask;
import ru.bk.beito3.hanabi.task.SmokeTask;
import ru.bk.beito3.hanabi.util.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class MainClass extends PluginBase implements Listener {

    public static final int DEFAULT_SOUND_WHISTLE = LevelEventPacket.EVENT_SOUND_FIZZ;
    public static final int DEFAULT_SOUND_FIRE = LevelEventPacket.EVENT_SOUND_DOOR_BUMP;

    public static final int DEFAULT_POS_MIN_HEIGHT = 20;
    public static final int DEFAULT_POS_MAX_HEIGHT = 25;
    public static final int DEFAULT_POS_ANGLE = -1;
    public static final boolean DEFAULT_POS_SOUND = true;

    public static final boolean DEFAULT_OPT_SMOKE = true;

    public static final String[] SUPPORT_IMAGE_EXTENSION = {
            ".png",
            ".jpg",
            ".jpeg"
    };

    //private Timing timing;
    private CustomMessage messages;

    private NukkitRandom rand = new NukkitRandom();

    private Map<String, HanaMap> maps = new HashMap<>();
    private Map<String, PosInfo> posData = new HashMap<>();
    private Map<String, List<Position>> temp = new HashMap<>();

    private int soundWhistle = DEFAULT_SOUND_WHISTLE;
    private int soundFire = DEFAULT_SOUND_FIRE;
    private boolean optSmoke = DEFAULT_OPT_SMOKE;

    public boolean existsHanaMap(String name) {
        return maps.containsKey(name);
    }

    public HanaMap getHanaMap(String name) {
        if (!this.existsHanaMap(name)) {
            return null;
        }

        return maps.get(name);
    }

    public void setHanaMap(String name, HanaMap map) {
        if (!this.existsHanaMap(name)) {
            maps.put(name, map);
        }
    }

    public void removeHanaMap(String name) {
        if (this.existsHanaMap(name)) {
            maps.remove(name);
        }
    }

    @Override
    public void onEnable() {
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
        }

        this.saveResource("messages.properties");
        this.saveResource("messages-en.properties");

        //load config
        Config config = new Config(new File(this.getDataFolder(), "config.yml"), Config.YAML);
        config.setDefault(new LinkedHashMap<String, Object>() {
            {
                put("load-image", true);
                put("sound-whistle", DEFAULT_SOUND_WHISTLE);
                put("sound-fire", DEFAULT_SOUND_FIRE);
                put("opt-smoke", DEFAULT_OPT_SMOKE);
            }
        });
        config.save();

        this.soundWhistle = config.getInt("sound-whistle");
        this.soundFire = config.getInt("sound-fire");

        //Load messages
        Config msgConfig = new Config(new File(this.getDataFolder(), "messages.properties"), Config.PROPERTIES);

        messages = new CustomMessage(msgConfig.getRootSection());
        messages.setPrefix(messages.get("command.prefix"));

        //Load image

        if (config.getBoolean("load-image")) {
            File imageFolder = new File(this.getDataFolder(), "image/");
            if (!imageFolder.exists()) {
                imageFolder.mkdirs();
            }

            File[] list = imageFolder.listFiles();
            if (list != null) {
                for (File file : list) {
                    if (!file.isFile() || !file.canRead()) {
                        continue;
                    }

                    String name = file.getName();

                    boolean supportedImage = false;
                    for (String e : SUPPORT_IMAGE_EXTENSION) {
                        if (name.endsWith(e)) {
                            supportedImage = true;
                            break;
                        }
                    }

                    if (!supportedImage) {
                        continue;
                    }

                    BufferedImage image;
                    try {
                        image = ImageIO.read(file);
                    } catch (IOException e) {
                        this.getLogger().warning("Couldn't load the image. Error:" + e.getMessage() + " File:" + file.getAbsolutePath());
                        continue;
                    }

                    if (image == null) {
                        continue;
                    }

                    this.setHanaMap("@" + Utils.getNameWithoutExt(file), HanaMap.FromImage(image));
                }
            }
        }

        //load maps
        Config mapConfig = new Config(new File(this.getDataFolder(), "map.yml"), Config.YAML);
        Set<String> list = mapConfig.getKeys(false);
        for (String name : list) {
            List<List<List<Integer>>> m = null;
            m = mapConfig.get(name, m);
            if (m == null) {
                continue;
            }

            this.setHanaMap(name, HanaMap.FromRGBList(m));
        }

        //load Positions
        Config posConfig = new Config(new File(this.getDataFolder(), "pos.yml"), Config.YAML);

        Set<String> keys = posConfig.getKeys(false);
        for (String key : keys) {
            ConfigSection section = posConfig.getSection(key);

            PosInfo info = new PosInfo();
            info.pos = MinecraftUtils.str2pos(section.getString("pos"));
            info.minHeight = section.getInt("min-height");
            info.maxHeight = section.getInt("max-height");
            info.angle = section.getInt("angle");
            info.sound = section.getBoolean("sound");

            if (info.pos == null) {
                this.getLogger().warning("Couldn't load the pos. Position name:" + key);
                continue;
            }

            posData.put(key, info);
        }

        //Test

        /*
        HanaMap jaga = this.getHanaMap("jaga");
        int deg = this.rand.nextBoundedInt(360);
        int c = 1000;

        long start = System.currentTimeMillis();

        for(int i = 0; i < c; i++) {
            test(jaga, deg);
        }

        long end = System.currentTimeMillis();
        this.getLogger().notice("Test1(t/c):" + String.format("%f", ((end - start) / (double) c))  + "ms");
        this.getLogger().notice("Test1(total):" + (end - start)  + "ms");

        start = System.currentTimeMillis();

        for(int i = 0; i < c; i++) {
            test2(jaga.toList(), deg);
        }

        end = System.currentTimeMillis();
        this.getLogger().notice("Test2(t/c):" + String.format("%f", ((end - start) / (double) c))  + "ms");
        this.getLogger().notice("Test2(Total):" + (end - start)  + "ms");
        */


        //Command settings

        //Registers event listener

        this.getServer().getPluginManager().registerEvents(this, this);

        //timings
        //timing = TimingsManager.getTiming("Hana Firework");
    }

    @Override
    public void onDisable() {
        //save maps
        Config mapConfig = new Config(new File(this.getDataFolder(), "map.yml"), Config.YAML);

        LinkedHashMap<String, Object> list = new LinkedHashMap<>();
        for (Map.Entry<String, HanaMap> entry : this.maps.entrySet()) {
            if (entry.getKey().startsWith("@")) {
                continue;
            }

            list.put(entry.getKey(), entry.getValue().toRGBList());
        }

        mapConfig.setAll(list);
        mapConfig.save();

        //save pos
        Config posConfig = new Config(new File(this.getDataFolder(), "pos.yml"), Config.YAML);

        for (Map.Entry<String, PosInfo> entry : posData.entrySet()) {
            PosInfo info = entry.getValue();
            posConfig.set(entry.getKey(), new LinkedHashMap<String, Object>() {{
                put("pos", MinecraftUtils.pos2str(info.pos));
                put("min-height", info.minHeight);
                put("max-height", info.maxHeight);
                put("angle", info.angle);
                put("sound", info.sound);
            }});
        }
        posConfig.save();

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (this.temp.containsKey(player.getName())) {
            this.temp.remove(player.getName());
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (this.temp.containsKey(player.getName())) {
            List<Position> list = this.temp.get(player.getName());
            if (list.size() == 0) {//first
                list.add(event.getBlock());

                messages.sendMessage(player, "command.hanac.make.pos1");
            } else if (list.size() == 1) {//second
                Position first = list.get(0);
                Position second = event.getBlock();

                if (!second.getLevel().getFolderName().equals(first.getLevel().getFolderName())) {
                    messages.sendMessage(player, "command.hanac.make.noSameWorld");
                    return;
                }

                list.add(second);

                BlockVector3 diff = first.subtract(second).abs().asBlockVector3();

                int count = Math.max(diff.x, 1) * Math.max(diff.y, 1) * Math.max(diff.z, 1);

                messages.sendMessage(player, "command.hanac.make.pos2", count);
            }

            this.temp.put(player.getName(), list);

            event.setCancelled();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.getLogger().notice("Test: " + Arrays.toString(args));
        String cmd = command.getName().toLowerCase();
        if (cmd.equals("hana")) {//hana
            if (args.length < 1) {
                messages.sendMessage(sender, "command.hana.name.notSet");
                return true;
            }

            String name = args[0];

            if (!this.existsHanaMap(name)) {
                messages.sendMessage(sender, "command.common.map.noExist");
                return true;
            }

            HanaMap map = this.getHanaMap(name);

            PosInfo info;
            if (args.length > 1) {
                String pos = args[1];
                if (!posData.containsKey(pos)) {
                    messages.sendMessage(sender, "command.common.pos.noExist");
                    return true;
                }

                info = posData.get(pos);
            } else {
                if (!sender.isPlayer()) {
                    messages.sendMessage(sender, "command.hana.pos.notSet");
                    return true;
                }

                Player player = (Player) sender;

                info = new PosInfo();
                info.pos = player.getPosition();
                info.minHeight = DEFAULT_POS_MIN_HEIGHT;
                info.maxHeight = DEFAULT_POS_MAX_HEIGHT;
                info.angle = DEFAULT_POS_ANGLE;
                info.sound = DEFAULT_POS_SOUND;
            }

            if (info.angle < 0) {
                info.angle = this.rand.nextBoundedInt(360);
            }

            firework(map, info);

            messages.sendMessage(sender, "command.hana.success");
        } else if (cmd.equals("hanac")) {//hanac
            if (args.length < 1) {
                return false;
            }

            String sub = args[0].toLowerCase();
            if (sub.equals("mode")) {
                if (!sender.isPlayer()) {
                    messages.sendMessage(sender, "command.common.ingame");

                    return true;
                }

                Player player = (Player) sender;

                boolean mode = false;
                if (args.length > 1) {
                    mode = Utils.str2bool(args[1]);
                } else {
                    if (!this.temp.containsKey(player.getName())) {
                        mode = true;
                    }
                }

                if (mode) {
                    this.temp.put(player.getName(), new ArrayList<>());

                    messages.sendMessage(sender, "command.hanac.mode.on");
                } else {
                    if (this.temp.containsKey(player.getName())) {
                        this.temp.remove(player.getName());
                    }

                    messages.sendMessage(sender, "command.hanac.mode.off");
                }
            } else if (sub.equals("make")) {
                if (!sender.isPlayer()) {
                    messages.sendMessage(sender, "command.common.ingame");

                    return true;
                }

                Player player = (Player) sender;

                if (!this.temp.containsKey(player.getName())) {
                    messages.sendMessage(player, "command.hanac.make.area.notSet");
                    return true;
                }

                List<Position> list = this.temp.get(player.getName());
                if (list.size() < 2) {
                    messages.sendMessage(player, "command.hanac.make.area.second.notSet");
                    return true;
                }

                if (args.length < 2) {
                    messages.sendMessage(player, "command.hanac.make.name.notSet");
                    return true;
                }

                String name = args[1];
                if (this.maps.containsKey(name)) {
                    messages.sendMessage(player, "command.hanac.common.alreadyExists");
                    return true;
                }

                List<Position> tempPos = this.temp.get(player.getName());

                Vector3 first = tempPos.get(0).floor();
                Vector3 second = tempPos.get(1).floor();

                Level level = tempPos.get(0).getLevel();

                Map<Vec3, BlockColor> map = new HashMap<>();

                int minX = (int) Math.min(first.getX(), second.getX());
                int maxX = (int) Math.max(first.getX(), second.getX());
                int minY = (int) Math.min(first.getY(), second.getY());
                int maxY = (int) Math.max(first.getY(), second.getY());
                int minZ = (int) Math.min(first.getZ(), second.getZ());
                int maxZ = (int) Math.max(first.getZ(), second.getZ());
                for (int x = maxX; x >= minX; x--) {
                    for (int y = maxY; y >= minY; y--) {
                        for (int z = maxZ; z >= minZ; z--) {
                            Block block = level.getBlock(new Vector3(x, y, z));
                            map.put(new Vec3(maxX - x, maxY - y, maxZ - z), block.getColor());
                        }
                    }
                }

                /*int dx = Math.abs(maxX - minX);
                int dy = Math.abs(maxY - minY);
                int dz = Math.abs(maxZ - minZ);
                BlockVector3 diff = first.subtract(second).abs().asBlockVector3();
                for(int x = 0; x < diff.x; x++) {
                    for(int y = 0; y < diff.y; y++) {
                        for(int z = 0; z < diff.z; z++) {
                            Block block = level.getBlock(new Vector3())
                        }
                    }
                }*/

                this.maps.put(name, new HanaMap(map));

                this.messages.sendMessage(player, "command.hanac.make.success");
            } else if (sub.equals("skin")) {
                if (args.length < 3) {
                    messages.sendMessage(sender, "command.hanac.help.skin");
                    return true;
                }

                String name = args[1];
                if (this.maps.containsKey(name)) {
                    messages.sendMessage(sender, "command.hanac.common.alreadyExists");
                    return true;
                }

                String target = args[2].toLowerCase();
                if (args.length > 3) {
                    StringBuilder nameBuilder = new StringBuilder();
                    for (int i = 2; i < args.length; i++) {
                        nameBuilder.append(args[i]);
                        nameBuilder.append(" ");
                    }

                    target = nameBuilder.substring(0, nameBuilder.length() - 1);
                }

                CompoundTag tags;

                Player player = Server.getInstance().getPlayerExact(target);
                if (player != null) {
                    tags = player.namedTag;
                } else {
                    File file = new File(Server.getInstance().getDataPath() + "players/" + target + ".dat");//TODO: supports the UUID someday
                    if (!file.exists() || !file.isFile()) {
                        messages.sendMessage(sender, "command.hanac.skin.notFound");
                        return true;
                    }

                    tags = MinecraftUtils.getPlayerCompoundTags(file);
                }

                if (tags == null) {
                    messages.sendMessage(sender, "command.hanac.skin.dataError");
                    return true;
                }

                BufferedImage skin = MinecraftUtils.getSkin(tags);
                if (skin == null) {
                    messages.sendMessage(sender, "command.hanac.skin.skinError");
                    return true;
                }

                BufferedImage face = MinecraftUtils.getSkinFace(skin);
                if (face == null) {
                    messages.sendMessage(sender, "command.hanac.skin.faceError");
                    return true;
                }

                this.maps.put(name, HanaMap.FromImage(face));

                messages.sendMessage(sender, "command.hanac.success");
            } else if (sub.equals("del")) {
                if (args.length < 2) {
                    messages.sendMessage(sender, "command.hanac.common.alreadyExists");
                    return true;
                }

                String name = args[1];
                if (!this.existsHanaMap(name)) {
                    messages.sendMessage(sender, "command.common.map.noExist");
                    return true;
                }

                this.removeHanaMap(name);

                messages.sendMessage(sender, "command.hanac.del.success");
            } else {//help
                messages.sendMessage(sender, "command.hanac.help.top");
                messages.sendMessage(sender, "command.hanac.help.mode");
                messages.sendMessage(sender, "command.hanac.help.make");
                messages.sendMessage(sender, "command.hanac.help.skin");
                messages.sendMessage(sender, "command.hanac.help.del");
            }
        } else if (cmd.equals("hanap")) {
            if (args.length < 1) {
                return false;
            }

            String sub = args[0].toLowerCase();
            if (sub.equals("add")) {
                if (args.length < 2) {
                    messages.sendMessage(sender, "command.hanap.common.name.notSet");
                    return true;
                }

                String name = args[1];

                if (posData.containsKey(name)) {
                    messages.sendMessage(sender, "command.hanap.add.alreadyExists");
                    return true;
                }

                Position pos;
                if (sender.isPlayer()) {
                    Player player = (Player) sender;
                    if (args.length >= 3) {//if enter coordinates.
                        if (args.length < 5) {
                            messages.sendMessage(player, "command.hanap.add.pos.notSet");
                            return true;
                        }

                        Vector3 p = MinecraftUtils.toAbsVector3(player, args[2], args[3], args[4]);
                        if (p == null) {
                            messages.sendMessage(player, "command.hanap.add.pos.invalid");
                            return true;
                        }

                        Level level;
                        if (args.length > 5) {//if enter world's name.
                            level = Server.getInstance().getLevelByName(args[5]);
                            if (level == null) {
                                messages.sendMessage(player, "command.hanap.add.world.notFound");
                                return true;
                            }
                        } else {
                            level = player.getLevel();
                        }

                        pos = Position.fromObject(p, level);
                    } else {
                        pos = player.getPosition();
                    }
                } else {//if sender was console
                    if (args.length >= 6) {
                        Double x, y, z;
                        x = Utils.str2double(args[2]);
                        y = Utils.str2double(args[3]);
                        z = Utils.str2double(args[4]);
                        if (x == null || y == null || z == null) {
                            messages.sendMessage(sender, "command.hanap.add.pos.invalid");
                            return true;
                        }

                        Level level = Server.getInstance().getLevelByName(args[5]);
                        if (level == null) {
                            messages.sendMessage(sender, "command.hanap.add.world.notFound");
                            return true;
                        }

                        pos = new Position(x, y, z, level);
                    } else {
                        messages.sendMessage(sender, "command.hanap.add.pos.notSet");
                        return true;
                    }
                }

                posData.put(name, new PosInfo(pos));

                sender.sendMessage("Adds the pos. (" + MinecraftUtils.pos2str(pos.floor()) + ")");
            } else if (sub.equals("del")) {
                if (args.length < 2) {
                    messages.sendMessage(sender, "command.hanap.common.name.notSet");
                    return true;
                }

                String name = args[1];

                if (!posData.containsKey(name)) {
                    sender.sendMessage("Couldn't find the pos.");
                    messages.sendMessage(sender, "");
                    return true;
                }

                posData.remove(name);
                sender.sendMessage("Deletes the pos.");
            } else if (sub.equals("set")) {
                if (args.length < 2) {
                    messages.sendMessage(sender, "command.hanap.common.name.notSet");
                    return true;
                }

                String name = args[1];

                if (!posData.containsKey(name)) {
                    messages.sendMessage(sender, "command.common.pos.noExist");
                    return true;
                }

                PosInfo info = posData.get(name);

                if (args.length > 3) {
                    String h = args[2];
                    int index = h.indexOf("-");
                    if (index != -1) {
                        Integer height = Utils.str2int(h.substring(0, index));
                        if (height == null) {
                            messages.sendMessage(sender, "command.hanap.set.height.invalid");
                            return true;
                        }

                        Integer nheight = Utils.str2int(h.substring(index + 1));
                        if (nheight == null) {
                            messages.sendMessage(sender, "command.hanap.set.height.invalid");
                            return true;
                        }

                        info.minHeight = Math.min(height, nheight);
                        info.maxHeight = Math.max(height, nheight);
                    } else {
                        Integer height = Utils.str2int(h);
                        if (height == null) {
                            messages.sendMessage(sender, "command.hanap.set.height.invalid");
                            return true;
                        }

                        info.minHeight = height;
                        info.maxHeight = height;
                    }
                }
                if (args.length > 4) {
                    Integer a = Utils.str2int(args[3]);
                    if (a == null) {
                        messages.sendMessage(sender, "command.hanap.set.angle.invalid");
                        return true;
                    }

                    info.angle = a;
                }

                if (args.length > 5) {
                    info.sound = Utils.str2bool(args[4]);
                }

                posData.put(name, info);

                messages.sendMessage(sender, "command.hanap.set.success", info.minHeight, info.maxHeight, info.angle, info.sound);
            } else {//help
                messages.sendMessage(sender, "command.hanap.help.top");
                messages.sendMessage(sender, "command.hanap.help.add");
                messages.sendMessage(sender, "command.hanap.help.del");
                messages.sendMessage(sender, "command.hanap.help.set");
            }
        }

        return true;
    }

    public void firework(HanaMap map, PosInfo info) {
        this.firework(map, info.pos, this.rand.nextRange(info.minHeight, info.maxHeight), info.angle, info.sound, this.optSmoke);
    }

    public void firework(HanaMap map, Position pos, int height, int angle, boolean sound, boolean smoke) {
        double ax = map.getMaxX() / 2;
        double az = map.getMaxZ() / 2;

        float rad = (float) Math.toRadians(angle);
        double x = ax * MathHelper.cos(rad) - az * MathHelper.sin(rad);
        double z = ax * MathHelper.sin(rad) + az * MathHelper.cos(rad);

        //Server.getInstance().getLogger().notice("red:" + rad + ", x:" + x + ", z:" + z + ", r" + r);

        if (sound) {
            pos.getLevel().addSound(new LevelEventSound(pos, this.soundWhistle));//TNTPrimeSound
        }

        if (smoke) {
            smoke(pos, height, 1);
        }

        Server.getInstance().getScheduler().scheduleDelayedTask(
                new FireTask(this, map, pos.add(-x, height, -z), angle, sound),
                3 * 20
        );
    }

    public void smoke(Position pos, int height, int seconds) {
        smoke(pos, height, seconds, 2);
    }

    public void smoke(Position pos, int height, int seconds, int count) {//TODO: rewrite
        NukkitRandom random = new NukkitRandom();

        for (int i = 0; i < seconds * 20; i++) {//TODO: rewrites
            Server.getInstance().getScheduler().scheduleDelayedTask(
                    new SmokeTask(this, pos.add(0, (height / (seconds * 10)) * i), count, random),
                    i
            );
        }
    }

    public void fire(HanaMap map, Position pos, int angle, boolean sound) {
        float rad = (float) Math.toRadians(angle);

        Level level = pos.level;

        List<Player> targets = new ArrayList<>(level.getChunkPlayers(pos.getFloorX() >> 4, pos.getFloorZ() >> 4).values());
        //List<Player> targets = new ArrayList<>(level.getPlayers().values());

        if (sound) {
            for (Player p : targets) {
                level.addSound(new LevelEventSound(p, this.soundFire));
            }
        }

        //particle

        pos = pos.add(0, map.getMaxY());//...

        Map<Vec2, Double> xMap = new HashMap<>();
        Map<Vec2, Double> zMap = new HashMap<>();

        Map<Vec3, BlockColor> m = map.getMap();
        for (Map.Entry<Vec3, BlockColor> e : m.entrySet()) {
            Vec3 v = e.getKey();
            Vec2 k = new Vec2(v.x, v.z);
            if (!xMap.containsKey(k)) {//for cache
                xMap.put(k, (double) v.x * MathHelper.cos(rad) - v.z * MathHelper.sin(rad));
                zMap.put(k, (double) v.x * MathHelper.sin(rad) + v.z * MathHelper.cos(rad));
            }

            level.addParticle(
                    new InstantSpellParticle(pos.add(xMap.get(k), -v.y, zMap.get(k)), e.getValue()), targets
            );
        }
    }


    //Test codes

    public void test(HanaMap map, int angle) {
        List<Double> data = new ArrayList<>();

        float rad = (float) Math.toRadians(angle);

        Map<Vec2, Double> xMap = new HashMap<>();
        Map<Vec2, Double> zMap = new HashMap<>();

        Map<Vec3, BlockColor> m = map.getMap();
        for (Map.Entry<Vec3, BlockColor> e : m.entrySet()) {
            Vec3 v = e.getKey();
            Vec2 k = new Vec2(v.x, v.z);
            if (!xMap.containsKey(k)) {//for cache
                xMap.put(k, (double) v.x * MathHelper.cos(rad) - v.z * MathHelper.sin(rad));
                zMap.put(k, (double) v.x * MathHelper.sin(rad) + v.z * MathHelper.cos(rad));
            }

            data.add(xMap.get(k));
            data.add(zMap.get(k));
        }
    }

    public void test2(List<List<List<BlockColor>>> list, int angle) {
        List<Double> data = new ArrayList<>();

        float rad = (float) Math.toRadians(angle);

        for (int x = 0; x < list.size(); x++) {
            List<List<BlockColor>> zlist = list.get(x);
            for (int z = 0; z < zlist.size(); z++) {
                List<BlockColor> ylist = zlist.get(z);

                double dx = x * MathHelper.cos(rad) - z * MathHelper.sin(rad);
                double dz = x * MathHelper.sin(rad) + z * MathHelper.cos(rad);
                for (int y = 0; y < ylist.size(); y++) {
                    BlockColor color = ylist.get(y);
                    if (color == null) {
                        continue;
                    }

                    data.add(dx);
                    data.add(dz);

                    //this.getLogger().debug("x:" + dx + ", z:" + dz);
                }
            }
        }
    }
}
