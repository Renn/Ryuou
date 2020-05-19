package org.ecnu.ryuou;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
public class MainApplication extends Application {

  public static final String NOTIFICATION_CHANNEL_ID = "default";
  private static Context context;

  public static Context getContext() {
    return context;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    context = getApplicationContext();
    createNotificationChannel();
  }

  private void createNotificationChannel() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      CharSequence name = getString(R.string.channel_name);
      String description = getString(R.string.channel_description);
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name,
          importance);
      channel.setDescription(description);
      // Register the channel with the system; you can't change the importance
      // or other notification behaviors after this
      getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
  }

}
