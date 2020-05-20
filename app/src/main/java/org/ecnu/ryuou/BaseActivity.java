package org.ecnu.ryuou;

import android.app.ActionBar;
import android.os.Bundle;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.ecnu.ryuou.util.ActivityController;
import org.ecnu.ryuou.util.LogUtil;

public class BaseActivity extends AppCompatActivity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    setContentView(getLayout());
//    initView();
//    initData();
//    initListener();
    LogUtil.d("BaseActivity", getClass().getSimpleName());
    ActionBar actionBar = getActionBar();
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
//  public abstract int getLayout();
//  public abstract void initView();
//  public abstract void initData();
//  public abstract void initListener();







}
