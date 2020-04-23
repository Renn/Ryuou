package org.ecnu.ryuou;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import org.ecnu.ryuou.util.ActivityController;
import org.ecnu.ryuou.util.LogUtil;

public class BaseActivity extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LogUtil.d("BaseActivity", getClass().getSimpleName());
    ActionBar actionBar = getSupportActionBar();
//    if (actionBar != null) {
//      actionBar.hide();
//    }
    ActivityController.addActivity(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    ActivityController.removeActivity(this);
  }
}
