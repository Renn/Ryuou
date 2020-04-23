package org.ecnu.ryuou.util;

import android.app.Activity;
import java.util.HashMap;
import java.util.Map;
import org.ecnu.ryuou.R;
import org.ecnu.ryuou.model.setting.BoolSetting;
import org.ecnu.ryuou.model.setting.Setting;

public class Configuration {

  private static BoolSetting notifications = new BoolSetting(R.string.notifications,
      true);  // Notification function.
  private static BoolSetting notificationSound = new BoolSetting(R.string.sound,
      true); // Sound. Valid only if notification function is on.
  private static BoolSetting notificationVibrate = new BoolSetting(R.string.vibrate,
      false);  // Vibrate. Valid only if notification function is on.
  private static Map<Integer, Setting> settingMap = new HashMap<>();

  static {
    settingMap.put(R.string.notifications, notifications);
    settingMap.put(R.string.sound, notificationSound);
    settingMap.put(R.string.vibrate, notificationVibrate);
  }

  public static Setting getSetting(Integer id) {
    return settingMap.get(id);
  }

  public static void updateSetting(Setting setting) {
    settingMap.put(setting.getId(), setting);
  }

  public static void init(Activity activity) {
    for (Integer id : settingMap.keySet()) {
      Setting setting = settingMap.get(id);
      assert setting != null;
      setting.setName(activity.getResources().getString(id));
      settingMap.put(id, setting);
    }
  }
}
