package org.ecnu.ryuou;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.ecnu.ryuou.base.BasePager;
import org.ecnu.ryuou.base.ReplaceFragment;
import org.ecnu.ryuou.menu.AboutActivity;
import org.ecnu.ryuou.menu.SetActivity;
import org.ecnu.ryuou.video.VideoPager;
import org.ecnu.ryuou.util.ActivityController;

public class MainActivity extends BaseActivity {

  static {
    System.loadLibrary("player");
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.folder);
    setSupportActionBar(toolbar);

    // open video pager by default
    BasePager videoPager = new VideoPager(this);
    videoPager.initData();
    videoPager.isInitData = true;
    setFragment(videoPager);

    // permission request
    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(MainActivity.this,
          new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }
  }

  private void setFragment(BasePager basePager) {
    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction ft = manager.beginTransaction();
    ft.replace(R.id.fl_main_content, new ReplaceFragment(basePager)).commit();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @SuppressLint("WrongConstant")
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.set:
        Intent intent = new Intent(MainActivity.this, SetActivity.class);
        item.setIntent(intent);
        break;
      case R.id.about:
        Intent intent2 = new Intent(MainActivity.this, AboutActivity.class);
        item.setIntent(intent2);
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy() {
    ActivityController.finishAll();
    super.onDestroy();
  }
}
