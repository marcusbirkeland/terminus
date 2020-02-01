package com.example.jobbkalender.DataClasses;

public class Setting {
    private String settingName;
    private int settingImageResource;

    public Setting(String settingName, int settingImageResource) {
        this.settingName = settingName;
        this.settingImageResource = settingImageResource;
    }

    public String getSettingName() {
        return settingName;
    }

    public void setSettingName(String settingName) {
        this.settingName = settingName;
    }

    public int getSettingImageResource() {
        return settingImageResource;
    }

    public void setSettingImageResource(int settingImageResource) {
        this.settingImageResource = settingImageResource;
    }
}
