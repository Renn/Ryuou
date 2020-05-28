package org.ecnu.ryuou.pager;


import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;


public class SurfaceView extends android.view.SurfaceView {

    public SurfaceView(Context context) {
        this(context,null);
    }

    public SurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
    }
    public void setVideoSize(int videoWidth, int videoheight){
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width=videoWidth;
        params.height=videoheight;
        setLayoutParams(params);
    }
}
