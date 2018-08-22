package com.lxj.dragpanel;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by hhkx01 on 2018/8/18.
 */

public class FillLinearLayout extends LinearLayout {
    public FillLinearLayout(Context context) {
        super(context);
    }

    public FillLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FillLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(getOrientation()==HORIZONTAL){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }else {
            int total = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View v = getChildAt(i);
                if(v.getVisibility()==GONE)continue;
                measureChild(v, widthMeasureSpec, heightMeasureSpec);
                total += v.getMeasuredHeight();
            }
            int width = MeasureSpec.getSize(widthMeasureSpec);
            setMeasuredDimension(width, total);
        }

    }
}
