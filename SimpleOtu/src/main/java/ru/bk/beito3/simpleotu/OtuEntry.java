package ru.bk.beito3.simpleotu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;

public class OtuEntry {

    public final static String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss x";

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("creationDate")
    @Expose
    private String creationDate;

    @SerializedName("source")
    @Expose
    private String source;

    @SerializedName("mode")
    @Expose
    private int mode;

    public OtuEntry(String name, int mode) {
        this(name, mode, null, null);
    }

    public OtuEntry(String name, int mode, OffsetDateTime creationDate) {
        this(name, mode, creationDate, null);
    }

    public OtuEntry(String name, int mode, OffsetDateTime creationDate, String source) {
        super();
        this.name = name.toLowerCase();
        this.mode = mode;
        this.creationDate = null;
        if (creationDate != null) {
            this.creationDate = creationDate.format(DateTimeFormatter.ofPattern(TIME_FORMAT));
        }
        this.source = source;
    }

    public String getName() {
        return name;
    }

    /*public void setName(String name) {
        this.name = name.toLowerCase();
    }*/

    public int getMode() {
        return this.mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
    public OffsetDateTime getCreationDate() {
        return OffsetDateTime.parse(this.creationDate, DateTimeFormatter.ofPattern(TIME_FORMAT));
        //return DateTimeFormatter.ofPattern(TIME_FORMAT).parse(this.creationDate, OffsetDateTime::from);
    }

    public void setCreationDate(OffsetDateTime creationDate) {
        if (creationDate == null) {
            this.creationDate = null;
        }

        this.creationDate = creationDate.format(DateTimeFormatter.ofPattern(TIME_FORMAT));
    }

    public String getCreationDateString() {
        return this.creationDate;
    }

    public void setCreationDateString(String creationDateString) {
        this.creationDate = creationDateString;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


    public LinkedHashMap<String, String> getMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("name", this.getName());
        map.put("mode", String.valueOf(this.getMode()));
        map.put("creationDate", this.getCreationDateString());
        map.put("source", this.getSource());

        return map;
    }

    public static OtuEntry fromMap(LinkedHashMap<String, String> map) {//bad
        String name = map.get("name");
        int mode = Integer.parseInt(map.getOrDefault("mode", "0"));

        OffsetDateTime creationDate = null;
        if(map.containsKey("creationDate")) {
            creationDate = OffsetDateTime.parse(map.get("creationDate"), DateTimeFormatter.ofPattern(TIME_FORMAT));
        }

        String source = null;
        if(map.containsKey("source")) {
            source = map.get("source");
        }

        return new OtuEntry(name, mode, creationDate, source);
    }

    @Override
    public String toString() {
        return "OtuEntry(" + "" +
                "name=" + this.name +
                ", mode=" + this.mode +
                ", creationDate=" + this.creationDate +
                ", source=" + this.source + ")";
    }
}
