package org.ecnu.ryuou.SubtitleFileReader;

import android.text.Spanned;
import android.text.Html;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.TreeMap;
public class ParseSrt {
  /**
   * 解析SRT字幕文件
   *
   * @param srtPath
   * 字幕路径
   */
  public TreeMap<Integer, SRT> srt_map = null;

  public void parseSrt(String srtPath) {

    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(srtPath);
//      srtPath = "C:\\Users\\huangqianru\\Desktop\\ruru.srt";
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return;// 有异常，就没必要继续下去了
    }
    BufferedReader br = new BufferedReader(new InputStreamReader(
            inputStream));
    String line = null;
    srt_map = new TreeMap<Integer, SRT>();
    StringBuffer sb = new StringBuffer();
    int key = 0;
    try {
      while ((line = br.readLine()) != null) {
        if (!line.equals("")) {
          sb.append(line).append("@");
          continue;
        }

        String[] parseStrs = sb.toString().split("@");
// 该if为了适应一开始就有空行以及其他不符格式的空行情况
        if (parseStrs.length < 3) {
          sb.delete(0, sb.length());// 清空，否则影响下一个字幕元素的解析</i>
          continue;
        }

        SRT srt = new SRT();
// 解析开始和结束时间
        String timeTotime = parseStrs[1];
        int begin_hour = Integer.parseInt(timeTotime.substring(0, 2));
        int begin_mintue = Integer.parseInt(timeTotime.substring(3, 5));
        int begin_scend = Integer.parseInt(timeTotime.substring(6, 8));
        int begin_milli = Integer.parseInt(timeTotime.substring(9, 12));
        int beginTime = (begin_hour * 3600 + begin_mintue * 60 + begin_scend)
                * 1000 + begin_milli;
        int end_hour = Integer.parseInt(timeTotime.substring(17, 19));
        int end_mintue = Integer.parseInt(timeTotime.substring(20, 22));
        int end_scend = Integer.parseInt(timeTotime.substring(23, 25));
        int end_milli = Integer.parseInt(timeTotime.substring(26, 29));
        int endTime = (end_hour * 3600 + end_mintue * 60 + end_scend)
                * 1000 + end_milli;


        System.out.println("开始:" + begin_hour + ":" + begin_mintue +
                ":"
                + begin_scend + ":" + begin_milli + "=" + beginTime
                + "ms");
        System.out.println("结束:" + end_hour + ":" + end_mintue + ":"
                + end_scend + ":" + end_milli + "=" + endTime + "ms");

//解析字幕文字
        String srtBody = "";


        // 可能1句字幕，也可能2句及以上。
        for (int i = 2; i < parseStrs.length; i++) {
          srtBody += parseStrs[i] + "\n";
        }
        // 删除最后一个"\n"
        srtBody = srtBody.substring(0, srtBody.length() - 1);
// 设置SRT
        srt.setBeginTime(beginTime);
        srt.setEndTime(endTime);
        srt.setSrtBody(new String(srtBody.getBytes(), "UTF-8"));
// 插入队列
        srt_map.put(key, srt);
        key++;
        sb.delete(0, sb.length());// 清空，否则影响下一个字幕元素的解析
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void  showSRT() {
//    TextView tvSrt = (TextView) srtView.findViewById(R.id.srt);//项目中显示字幕的控件
//    int currentPosition = vv.getCurrentPosition();//vv是VideoView播放器
    Iterator<Integer> keys = srt_map.keySet().iterator();
//通过while循环遍历比较
    while (keys.hasNext()) {
      Integer key = keys.next();
      SRT srtbean = srt_map.get(key);
   // if (currentPosition > srtbean.getBeginTime()  //todo:TreeMap.keySet()是一个空指针无法将currentPosition传入showSRT()
      //       && currentPosition < srtbean.getEndTime()) {
////Html.fromHtml可以解析出字幕内容的格式
       //Spanned srtBodyHtml = Html.fromHtml(srtbean
//                .getSrtBody());
//        tvsrt.setText(srtBodyHtml);
     // break;//找到后就没必要继续遍历下去，节约资源
      //}
      System.out.println(srtbean
              .getSrtBody());
    break;//找到后就没必要继续遍历下去，节约资源
    }
  }




}

