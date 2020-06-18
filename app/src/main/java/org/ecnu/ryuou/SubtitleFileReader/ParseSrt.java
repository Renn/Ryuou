package org.ecnu.ryuou.SubtitleFileReader;

import android.widget.TextView;

import org.ecnu.ryuou.util.LogUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class ParseSrt {

  private LinkedList<SRT> SRTList;
  private TextView srtView;

  public ParseSrt(TextView srtView) {
    this.srtView = srtView;
    SRTList = new LinkedList<>();
  }

  public void parseSrt(String srtPath) {
    SRTList.clear();

    // open srt file
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(srtPath);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return;
    }

    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
    StringBuffer sb = new StringBuffer();
    String line = null;
    try {
      Queue<String> toParse = new LinkedList<>();
      while ((line = br.readLine()) != null) {
        if ("".equals(line)) {
          LogUtil.d("parse srt", "empty line");
          // one srt chunk has been read, start parse
          SRT srt = new SRT();

          // line1 is SRT ID
          String line1 = toParse.poll();
          if (line1 != null) {
            srt.setId(Integer.parseInt(line1));
          }

          // line2 is SRT duration
          String line2 = toParse.poll();
          if (line2 == null) {
            toParse.clear();
            continue;
          }
          String[] times = line2.split("-->");
          LogUtil.d("parse time", times[0]);
          Calendar calendar = Calendar.getInstance();
          // parse begin time
          Date date = new SimpleDateFormat("HH:mm:ss,SSS", Locale.US).parse(times[0].trim());
          if (date == null) {
            toParse.clear();
            continue;
          }
          calendar.setTime(date);
          srt.setBeginTime(calendar.getTimeInMillis() / 1000.0);
          System.out.println("begin seconds:" + calendar.getTimeInMillis());
          // parse end time
          date = new SimpleDateFormat("HH:mm:ss,SSS", Locale.US).parse(times[1].trim());
          if (date == null) {
            toParse.clear();
            continue;
          }
          calendar.setTime(date);
          srt.setEndTime(calendar.getTimeInMillis() / 1000.0);
          System.out.println("end seconds:" + calendar.getTimeInMillis());

          // line3 and later are SRT body
          String line3 = "";
          while (!toParse.isEmpty()) {
            line3 += toParse.poll() + "\n";
          }
          srt.setSrtBody(line3);

          SRTList.add(srt);
        } else {
          // put the new line into toParse
          toParse.offer(line);
        }

      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void show(double currentPosition) {
    if (currentPosition >= SRTList.peek().getBeginTime()) {
      srtView.setText(SRTList.peek().getSrtBody());
    }
    if (currentPosition >= SRTList.peek().getEndTime()) {
      srtView.setText("");
      SRTList.poll();
    }

  }

}

