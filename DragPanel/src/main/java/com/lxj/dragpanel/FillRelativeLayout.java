package com.lxj.dragpanel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class FillRelativeLayout extends RelativeLayout {
    public FillRelativeLayout(Context context) {
        super(context);
    }

    public FillRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FillRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxheight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            measureChild(v, widthMeasureSpec, heightMeasureSpec);
            maxheight = Math.max(maxheight, v.getMeasuredHeight());
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), maxheight);
    }
}
