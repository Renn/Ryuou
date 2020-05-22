package org.ecnu.ryuou;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.FrameLayout;
import android.widget.RadioGroup;


import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.ecnu.ryuou.base.BasePager;
import org.ecnu.ryuou.base.ReplaceFragment;
import org.ecnu.ryuou.menu.AboutActivity;
import org.ecnu.ryuou.menu.SetActivity;

import java.util.ArrayList;

import pager.ImagePager;
import pager.MusicPager;
import pager.VideoPager;

public class MainActivity extends BaseActivity{
  // Used to load the 'native-lib' library on application startup.
  static {
    System.loadLibrary("native-lib");
  }

//  load bottom
  private FrameLayout fl_main_content;
  private RadioGroup rg_bottom_tag;
  private ArrayList<BasePager> basePagers;

private  int position;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
//    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.文件夹);
    setSupportActionBar(toolbar);

    fl_main_content=(FrameLayout)findViewById(R.id.fl_main_content);
    rg_bottom_tag=(RadioGroup)findViewById(R.id.rg_bottom_tag);


    basePagers = new ArrayList<>();
    basePagers.add(new VideoPager(this));
      basePagers.add(new MusicPager(this));
      basePagers.add(new ImagePager(this));

//      set
rg_bottom_tag.setOnCheckedChangeListener(new MyOnCheckedChangeListener());

      rg_bottom_tag.check(R.id.rb_video);


  }


  class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener{

      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
       switch (checkedId){
           default:
               position=0;
               break;
           case R.id.rb_music:
               position=1;
               break;
           case R.id.rb_image:
               position=2;
               break;
       }
       setFragment();
      }
  }

    private void setFragment() {
//    android.app.FragmentManager manager = getFragmentManager();
//
      FragmentManager manager =getSupportFragmentManager();
      FragmentTransaction ft =  manager.beginTransaction();
      ft.replace(R.id.fl_main_content,new ReplaceFragment(getBasePager())).commit();

    };
//根据位置得到页面
    private BasePager getBasePager() {
      BasePager basePager = basePagers.get(position);
      if(basePager!=null&&!basePager.isInitData){
          basePager.initData();
          basePager.isInitData = true;
//          联网请求绑定数据
      }
      return basePager;
    }


    @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @SuppressLint("WrongConstant")
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }
  switch (item.getItemId()){
    case R.id.set:
     // Toast.makeText(MainActivity.this ,"设置",0).show();
      Intent intent = new Intent(MainActivity.this, SetActivity.class);
      item.setIntent(intent);
      break;
    case R.id.about:
      //Toast.makeText(MainActivity.this ,"关于",0).show();
      Intent intent2 = new Intent(MainActivity.this, AboutActivity.class);
      item.setIntent(intent2);
      break;
  }
    return super.onOptionsItemSelected(item);
  }




  /**
   * A native method that is implemented by the 'native-lib' native library, which is packaged with
   * this application.
   */
  public native String stringFromJNI();
}
