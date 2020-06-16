package org.ecnu.ryuou;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import java.util.ArrayList;
import org.ecnu.ryuou.base.BasePager;
import org.ecnu.ryuou.base.ReplaceFragment;
import org.ecnu.ryuou.menu.AboutActivity;
import org.ecnu.ryuou.menu.SetActivity;
import org.ecnu.ryuou.pager.ImagePager;
import org.ecnu.ryuou.pager.MusicPager;
import org.ecnu.ryuou.pager.VideoPager;
import org.ecnu.ryuou.util.ActivityController;

public class MainActivity extends BaseActivity {

  static {
    System.loadLibrary("player");
  }

  //  load bottom
  private FrameLayout fl_main_content;
  private RadioGroup rg_bottom_tag;
  private ArrayList<BasePager> basePagers;

  private int position;

  //  /**
//   * for player test
//   */
//  private SurfaceView surfaceView;
//  private SurfaceHolder surfaceHolder;
//  private Player player;
//  private Button btn1;//为cut按钮设定dialog监听事件
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
//    btn1=(Button) findViewById(R.id.cut);
//    btn1.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        new MaterialDialog.Builder(MainActivity.this)
//                .title("请输入截取的开始和结束时间")
//                .customView(R.layout.dialog, true)
//                .positiveText("确认")
//                .show();
//      }
//    });

    Toolbar toolbar = findViewById(R.id.folder);
    setSupportActionBar(toolbar);

    fl_main_content = findViewById(R.id.fl_main_content);
//    rg_bottom_tag = findViewById(R.id.rg_bottom_tag);

    basePagers = new ArrayList<>();
    basePagers.add(new VideoPager(this));
    basePagers.add(new MusicPager(this));
    basePagers.add(new ImagePager(this));

//      set
//    rg_bottom_tag.setOnCheckedChangeListener(new MyOnCheckedChangeListener());
//    rg_bottom_tag.check(R.id.rb_video);
    position = 0;
    setFragment();

    // permission request
    if (ContextCompat
        .checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(MainActivity.this,
          new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }
//    /**
//     * for player test
//     */
//    surfaceView = findViewById(R.id.surface_view);
//    surfaceHolder = surfaceView.getHolder();
//    player = Player.getPlayer();

  }

//  /**
//   * for player test
//   */
//  public void tryPlay(View view) {
//    String videoPath = Environment.getExternalStorageDirectory().getPath()
//        + File.separator + "Download" + File.separator + "test.mp4";
//    LogUtil.d("tryPlay", videoPath);
//    player.init(videoPath, surfaceHolder.getSurface());
//    player.seekTo(20);
//    player.start(new PlayerCallback() {
//      @Override
//      public void onProgress(double current, double total) {
//        LogUtil.d("Progress", String.format("current=%f,total=%f", current, total));
//      }
//    });
////    player.seekTo(20);
//  }
//
//  /**
//   * for player test
//   */
//  public void tryStop(View view) {
//    player.stop();
//  }
//
//  /**
//   * for player test
//   */
//  public void tryCut(View view) {
//    // permission request
//    if (ContextCompat
//        .checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        != PackageManager.PERMISSION_GRANTED) {
//      ActivityCompat.requestPermissions(MainActivity.this,
//          new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//    }
//    String videoPath = Environment.getExternalStorageDirectory().getPath()
//        + File.separator + "Download" + File.separator + "test.mp4";
//    double start = 10;
//    double dest = 25;
//    Editor editor = Editor.getEditor();
//    editor.cut(videoPath, start, dest);
//  }


  private void setFragment() {
//    android.app.FragmentManager manager = getFragmentManager();
//
    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction ft = manager.beginTransaction();
    ft.replace(R.id.fl_main_content, new ReplaceFragment(getBasePager())).commit();

  }

  //根据位置得到页面
  private BasePager getBasePager() {
    BasePager basePager = basePagers.get(position);
    if (basePager != null && !basePager.isInitData) {
      basePager.initData();
      basePager.isInitData = true;
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
    switch (item.getItemId()) {
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

  @Override
  protected void onDestroy() {
    super.onDestroy();
    ActivityController.finishAll();
  }
  //  class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
//
//    @Override
//    public void onCheckedChanged(RadioGroup group, int checkedId) {
//      switch (checkedId) {
//        default:
//          position = 0;
//          break;
//        case R.id.rb_music:
//          position = 1;
//          break;
//        case R.id.rb_image:
//          position = 2;
//          break;
//      }
//      setFragment();
//    }
//  }

}
