package org.ecnu.ryuou.pager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.TreeMap;
import java.lang.String;

import java.util.Iterator;
import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
//import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.ecnu.ryuou.BaseActivity;
import org.ecnu.ryuou.R;
import org.ecnu.ryuou.SubtitleFileReader.ParseSrt;
import org.ecnu.ryuou.SubtitleFileReader.SRT;
import org.ecnu.ryuou.player.Player;
import org.ecnu.ryuou.util.LogUtil;

import java.io.File;

//implements android.view.View.OnClickListener
public class SystemVideoPlayer extends BaseActivity implements android.view.View.OnClickListener  {
//    全屏

  private static final int FULL_SCREEN =1 ;

  static {
    System.loadLibrary("player");
  }
  private SurfaceView surfaceView;
  private SurfaceHolder surfaceHolder;
  private Player player;

  private LinearLayout llTop;
  private TextView tvName;
  private ImageView ivBattery;
  private TextView tvSystemTime;
  private Button btnVoice;
  private SeekBar seekbarVoice;
  private LinearLayout llBottom;
  private TextView tvCurrentTime;
  private SeekBar seekbarVideo;
  private TextView tvDuration;
  private Button btnExit;
  private boolean isnotFull ;
  private Button btnVideoPre;
  private Button btnVideoStartPause;
  private Button btnVideoNext;
  private Button btnVideoSwitchScreen;
  private TextView srtView;
  private int screenWidth = 0;
  private int screenHeight = 0;
  private boolean isnotPlay;
  private String videoPath = Environment.getExternalStorageDirectory().getPath()
          + File.separator + "Download" + File.separator + "test.mp4";
  ////    /**
////     * Find the Views in the layout<br />
////     * <br />
////     * Auto-created on 2020-05-28 00:08:02 by Android Layout Finder
////     * (http://www.buzzingandroid.com/tools/android-layout-finder)
////     */
  private void findViews() {
    llTop = (LinearLayout)findViewById( R.id.ll_top );
    tvName = (TextView)findViewById( R.id.tv_name );
    ivBattery = (ImageView)findViewById( R.id.iv_battery );
    tvSystemTime = (TextView)findViewById( R.id.tv_system_time );
    btnVoice = (Button)findViewById( R.id.btn_voice );
    seekbarVoice = (SeekBar)findViewById( R.id.seekbar_voice );
    llBottom = (LinearLayout)findViewById( R.id.ll_bottom );
    tvCurrentTime = (TextView)findViewById( R.id.tv_current_time );
    seekbarVideo = (SeekBar)findViewById( R.id.seekbar_video );
    tvDuration = (TextView)findViewById( R.id.tv_duration );
    btnExit = (Button)findViewById( R.id.btn_exit );
    btnVideoPre = (Button)findViewById( R.id.btn_video_pre );
    btnVideoStartPause = (Button)findViewById( R.id.btn_video_start_pause );
    btnVideoNext = (Button)findViewById( R.id.btn_video_next );
    btnVideoSwitchScreen = (Button)findViewById( R.id.btn_video_switch_screen );

    btnVoice.setOnClickListener( this );

    btnExit.setOnClickListener( this );
    btnVideoPre.setOnClickListener( this );
    btnVideoStartPause.setOnClickListener( this );
    btnVideoNext.setOnClickListener( this );
    btnVideoSwitchScreen.setOnClickListener( this );
  }

