package org.ecnu.ryuou.pager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;
import org.ecnu.ryuou.BaseActivity;
import org.ecnu.ryuou.R;
import org.ecnu.ryuou.SubtitleFileReader.ParseSrt;
import org.ecnu.ryuou.SubtitleFileReader.SRT;
import org.ecnu.ryuou.player.Player;
import org.ecnu.ryuou.player.PlayerController;
import org.ecnu.ryuou.player.PlayerController.PlayerCallback;
import org.ecnu.ryuou.util.LogUtil;


public class SystemVideoPlayer extends BaseActivity implements android.view.View.OnClickListener {

  private static final String TAG = "SystemVideoPlayer";
  private static final int FULL_SCREEN = 1;
  private static final int PROGRESS = 1;

  static {
    System.loadLibrary("player");
  }

  /** For player */
  private SurfaceView surfaceView;
  private SurfaceHolder surfaceHolder;
  private Player player;

  /** Other views */
  private LinearLayout llTop;
  private TextView tvName;
  private ImageView ivBattery;
  private TextView tvSystemTime;
  private Button btnVoice;
  private SeekBar seekbarVoice;
  private LinearLayout llBottom;
  private TextView tvCurrentTime;
  private RelativeLayout mediaController;
  private SeekBar seekbarVideo;
  private TextView tvDuration;
  private Button btnExit;
  private Button btnVideoSwitchScreen;
  private Button btnVideoPre;
  private Button btnVideoStartPause;
  private Button btnVideoNext;
  private GestureDetector detector;

  private boolean isnotFull;
  private int screenWidth;
  private int screenHeight;
  private boolean isnotPlay;
  private double currentPosition;
  private double stop;
  private double totalPosition;
  private AudioManager am;
  private int currentVoice;
  private int maxVoice;
  private boolean isMute;
  private boolean isshowMediaController;
  private float startY;
  private float startX;
  private float touchRang;
  private int mVol;

  private final MyHandler handler = new MyHandler(this);
  private PlayerCallback playerCallback;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_system_video_player);

    isnotFull = true;
    isnotPlay = true;
    isMute = false;
    isshowMediaController = true;

    findViews();

    // 申请权限
    if (ContextCompat.checkSelfPermission(SystemVideoPlayer.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                          != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(SystemVideoPlayer.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    // get window size
    DisplayMetrics displayMetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    screenWidth = displayMetrics.widthPixels;
    screenHeight = displayMetrics.heightPixels;

    // initialize player callback
    playerCallback = new PlayerCallback() {
      @Override
      public void onProgress(double current, double total) {
        currentPosition = current;
        totalPosition = total;
        seekbarVideo.setMax((int) total);
//                      LogUtil.d("Progress", String.format("current=%f,total=%f", currentPosition, total));
        tvDuration.setText(String.format(Locale.CHINA, "%.2f", total));
        handler.sendEmptyMessage(PROGRESS);
      }
    };

    // set current volume to system volume
    am = (AudioManager)getSystemService(AUDIO_SERVICE);
    if (am != null) {
      currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
      maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }
    seekbarVoice.setMax(maxVoice);
    seekbarVoice.setProgress(currentVoice);

    // set seek bar listeners
    setListener();

    // initialize player
    player = Player.getPlayer();
    final Uri uri = getIntent().getData();
    if (uri == null) {
      Toast.makeText(this, "Intent has no data.", Toast.LENGTH_SHORT).show();
      finish();
    }
    surfaceHolder = surfaceView.getHolder();
    surfaceHolder.addCallback(new Callback() {

      @Override
      public void surfaceCreated(SurfaceHolder holder) {
        player.init(uri.toString(), holder.getSurface());
        player.start(playerCallback);
        btnVideoStartPause.setBackgroundResource(R.drawable.btn_pause_start_selector);
        isnotPlay = false;
      }

      @Override
      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

      }

      @Override
      public void surfaceDestroyed(SurfaceHolder holder) {

      }
    });

    // set gesture detector
    detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

      @Override
      public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
      }

      @Override
      public boolean onDoubleTap(MotionEvent e) {
        if (isnotPlay) {
          btnVideoStartPause.setBackgroundResource(R.drawable.btn_pause_start_selector);
          player.start(playerCallback);
        } else {
          player.pause();
          btnVideoStartPause.setBackgroundResource(R.drawable.btn_start_pause_selector);
        }
        isnotPlay = !isnotPlay;
        return false;
      }

      @Override
      public boolean onSingleTapConfirmed(MotionEvent e) {
        if (isshowMediaController) {
          mediaController.setVisibility(View.GONE);
        } else {
          mediaController.setVisibility(View.VISIBLE);
        }
        isshowMediaController = !isshowMediaController;
        return false;
      }
    });

    //TODO:将字幕显示在activity_system_video_player的TextView，@+id/srtView
    ParseSrt test = new ParseSrt();
    test.parseSrt(Environment.getExternalStorageDirectory().getPath()
        + File.separator + "Download" + File.separator + "test.srt");
    // test.showSRT(currentPosition);
    TreeMap<Integer, SRT> srt_map = test.srt_map;
    Iterator<Integer> keys = srt_map.keySet().iterator();
    while (keys.hasNext()) {
      TextView text = this.findViewById(R.id.srtView);
      Integer key = keys.next();
      SRT srtbean = srt_map.get(key);
      text.setText(srtbean.getSrtBody());
      System.out.println(srtbean.getSrtBody());
    }
  }

  // 手势快捷键
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    detector.onTouchEvent(event);

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        startY = event.getY();
        startX = event.getX();
        mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        touchRang = Math.min(screenHeight, screenWidth);
