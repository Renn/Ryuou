package org.ecnu.ryuou.player;

import android.view.Surface;

public interface PlayerController {

  /**
   * Initialize the player with <code>file</code> and the <code>surface</code>.
   *
   * @return ErrorCode to indicate the result
   */
  ErrorCode init(String file, Surface surface);

  /**
   * Start or continue.
   *
   * @return ErrorCode to indicate the result
   */
  ErrorCode start(PlayerCallback callback);

  /**
   * Pause.
   *
   * @return Error
   */
  ErrorCode pause();

  /**
   * Stop and release all allocated resources.
   *
   * @return ErrorCode to indicate the result
   */
  ErrorCode stop();

  interface PlayerCallback {

    void onProgress(double current, double total);
  }
}
