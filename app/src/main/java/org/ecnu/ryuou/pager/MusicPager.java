package org.ecnu.ryuou.pager;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import org.ecnu.ryuou.base.BasePager;

public class MusicPager extends BasePager {
    private TextView textView;
    public MusicPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        textView=new TextView(context);
        textView.setTextSize(25);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.RED);
        return textView;
    }

    @Override
    public void initData() {
        super.initData();

        textView.setText("截取片段");
    }
}
