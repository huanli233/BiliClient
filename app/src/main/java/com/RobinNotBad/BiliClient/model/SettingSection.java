package com.RobinNotBad.BiliClient.model;

public class SettingSection {
    public String type;
    public String id;
    public String name;
    public String desc;
    public String defaultValue;

    public SettingSection(String type, String name, String key, String desc, String defaultValue) {
        this.type = type;
        this.id = key;
        this.name = name;
        this.desc = desc;
        this.defaultValue = defaultValue;
    }
}
