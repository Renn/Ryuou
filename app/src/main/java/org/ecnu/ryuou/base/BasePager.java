package org.ecnu.ryuou.base;

import android.content.Context;
import android.view.View;

public abstract class BasePager{
    public final Context context;
    public View rootView;
    public boolean isInitData;

    public BasePager(Context context){
        this.context=context;
        rootView=initView();
    }


    public abstract View initView();
    public void initData(){};

}

