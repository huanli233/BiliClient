package com.RobinNotBad.BiliClient.model;

public class SettingSection {
    public final String type;
    public final String id;
    public final String name;
    public final String desc;
    public final String defaultValue;

    public SettingSection(String type, String name, String key, String desc, String defaultValue) {
        this.type = type;
        this.id = key;
        this.name = name;
        this.desc = desc;
        this.defaultValue = defaultValue;
    }
}
