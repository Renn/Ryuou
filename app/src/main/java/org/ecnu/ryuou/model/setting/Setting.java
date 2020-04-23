package org.ecnu.ryuou.model.setting;

/**
 * <p>Base class of settings. Specify <code>id</code>, <code>name</code> and <code>type</code>.
 * Must be extended before use.</p>
 * <p>Attention: Once set, <code>id</code>, and <code>type</code> cannot be changed.</p>
 * <p>WARNING: Attribute <code>name</code> can be set only because of the need of initialization.
 * <br /> Setting this field to another value other than the auto-set one may cause serious problem
 * to the whole program.</p>
 */
public abstract class Setting {

  protected Integer id;
  protected String name;
  protected SettingType type;

  Setting(Integer id, String name, SettingType type) {
    this.id = id;
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public SettingType getType() {
    return type;
  }
}
