package org.ecnu.ryuou.player;

import android.view.Surface;

public class Player implements PlayerController {

  private static Player player = null;

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
    return null;
  }

  @Override
  public ErrorCode start() {
    return null;
  }

  @Override
  public ErrorCode pause() {
    return null;
  }

  @Override
  public ErrorCode stop() {
    return null;
  }
}
