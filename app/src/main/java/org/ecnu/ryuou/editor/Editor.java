package org.ecnu.ryuou.editor;

public class Editor implements EditorInterface {

  private static Editor editor = null;

  static {
    System.loadLibrary("editor");
  }

  private Editor() {
  }

  public static Editor getEditor() {
    if (editor == null) {
      editor = new Editor();
    }
    return editor;
  }

  @Override
  public ErrorCode cut(String file, double start, double dest) {
    cutByNative(file, start, dest);
    return ErrorCode.SUCCESS;
  }

  private native void cutByNative(String file, double start, double dest);
}
