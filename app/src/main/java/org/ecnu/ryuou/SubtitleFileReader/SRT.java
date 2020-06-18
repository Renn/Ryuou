package org.ecnu.ryuou.SubtitleFileReader;

public class SRT {

    private int id;
    private double beginTime;
    private double endTime;
    private String srtBody;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(double beginTime) {
        this.beginTime = beginTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public String getSrtBody() {
        return srtBody;
    }

    public void setSrtBody(String srtBody) {
        this.srtBody = srtBody;
    }

    @Override
    public String toString() {
        return "" + beginTime + ":" + endTime + ":" + srtBody;
    }

}
