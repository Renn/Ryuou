package org.ecnu.ryuou.pager;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
//import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.RelativeLayout;
import android.view.WindowManager;
import android.view.GestureDetector;
import android.media.AudioManager;
import android.view.MotionEvent;
import org.ecnu.ryuou.BaseActivity;
import org.ecnu.ryuou.MainActivity;
import org.ecnu.ryuou.R;
import org.ecnu.ryuou.SubtitleFileReader.ParseSrt;
import org.ecnu.ryuou.SubtitleFileReader.SRT;
import org.ecnu.ryuou.player.Player;
import org.ecnu.ryuou.player.PlayerController;
import org.ecnu.ryuou.util.LogUtil;

import java.io.File;
import java.util.Iterator;
import java.util.TreeMap;

//implements android.view.View.OnClickListener
public class SystemVideoPlayer extends BaseActivity implements android.view.View.OnClickListener  {
//    全屏

    private static final int FULL_SCREEN =1 ;
    private static final int PROGRESS = 1;

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
    private RelativeLayout media_controller;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnExit;
    private boolean isnotFull ;
    private Button btnVideoPre;
    private Button btnVideoStartPause;
    private Button btnVideoNext;
//    private  double current;
//    private  double total;
    private Button btnVideoSwitchScreen;
    private int screenWidth = 0;
    private int screenHeight = 0;
    private boolean isnotPlay;
    private double currentPosition;
    private double totalPosition;
    private AudioManager am;
    private  int currentVoice;
    private  int maxVoice;
    private boolean isMute = false;
    private boolean isshowMediaController = false;
    private GestureDetector detector;
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
        media_controller=findViewById(R.id.media_controller);
        btnVoice.setOnClickListener( this );

        btnExit.setOnClickListener( this );
        btnVideoPre.setOnClickListener( this );
        btnVideoStartPause.setOnClickListener( this );
        btnVideoNext.setOnClickListener( this );
        btnVideoSwitchScreen.setOnClickListener( this );
        detector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {


                if (isnotPlay) {
                    btnVideoStartPause.setBackgroundResource(R.drawable.btn_pause_start_selector);
                    String videoPath = Environment.getExternalStorageDirectory().getPath()
                            + File.separator + "Download" + File.separator + "test.mp4";
                    LogUtil.d("tryPlay", videoPath);
                    isnotPlay = !isnotPlay;

                    player.init(videoPath, surfaceHolder.getSurface());
//              player.start();
                    player.seekTo(10);
                    player.start(new PlayerController.PlayerCallback() {
                        @Override
                        public void onProgress(double current, double total) {
//TODO：进度条以及进度总时间随视频大小动态显示
                            currentPosition = current;
                            totalPosition = total;
                            seekbarVideo.setMax((int) total);
                            hideMediaController();
//                      LogUtil.d("Progress", String.format("current=%f,total=%f", currentPosition, total));
                            tvDuration.setText(String.format("%.2f", total));
                            handler.sendEmptyMessage(PROGRESS);
                        }
                    });


                }
//        imageButton1.setVisibility(view.INVISIBLE);
//        imageButton2.setVisibility(view.VISIBLE);
                else {
                    isnotPlay = !isnotPlay;
                    player.stop();
                    btnVideoStartPause.setBackgroundResource(R.drawable.btn_start_pause_selector);
                }

                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return super.onSingleTapConfirmed(e);
            }
        });
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
            isMute = !isMute;
            updateVoice(currentVoice,isMute);
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
//              player.start();
              player.seekTo(10);
              player.start(new PlayerController.PlayerCallback() {
                  @Override
                  public void onProgress(double current, double total) {

                      currentPosition=  current;
                      totalPosition =  total;
                      seekbarVideo.setMax((int) total);
//                      LogUtil.d("Progress", String.format("current=%f,total=%f", currentPosition, total));
                      tvDuration.setText(String.format("%.2f",total));
                      handler.sendEmptyMessage(PROGRESS);
                  }
              });


          }
//        imageButton1.setVisibility(view.INVISIBLE);
//        imageButton2.setVisibility(view.VISIBLE);
            else{
              isnotPlay = !isnotPlay;
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

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case PROGRESS:
                    seekbarVideo.setProgress((int) currentPosition);

                    LogUtil.d("Progress", String.format("current=%f,total=%f,%d", currentPosition, totalPosition,
                            PROGRESS));
                    tvCurrentTime.setText(String.format("%.2f",currentPosition));
                    removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS,1000);
                    break;

            }
        }
    };
    public void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
        } else if (lp.screenBrightness < 0.1) {
            lp.screenBrightness = (float) 0.1;
        }
        getWindow().setAttributes(lp);

        float sb = lp.screenBrightness;
