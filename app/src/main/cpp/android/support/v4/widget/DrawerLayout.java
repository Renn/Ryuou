package android.support.v4.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

class DrawerLayout extends View {
    public DrawerLayout(Context context) {
        this(context, null);
    }

    public DrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
