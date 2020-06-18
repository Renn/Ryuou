package org.ecnu.ryuou.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaScanner {

  private volatile static MediaScanner instance;

  private MediaScanner(){
  }

  public static MediaScanner getInstace(){
    synchronized (MediaScanner.class){
      if(instance == null){
        instance = new MediaScanner();
      }
    }
    return instance;
  }


  /**
   * 扫描一个媒体文件
   * @param filePath 要扫描的媒体文件
   */
  public void scanFile(Context context, ScanFile filePath) {
    List<ScanFile> filePaths = new ArrayList<ScanFile>(1);
    filePaths.add(filePath);
    scanFiles(context, filePaths);
  }

  /**
   * 扫描多个媒体文件
   * @param filePaths 要扫描的文件列表
   */
  public void scanFiles(Context context, List<ScanFile> filePaths){
    SannerClient client = new SannerClient(context, filePaths);
    client.connectAndScan();
  }


  /**
   * 媒体文件扫描对象构造器
   */
  public static class ScanFile{

    /**
     * 要扫描的媒体文件路劲或包含媒体文件的文件夹路径
     */
    public String filePaths;

    /**
     * 要扫描的媒体文件类型 eg: audio/mp3  media/*  application/ogg
     *             image/jpeg  image/png  video/mpeg   video/3gpp
     *             ......
     */
    public String mineType;

    public ScanFile(String filePaths, String mineType) {
      this.filePaths = filePaths;
      this.mineType = mineType;
    }
  }

  public class SannerClient implements
      MediaScannerConnection.MediaScannerConnectionClient {

    /**
     * 要扫描的文件或文件夹
     */
    private List<ScanFile> scanFiles = null;

    /**
     * 实际要扫描的单个文件集合
     */
    private List<ScanFile> filePaths = null;

    private MediaScannerConnection mediaScanConn;

    public SannerClient(Context context, List<ScanFile> scanFiles) {
      this.scanFiles = scanFiles;
      mediaScanConn = new MediaScannerConnection(context, this);
    }

    /**
     * 连接MediaScanner并开始扫描
     */
    public void connectAndScan(){
      if(scanFiles != null && !scanFiles.isEmpty()){
        this.filePaths = new ArrayList<ScanFile>();

        //遍历取得单个文件集合
        for(ScanFile sf : scanFiles){
          findFile(sf);
        }

        mediaScanConn.connect();
      }
    }

    private void findFile(ScanFile file){
      File f = new File(file.filePaths);
      if(f.isFile()){
        filePaths.add(file);

      }else{
        File[] fs = f.listFiles();
        if(fs != null && fs.length > 0){
          for(File cf : fs){
            findFile(new ScanFile(cf.getAbsolutePath(), file.mineType));
          }
        }
      }
    }

    private void scanNext(){
      if(filePaths != null && !filePaths.isEmpty()){
        ScanFile sf = filePaths.remove(filePaths.size() - 1);
        mediaScanConn.scanFile(sf.filePaths, sf.mineType);

      }else{
        mediaScanConn.disconnect();
      }
    }

    @Override
    public void onMediaScannerConnected() {
      scanNext();
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
      // TODO Auto-generated method stub
      scanNext();

      //媒体扫描完成可以配合EventBus等消息通讯工具发出通知,也可接收Intent.ACTION_MEDIA_SCANNER_FINISHED的广播
      //EventBus.getDefault().post(new EventMediaScanCompleted(path));
    }
  }
}