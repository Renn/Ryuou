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

  /**
   * Seek to the frame specified by <code>dest</code> (in seconds).
   *
   * @return ErrorCode to indicate the result
   */
  ErrorCode seekTo(double dest);

  /**
   * Cut and save frames from <code>start</code> to <code>dest</code> of <code>file</code></code> (in seconds).
   *
   * @return ErrorCode to indicate the result
   */
  ErrorCode cut(String file, double start, double dest);

  interface PlayerCallback {

    void onProgress(double current, double total);
  }
}
