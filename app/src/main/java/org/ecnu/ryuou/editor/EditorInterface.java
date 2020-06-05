package org.ecnu.ryuou.editor;

public interface EditorInterface {

  /**
   * Cut and save frames from <code>start</code> to <code>dest</code> of <code>file</code></code>
   * (in seconds).
   *
   * @return ErrorCode to indicate the result
   */
  ErrorCode cut(String file, double start, double dest);
}