//            brightnessTextView.setText((int) Math.ceil(sb * 100) + "%");
    }
    private float startY;
    private float startX;
    private float touchRang;
    private int mVol;
    @Override
    public boolean onTouchEvent(MotionEvent event){
        detector.onTouchEvent(event);

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                startX = event.getX();
                mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                touchRang=Math.min(screenHeight,screenWidth);
//                handler.removeMessages(HIDE_MEDIACONTROLLER);
                break;
            case MotionEvent.ACTION_MOVE:
                float endY = event.getY();
                float distanceY = startY-endY;
                if(startX > screenWidth / 2){
                    float delta = (distanceY/touchRang)*maxVoice;
                    int voice = (int)Math.min(Math.max(mVol+delta,0),maxVoice);
                    if(delta!=0){
                        isMute = false;
                        updateVoice(voice,false);
                    }}
                else {
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    if (distanceY > FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(10);
                    }
                    if (distanceY < FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(-10);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
//                handler.removeMessages(HIDE_MEDIACONTROLLER,4000);
                break;
        }

        return super.onTouchEvent(event);
    }

    private void setListener(){
        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());
        seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());
    }
    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                if(progress>0){
                    isMute = false;
                }
                else{
                    isMute = true;
                }
                updateVoice(progress,isMute);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
//            handler.removeMessages(HIDE_MESIACONTROLLER,4000);

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private void updateVoice(int progress,boolean isMute) {
        if(isMute){
            am.setStreamVolume(AudioManager.STREAM_MUSIC,0,0);
            seekbarVoice.setProgress(0);
        }
        else{
            am.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
            seekbarVoice.setProgress(progress);
            currentVoice=progress;
        }

    }

    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                player.seekTo(progress);
//                zhuyi
//                tvCurrentTime.setText(String.format("%.2f",progress));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

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
            am = (AudioManager) getSystemService(AUDIO_SERVICE);
            currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            seekbarVoice.setMax(maxVoice);
            seekbarVoice.setProgress(currentVoice);
            setListener();

        surfaceView = findViewById(R.id.surface_view);
        surfaceHolder = surfaceView.getHolder();
        player = Player.getPlayer();

      //TODO:将字幕显示在activity_system_video_player的TextView，@+id/srtView
            ParseSrt test = new ParseSrt();
            test.parseSrt(Environment.getExternalStorageDirectory().getPath()
                    + File.separator + "Download" + File.separator + "test.srt");
            // test.showSRT(currentPosition);
            TreeMap<Integer, SRT> srt_map = test.srt_map;
            Iterator<Integer> keys = srt_map.keySet().iterator();
            while (keys.hasNext()) {
                TextView text = (TextView) this.findViewById(R.id.srtView);
                Integer key = keys.next();
                SRT srtbean = srt_map.get(key);
                text.setText(srtbean.getSrtBody());
                System.out.println(srtbean.getSrtBody());
            }


    }


    public void tryPlay(View view) {
        String videoPath = Environment.getExternalStorageDirectory().getPath()
                + File.separator + "Download" + File.separator + "test.mp4";
        LogUtil.d("tryPlay", videoPath);
        player.init(videoPath, surfaceHolder.getSurface());
        player.seekTo(20);
        player.start(new PlayerController.PlayerCallback() {
            @Override
            public void onProgress(double current, double total) {
                LogUtil.d("Progress", String.format("current=%f,total=%f", current, total));
            }
        });
//    player.seekTo(20);
    }

  public void tryStop(View view) {
    player.stop();
  }
    public void tryCut(View view) {
        // permission request
        if (ContextCompat
                .checkSelfPermission(SystemVideoPlayer.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SystemVideoPlayer.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        String videoPath = Environment.getExternalStorageDirectory().getPath()
                + File.separator + "Download" + File.separator + "test.mp4";
        double start = 10;
        double dest = 25;
            Editor editor = Editor.getEditor();
        player.cut(videoPath, start, dest);
    }


//    隐藏

        private void showMediaController(){
            media_controller.setVisibility(surfaceView.VISIBLE);
            isshowMediaController=true;
        }
        private void hideMediaController(){
            media_controller.setVisibility(surfaceView.GONE);
            isshowMediaController=false;
        }


//
                            }

