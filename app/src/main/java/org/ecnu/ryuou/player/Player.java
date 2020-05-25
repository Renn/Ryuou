package org.ecnu.ryuou.player;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.view.Surface;

public class Player implements PlayerController {

  private static Player player = null;

  static {
    System.loadLibrary("player");
  }

  private AudioTrack audioTrack = null;
  private String file = null;
  private Surface surface = null;

  private Player() {
  }


  public static Player getPlayer() {
    if (player == null) {
      player = new Player();
    }
    return player;
  }

  @Override
  public ErrorCode init(String file, Surface surface) {
    this.file = "file:" + file;
    this.surface = surface;
    initByNative(file, surface);
    return null;
  }

  @Override
  public ErrorCode start() {
    playByNative();
    return ErrorCode.SUCCESS;
  }

  @Override
  public ErrorCode pause() {
    pauseByNative();
    return ErrorCode.SUCCESS;
  }

  @Override
  public ErrorCode stop() {
    stopByNative();
//    LogUtil.e("Player","STOP button pressed");
    return ErrorCode.SUCCESS;
  }

  private native void initByNative(String path, Surface surface);

  private native void playByNative();

  private native void stopByNative();

  private native void pauseByNative();

  /**
   * Called by native code using reflection.
   */
  private void createAudioTrack(int sampleRate, int numChannels) {
    int channelConfig;
    if (numChannels == 2) {
      channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
    } else {
      channelConfig = AudioFormat.CHANNEL_OUT_MONO;
    }
    int bufferSize = AudioTrack
        .getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT);
    audioTrack = new AudioTrack.Builder().setAudioAttributes(
        new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build()).setAudioFormat(
        new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate).setChannelMask(channelConfig).build())
        .setBufferSizeInBytes(bufferSize).build();
    audioTrack.play();
  }

  /**
   * Called by native code using reflection.
   */
  private void addToAudioTrack(byte[] data, int length) {
    if (audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
      audioTrack.write(data, 0, length);
    }
  }

  /**
   * Called by native code using reflection.
   */
  private void releaseAudioTrack() {
    if (audioTrack != null) {
      if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
        audioTrack.pause();
        audioTrack.flush();
      }
      audioTrack.release();
      audioTrack = null;
    }
  }
}
