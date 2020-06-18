package org.ecnu.ryuou.editor;

public class Editor implements EditorInterface {

  private static Editor editor = null;
  private String outfile;

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

  public String getOutfile() {
    return this.outfile;
  }

  @Override
  public ErrorCode cut(String file, double start, double dest) {
    outfile = cutByNative(file, start, dest);
    return ErrorCode.SUCCESS;
  }

  private native String cutByNative(String file, double start, double dest);
}
