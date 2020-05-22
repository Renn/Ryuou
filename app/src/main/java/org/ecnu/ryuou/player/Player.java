package org.ecnu.ryuou.player;

import android.view.Surface;

public class Player implements PlayerController {

  static {
    System.loadLibrary("player");
  }

  private static Player player = null;
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
    this.file = file;
    this.surface = surface;
    initByNative(file, surface);
    return null;
  }

  @Override
  public ErrorCode start() {
    playByNative();
    return null;
  }

  @Override
  public ErrorCode pause() {
    return null;
  }

  @Override
  public ErrorCode stop() {
    stopByNative();
    return null;
  }

  public native void initByNative(String path, Surface surface);

  public native void playByNative();

  public native void stopByNative();
}
