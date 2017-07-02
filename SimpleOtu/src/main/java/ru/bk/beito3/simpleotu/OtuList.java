package ru.bk.beito3.simpleotu;

/*
 * SimpleOtu
 *
 * Copyright (c) 2017 beito
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
*/

import cn.nukkit.utils.Logger;
import cn.nukkit.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;

public class OtuList {

    public final static int MODE_OTU = 0;
    public final static int MODE_RUNA = 1;
    public final static int MODE_OTU_AND_RUNA = 2;

    LinkedHashMap<String, OtuEntry> list = new LinkedHashMap<>();

    File file;

    Logger logger = null;

    public OtuList(File file, Logger logger) {
        this.file = file;
        this.logger = logger;
    }

    public LinkedHashMap<String, OtuEntry> getEntries() {
        return this.list;
    }

    public List<OtuEntry> getEntryList() {
        return new ArrayList<>(this.list.values());
    }

    public List<String> getOtuNames() {
        List<String> list = new ArrayList<>();

        LinkedHashMap<String, OtuEntry> entries = this.getEntries();
        for(Map.Entry<String, OtuEntry> entry : entries.entrySet()) {
            int mode = entry.getValue().getMode();
            if(mode == MODE_OTU || mode == MODE_OTU_AND_RUNA) {
                list.add(entry.getValue().getName());
            }
        }

        return list;
    }

    public List<String> getRunaNames() {
        List<String> list = new ArrayList<>();

        LinkedHashMap<String, OtuEntry> entries = this.getEntries();
        for(Map.Entry<String, OtuEntry> entry : entries.entrySet()) {
            int mode = entry.getValue().getMode();
            if(mode == MODE_RUNA || mode == MODE_OTU_AND_RUNA) {
                list.add(entry.getValue().getName());
            }
        }

        return list;
    }

    public OtuEntry getEntry(String name) {
        return this.list.get(name.toLowerCase());
    }

    public OffsetDateTime getCreationDate(String name) {
        return this.getEntry(name).getCreationDate();
    }

    public String getCreationDateString(String name) {
        return this.getEntry(name).getCreationDateString();
    }

    public String getSource(String name) {
        return this.getEntry(name).getSource();
    }

    public int getMode(String name) {
        return this.getEntry(name).getMode();
    }

    public void setMode(String name, int mode) {
        this.getEntry(name).setMode(mode);
    }

    public String getReason(String name) {
        return this.getEntry(name).getReason();
    }

    public void setReason(String name, String reason) {
        this.getEntry(name).setReason(reason);
    }

    public boolean isOtu(String name) {
        int mode = this.getMode(name);

        return mode == MODE_OTU || mode == MODE_OTU_AND_RUNA;
    }

    public boolean isRuna(String name) {
        int mode = this.getMode(name);

        return mode == MODE_RUNA || mode == MODE_OTU_AND_RUNA;
    }

    public void add(OtuEntry entry) {
        this.list.put(entry.getName(), entry);
    }

    public void remove(String name) {
        list.remove(name.toLowerCase());
    }

    public boolean exists(String name) {
        return this.list.containsKey(name.toLowerCase());
    }

    public void addOtu(String name, int mode) {
        this.add(new OtuEntry(name, mode));
    }

    public void addOtu(String name, int mode, OffsetDateTime creationDate) {
        this.add(new OtuEntry(name, mode, creationDate));
    }

    public void addOtu(String name, int mode, OffsetDateTime creationDate, String source) {
        this.add(new OtuEntry(name, mode, creationDate, source));
    }

    public void load() {
        this.list = new LinkedHashMap<>();

        if (!this.file.getParentFile().exists()) {
            this.file.mkdirs();
        }

        try {
            if(!this.file.exists()) {
                this.file.createNewFile();
                this.save();
            } else {
                List<OtuEntry> entries = new Gson().fromJson(Utils.readFile(this.file), new TypeToken<List<OtuEntry>>(){}.getType());

                for (OtuEntry e : entries) {
                    if(e.getName() != null) {
                        this.list.put(e.getName(), e);
                    }
                }
            }
        } catch (IOException e) {
            this.logger.error("Could not load the otulist from " + this.file.getAbsolutePath(), e);
        }
    }

    public void save() {
        if (!this.file.getParentFile().exists()) {
            this.file.mkdirs();
        }

        try {
            //Utils.writeFile(this.file, new Gson().toJson(this.list));

            List<LinkedHashMap<String, String>> entries = new LinkedList<>();
            for(Map.Entry<String, OtuEntry> entry : this.list.entrySet()) {
                entries.add(entry.getValue().getMap());
            }

            Utils.writeFile(this.file, new ByteArrayInputStream(
                    new GsonBuilder()
                            .setPrettyPrinting()//format print
                            .excludeFieldsWithoutExposeAnnotation()//use annotation
                            .create().toJson(entries)//convert to json
                            .getBytes(StandardCharsets.UTF_8)));//convert to UTF-8
        } catch (IOException e) {
            this.logger.error("Could not save the otulist to " + this.file.getAbsolutePath(), e);
        }
    }
}
