package org.ecnu.ryuou.video;

import java.util.Locale;

//代表视频音频
public class MediaItem {
    private String name;
    private long duration;
    private  long size;
    private  String data;
    private String artist;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

  public String getFormattedDuration() {
    long temp = this.duration;
    int microSec = (int) (temp % 1000);
    temp /= 1000;
    int sec = (int) (temp % 60);
    temp /= 60;
    int min = (int) (temp % 60);
    temp /= 60;
    return String.format(Locale.CHINA, "%d:%d:%d.%d", temp, min, sec, microSec);
  }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public String toString() {
        return "MediaItem{" +
                "name='" + name + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", data='" + data + '\'' +
                ", artist='" + artist + '\'' +
                '}';
    }
}
