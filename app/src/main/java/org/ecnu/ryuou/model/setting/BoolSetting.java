package org.ecnu.ryuou.model.setting;

public class BoolSetting extends Setting {

  private boolean on;

  public BoolSetting(Integer id, String name, boolean on) {
    super(id, name, SettingType.BOOL);
    this.on = on;
  }

  public BoolSetting(Integer id, boolean on) {
    super(id, null, SettingType.BOOL);
    this.on = on;
  }

  public boolean isOn() {
    return on;
  }

  public void setOn(boolean on) {
    this.on = on;
  }
}
