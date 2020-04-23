package org.ecnu.ryuou.util;

import android.app.Activity;
import java.util.ArrayList;
import java.util.List;

public class ActivityController {

  private static List<Activity> activityList = new ArrayList<>();

  public static void addActivity(Activity activity) {
    activityList.add(activity);
  }

  public static void removeActivity(Activity activity) {
    activityList.remove(activity);
  }

  public static void finishAll() {
    for (Activity activity : activityList) {
      if (!activity.isFinishing()) {
        activity.finish();
      }
    }
    activityList.clear();
    //Kill the current process to ensure the program is closed
    android.os.Process.killProcess(android.os.Process.myPid());
  }
}