  ////    /**
////     * Handle button click events<br />
////     * <br />
////     * Auto-created on 2020-05-28 00:08:02 by Android Layout Finder
////     * (http://www.buzzingandroid.com/tools/android-layout-finder)
////     */
  @Override
  public void onClick(View v) {
    if ( v == btnVoice ) {
//            // Handle clicks for btnVoice
    } else if ( v == btnExit ) {
//            // Handle clicks for btnExit
    } else if ( v == btnVideoPre ) {
//            // Handle clicks for btnVideoPre
    } else if ( v == btnVideoStartPause ) {
//            // Handle clicks for btnVideoStartPause
//            surfaceView.setVideoSize(100,200);
      if(isnotPlay){
        btnVideoStartPause.setBackgroundResource(R.drawable.btn_pause_start_selector);

        LogUtil.d("tryPlay", videoPath);
        isnotPlay = !isnotPlay;

        player.init(videoPath, surfaceHolder.getSurface());
     //   player.start();

      }

      else{
        player.stop();
        btnVideoStartPause.setBackgroundResource(R.drawable.btn_start_pause_selector);
      }

    } else if ( v == btnVideoNext ) {

    } else if ( v == btnVideoSwitchScreen ) {
      // Handle clicks for btnVideoSwitchScreen
      setVideoType(FULL_SCREEN);

    }
  }

  private void setVideoType(int fullScreen) {
    if(isnotFull){
      isnotFull = !isnotFull;
      surfaceView.setVideoSize(screenWidth,screenHeight);
    //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      btnVideoSwitchScreen.setBackgroundResource(R.drawable.jz_enlarge);
    }
    else{
//           int mVideoWidth =110;
//           int mvideoHeight = 50 ;
//           int width = screenWidth;
//           int height = screenHeight;
//
//           if(mvideoHeight*height<width*mvideoHeight){
//               width=height*mVideoWidth/mvideoHeight;
//           }
//           else if(mVideoWidth*height>width*mvideoHeight){
//               height = width*mvideoHeight/mVideoWidth;
//           }

//           surfaceView.setVideoSize(width,height);
      surfaceView.setVideoSize(screenWidth,screenHeight);
     // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      btnVideoSwitchScreen.setBackgroundResource(R.drawable.jz_enlarge);
    }
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    TextView text;
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_system_video_player);
   ParseSrt test=new ParseSrt();
   test.parseSrt("C:\\Users\\huangqianru\\Desktop\\ruru.srt");  //TODO:将字幕显示在activity_system_video_player的TextView，@+id/srtView
  //  test.showSRT();
  //TreeMap<Integer, SRT> srt_map=test.srt_map;
   //Iterator<Integer> keys = srt_map.keySet().iterator();
  //  while (keys.hasNext()) {
    //text=(TextView)this.findViewById(R.id.srtView);
    //  Integer key = keys.next();
    //  SRT srtbean = srt_map.get(key);
    //  text.setText(srtbean.getSrtBody());
    //  System.out.println(srtbean
     //         .getSrtBody());
   // }

    isnotFull=true;
    isnotPlay=true;
    findViews();

    if (ContextCompat
            .checkSelfPermission(SystemVideoPlayer.this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(SystemVideoPlayer.this,
              new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }


    long run = getLocalVideoDuration(videoPath);
//       LogUtil.d("System");

//        get width
    DisplayMetrics displayMetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    screenWidth=displayMetrics.widthPixels;
    screenHeight=displayMetrics.heightPixels;

    surfaceView = findViewById(R.id.surface_view);
    surfaceHolder = surfaceView.getHolder();
    player = Player.getPlayer();


  }


  public void tryPlay(View view) {
    String videoPath = Environment.getExternalStorageDirectory().getPath()
            + File.separator + "Download" + File.separator + "test.mp4";
    LogUtil.d("tryPlay", videoPath);
//    surfaceView.setVideoSize(100,200);
    player.init(videoPath, surfaceHolder.getSurface());
  //  player.start();
  }


  public void tryStop(View view) {
    player.stop();
  }

  public static int getLocalVideoDuration(String videoPath) {
//除以 1000 返回是秒
    int duration;
    try {
      MediaMetadataRetriever mmr = new  MediaMetadataRetriever();
      mmr.setDataSource(videoPath);
      duration = Integer.parseInt(mmr.extractMetadata
              (MediaMetadataRetriever.METADATA_KEY_DURATION))/1000;

//时长(毫秒)
//String duration = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
//宽
      String width = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
//高
      String height = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
    return duration;
  }




//
}

