package com.lxj.dragpanel;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by lixiaojun on 2018/8/17.
 */

public class DragPanel extends FrameLayout {
    private View dragView;
    private View fixedView;
    private int maxTop, minTop, defaultTop, fixedViewMaxTop;
    private int defaultShowHeight;
    private ViewDragHelper dragHelper;
    private boolean hasShadow = true;
    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private int endColor = Color.parseColor("#ee000000");

    public DragPanel(@NonNull Context context) {
        super(context);
        init();
    }

    public DragPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragPanel(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        dragHelper = ViewDragHelper.create(this, cb);
        defaultShowHeight = dip2px(240);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 2) {
            throw new IllegalArgumentException("DragPanel must only have 2 children!");
        }

        dragView = getChildAt(0);
        fixedView = getChildAt(1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        defaultTop = getMeasuredHeight() - defaultShowHeight - fixedView.getMeasuredHeight();
        minTop = getMeasuredHeight() - dragView.getMeasuredHeight() - fixedView.getMeasuredHeight();
        maxTop = getMeasuredHeight();
        fixedViewMaxTop = maxTop + getMeasuredHeight() - defaultTop - fixedView.getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        dragView.layout(0, defaultTop, dragView.getMeasuredWidth(), defaultTop + dragView.getMeasuredHeight());
        fixedView.layout(0, getMeasuredHeight() - fixedView.getMeasuredHeight(), fixedView.getMeasuredWidth(), getMeasuredHeight());

        changeShadow();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean isCapture = dragHelper.isViewUnder(dragView, (int) ev.getX(), (int) ev.getY());
        dragHelper.processTouchEvent(ev);
        return isCapture;
    }

    ViewDragHelper.Callback cb = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == dragView;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return 1;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (top > maxTop) {
                top = maxTop;
            }
            if (top < minTop) {
                top = minTop;
            }
            return top;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            changeShadow();

            if (changedView == dragView) {
                if (dy > 0 && top < defaultTop) {
                    return;
                }
                moveFixedView(dy);
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            int middle = getMeasuredHeight() / 2;
            if (releasedChild.getTop() >= middle && releasedChild.getTop() < defaultTop) {
                dragHelper.smoothSlideViewTo(releasedChild, 0, defaultTop);
            } else if (releasedChild.getTop() >= defaultTop) {
                dragHelper.smoothSlideViewTo(releasedChild, 0, maxTop);
            } else {
                dragHelper.flingCapturedView(0, minTop, 0, defaultTop - 1);
            }
            ViewCompat.postInvalidateOnAnimation(DragPanel.this);


        }
    };

    private void changeShadow() {
        if(!hasShadow)return;

        float fraction = dragView.getTop() * 1f / maxTop;
        if (fraction < 0f) {
            fraction = 0f;
        }
        setBackgroundColor((Integer) argbEvaluator.evaluate(fraction, endColor, Color.TRANSPARENT));
    }

    private void moveFixedView(int dy) {
        int t = fixedView.getTop() + dy;

        if (t > fixedViewMaxTop) {
            t = fixedViewMaxTop;
        }
        if (t < (getMeasuredHeight() - fixedView.getMeasuredHeight())) {
            t = getMeasuredHeight() - fixedView.getMeasuredHeight();
        }
        fixedView.layout(fixedView.getLeft(), t, fixedView.getRight(), t + fixedView.getMeasuredHeight());
    }

    /**
     * close DragPanel to maxTop, and hide it.
     */
    public void close() {
        dragHelper.smoothSlideViewTo(dragView, 0, maxTop);
        ViewCompat.postInvalidateOnAnimation(DragPanel.this);
    }

    /**
     * open DragPanel to defaultTop.
     */
    public void open() {
        dragHelper.smoothSlideViewTo(dragView, 0, defaultTop);
        ViewCompat.postInvalidateOnAnimation(DragPanel.this);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(DragPanel.this);
        }
    }

    public int dip2px(float dpValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * set DragPanel has shadow bg.
     * @param hasShadow
     */
    public void setHasShadow(boolean hasShadow){
        this.hasShadow = hasShadow;
    }

    public void setDefaultShowHeight(int h){
        this.defaultShowHeight = h;
    }


}
