package org.ecnu.ryuou.pager;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.ecnu.ryuou.BaseActivity;
import org.ecnu.ryuou.R;
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
    private int screenWidth = 0;
    private int screenHeight = 0;
    private boolean isnotPlay;
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
              String videoPath = Environment.getExternalStorageDirectory().getPath()
                      + File.separator + "Download" + File.separator + "test.mp4";
              LogUtil.d("tryPlay", videoPath);
              isnotPlay = !isnotPlay;

              player.init(videoPath, surfaceHolder.getSurface());
              player.start();
          }
//        imageButton1.setVisibility(view.INVISIBLE);
//        imageButton2.setVisibility(view.VISIBLE);
            else{
                player.stop();
              btnVideoStartPause.setBackgroundResource(R.drawable.btn_start_pause_selector);
          }


        } else if ( v == btnVideoNext ) {
            // Handle clicks for btnVideoNext
        } else if ( v == btnVideoSwitchScreen ) {
            // Handle clicks for btnVideoSwitchScreen
            setVideoType(FULL_SCREEN);
            
        }
    }

    private void setVideoType(int fullScreen) {
       if(isnotFull){
           isnotFull = !isnotFull;
           surfaceView.setVideoSize(screenWidth,screenHeight);
           setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
           setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
           btnVideoSwitchScreen.setBackgroundResource(R.drawable.jz_enlarge);
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_video_player);
//        ImageButton imageButton1 = findViewById(R.id.imageButton1);
//        ImageButton imageButton2 = findViewById(R.id.imageButton2);
        isnotFull=true;
        isnotPlay=true;
        findViews();

        if (ContextCompat
                .checkSelfPermission(SystemVideoPlayer.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SystemVideoPlayer.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
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
    player.start();
  }

  public void tryStop(View view) {
    player.stop();
  }




//
                            }

