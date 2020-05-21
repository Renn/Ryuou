package org.ecnu.ryuou;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioGroup;


import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.ecnu.ryuou.base.BasePager;
import org.ecnu.ryuou.base.ReplaceFragment;
import org.ecnu.ryuou.player.Player;

import java.io.File;
import java.util.ArrayList;

import pager.ImagePager;
import pager.MusicPager;
import pager.VideoPager;

public class MainActivity extends BaseActivity{
  // Used to load the 'native-lib' library on application startup.
  static {
    System.loadLibrary("player");
  }

//  load bottom
  private FrameLayout fl_main_content;
  private RadioGroup rg_bottom_tag;
  private ArrayList<BasePager> basePagers;

  private  int position;

  // for player test
  private SurfaceView surfaceView;
  private SurfaceHolder surfaceHolder;
  private Player player;

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

    // permission request
    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission. READ_EXTERNAL_STORAGE }, 1);
    }

    // for player test
    surfaceView = (SurfaceView) findViewById(R.id.surface_view);
    surfaceHolder = surfaceView.getHolder();
    player = Player.getPlayer();
    /*
    surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
        String videoPath = Environment.getExternalStorageDirectory().getPath()
                + File.separator + "Download" + File.separator + "20200518171532.mp4";
        //String videoPath = Environment.getExternalStorageDirectory().getPath()
        //        + File.separator + "Download" + File.separator + "20200521135921.mp4";
        // 确保调用 jni 方法时 Surface 已经被创建，避免 NullPointerException
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            player.teststart(videoPath, holder.getSurface());
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    });*/
  }

  public void tryPlay(View view) {
    String videoPath = Environment.getExternalStorageDirectory().getPath()
            + File.separator + "Download" + File.separator + "20200518171532.mp4";
    player.init(videoPath, surfaceHolder.getSurface());
    player.start();
  }

  public void tryStop(View view) {
      player.stop();
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
      Intent intent2 = new Intent(MainActivity.this, aboutActivity.class);
      item.setIntent(intent2);
      break;
  }
    return super.onOptionsItemSelected(item);
  }




  /**
   * A native method that is implemented by the 'native-lib' native library, which is packaged with
   * this application.
   */
  //public native String stringFromJNI();
}
