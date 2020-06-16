package org.ecnu.ryuou;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;


public class SplashActivity extends BaseActivity {

  private Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      if (isFinishing()) {
        return;
      }
      Intent intent = new Intent(MainApplication.getContext(), MainActivity.class);
      startActivity(intent);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏，一定要在setContentView之前
    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }
    setContentView(R.layout.activity_splash);
    handler.sendMessageDelayed(Message.obtain(), 3000);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    handler.removeCallbacksAndMessages(null);
  }
}