//                handler.removeMessages(HIDE_MEDIACONTROLLER);
        break;
      case MotionEvent.ACTION_MOVE:
        float endY = event.getY();
        float distanceY = startY - endY;
        if (startX > screenWidth / 2.0) {
          float delta = (distanceY / touchRang) * maxVoice;
          int voice = (int) Math.min(Math.max(mVol + delta, 0), maxVoice);
          if (delta != 0) {
            isMute = false;
            updateVoice(voice, false);
          }
        } else {
          LogUtil.d("touch","to set brightness");
          final double FLING_MIN_DISTANCE = 0.5;
          final double FLING_MIN_VELOCITY = 0.5;
          if (distanceY > FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
            setBrightness(30);
          }
          if (distanceY < FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
            setBrightness(-30);
          }
        }

        break;
      case MotionEvent.ACTION_UP:
//                handler.removeMessages(HIDE_MEDIACONTROLLER,4000);
        break;
    }

    return super.onTouchEvent(event);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_voice:
        isMute = !isMute;
        updateVoice(currentVoice, isMute);
        break;
      case R.id.btn_video_start_pause:
        if (isnotPlay) {
          player.start(playerCallback);
          btnVideoStartPause.setBackgroundResource(R.drawable.btn_pause_start_selector);
        }
        else {
          player.pause();
          btnVideoStartPause.setBackgroundResource(R.drawable.btn_start_pause_selector);
        }
        isnotPlay = !isnotPlay;
        break;
      case R.id.btn_exit:
        onBackPressed();
        break;
      default:
        break;
    }
  }

  private void findViews() {
    surfaceView = findViewById(R.id.surface_view);
    llTop = findViewById(R.id.ll_top);
    llBottom = findViewById(R.id.ll_bottom);

    tvName = findViewById(R.id.tv_name);
    tvCurrentTime = findViewById(R.id.tv_current_time);
    tvDuration = findViewById(R.id.tv_duration);

    seekbarVoice = findViewById(R.id.seekbar_voice);
    seekbarVideo = findViewById(R.id.seekbar_video);

    btnVoice = findViewById(R.id.btn_voice);
    btnVoice.setOnClickListener(this);

    btnExit = findViewById(R.id.btn_exit);
    btnExit.setOnClickListener(this);

    btnVideoStartPause = findViewById(R.id.btn_video_start_pause);
    btnVideoStartPause.setOnClickListener(this);

    mediaController = findViewById(R.id.md_controller);
//        ivBattery = (ImageView)findViewById( R.id.iv_battery );
//        tvSystemTime = (TextView)findViewById( R.id.tv_system_time );
    //    btnVideoPre = findViewById(R.id.btn_video_pre);
//    btnVideoNext = findViewById(R.id.btn_video_next);
//        btnVideoSwitchScreen = (Button)findViewById( R.id.btn_video_switch_screen );



//    btnVideoPre.setOnClickListener(this);

//    btnVideoNext.setOnClickListener(this);
//        btnVideoSwitchScreen.setOnClickListener( this );

  }

  public void setBrightness(float brightness) {
    LogUtil.d("touch","in set brightness");
    WindowManager.LayoutParams lp = getWindow().getAttributes();
    lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
    if (lp.screenBrightness > 1) {
      lp.screenBrightness = 1;
    } else if (lp.screenBrightness < 0.1) {
      lp.screenBrightness = (float) 0.1;
    }
    getWindow().setAttributes(lp);

//    float sb = lp.screenBrightness;
//            brightnessTextView.setText((int) Math.ceil(sb * 100) + "%");
  }

  private void setListener() {
    seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());
    seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());
  }

  private void updateVoice(int progress, boolean isMute) {
    if (isMute) {
      am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
      seekbarVoice.setProgress(0);
    } else {
      am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
      seekbarVoice.setProgress(progress);
      currentVoice = progress;
    }
  }

  @SuppressLint("SourceLockedOrientationActivity")
  private void setVideoType(int fullScreen) {
    if (isnotFull) {
      isnotFull = !isnotFull;
      surfaceView.setVideoSize(screenWidth, screenHeight);
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      btnVideoSwitchScreen.setBackgroundResource(R.drawable.jz_enlarge);
    } else {
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
      surfaceView.setVideoSize(screenWidth, screenHeight);
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      btnVideoSwitchScreen.setBackgroundResource(R.drawable.jz_enlarge);
    }
  }

  class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      if (fromUser) {
        isMute = progress <= 0;
        updateVoice(progress, isMute);
      }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
  }

  class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      if (fromUser) {
        // todo:暂停的时候拖动进度条，进度条回弹回去。。但是快进功能是正常的
        player.seekTo(progress);
      }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
  }

  private static class MyHandler extends Handler {

    private final WeakReference<SystemVideoPlayer> refActivity;

    public MyHandler(SystemVideoPlayer systemVideoPlayer) {
      refActivity = new WeakReference<SystemVideoPlayer>(systemVideoPlayer);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
      super.handleMessage(msg);
      SystemVideoPlayer systemVideoPlayer = refActivity.get();
      if (msg.what == PROGRESS) {
        systemVideoPlayer.seekbarVideo.setProgress((int)systemVideoPlayer.currentPosition);

        LogUtil.d("Progress", String.format(Locale.CHINA, "current=%f,total=%f,%d",
                  systemVideoPlayer.currentPosition, systemVideoPlayer.totalPosition, PROGRESS));

        systemVideoPlayer.tvCurrentTime.setText(String.format("%.2f", systemVideoPlayer.currentPosition));
        removeMessages(PROGRESS);
        sendEmptyMessageDelayed(PROGRESS, 1000);
      }
    }
  }
}

