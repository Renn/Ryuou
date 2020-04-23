package org.ecnu.ryuou.model.setting;

public class StringSetting extends Setting {

  private String content;

  public StringSetting(Integer id, String name, String content) {
    super(id, name, SettingType.STRING);
    this.content = content;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
