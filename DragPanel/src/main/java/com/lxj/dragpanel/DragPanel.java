package com.lxj.dragpanel;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

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
    private FloatEvaluator floatEvaluator = new FloatEvaluator();

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
        defaultShowHeight = dip2px(340);
    }

    View headerView;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 2) {
            throw new IllegalArgumentException("DragPanel must only have 2 children!");
        }

        dragView = getChildAt(0);
        fixedView = getChildAt(1);
        headerView = findViewWithTag("HeaderView");
        headerView.setAlpha(0f);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                calculateValues();
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateValues();
    }

    private void calculateValues(){
        defaultTop = getMeasuredHeight() - defaultShowHeight - fixedView.getMeasuredHeight();
        minTop = getMeasuredHeight() - dragView.getMeasuredHeight() - fixedView.getMeasuredHeight();
        maxTop = getMeasuredHeight();
        fixedViewMaxTop = maxTop + getMeasuredHeight() - defaultTop - fixedView.getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        dragView.layout(0, maxTop, dragView.getMeasuredWidth(), maxTop + dragView.getMeasuredHeight());
        fixedView.layout(0, getMeasuredHeight(), fixedView.getMeasuredWidth(), getMeasuredHeight() + fixedView.getMeasuredHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isTouchImage = isTouchInImageView((int) ev.getX(), (int) ev.getY());

        if (isTouchImage) {
            return headerView.getAlpha() > 0f;
        }

        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean isCapture = dragHelper.isViewUnder(dragView, (int) ev.getX(), (int) ev.getY());
        dragHelper.processTouchEvent(ev);
        return isCapture;
    }

    private boolean isTouchInImageView(int x, int y) {
        return x >= dragView.getLeft() && x < dragView.getRight() && y >= dragView.getTop()
                && y < (dragView.getTop() + headerView.getMeasuredHeight());
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
            float fraction = dragView.getTop() * 1f / maxTop;
            if (fraction < 0f) {
                fraction = 0f;
            }

            changeShadow(fraction);

            changeImageAlpha();

            // notify listener
            if (dragListener != null) {
                dragListener.onDragging(fraction);
                if (dragView.getTop() == defaultTop) {
                    dragListener.onOpen();
                } else if (dragView.getTop() == maxTop) {
                    dragListener.onClose();
                }
            }

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
            int middle = defaultTop/2;
            if(releasedChild.getTop() >=0 && releasedChild.getTop() < middle){
                dragHelper.smoothSlideViewTo(releasedChild, 0, 0);
            } else if (releasedChild.getTop() >= middle && releasedChild.getTop() < defaultTop) {
                dragHelper.smoothSlideViewTo(releasedChild, 0, defaultTop);
            } else if (releasedChild.getTop() >= defaultTop) {
                dragHelper.smoothSlideViewTo(releasedChild, 0, maxTop);
            } else {
                dragHelper.flingCapturedView(0, minTop, 0, defaultTop - 1);
            }
            ViewCompat.postInvalidateOnAnimation(DragPanel.this);


        }
    };

    private void changeShadow(float fraction) {
        if (!hasShadow) return;
        setBackgroundColor((Integer) argbEvaluator.evaluate(fraction, endColor, Color.TRANSPARENT));
    }

    private void changeImageAlpha(){
        //calculate drag fraction from defaultTop to minTop;
        float fraction = dragView.getTop()*1f / defaultTop;
        if(fraction < 0f){
            fraction = 0f;
        }
        if(fraction > 1f){
            fraction = 1f;
        }

        Float val = floatEvaluator.evaluate(1f-fraction, 0f, 1f);
        headerView.setAlpha(val);
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
     */
    public void setHasShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
    }

    public void setDefaultShowHeight(int h) {
        this.defaultShowHeight = h;
        defaultTop = getMeasuredHeight() - defaultShowHeight - fixedView.getMeasuredHeight();
    }


    public interface OnPanelDragListener {
        void onOpen();

        void onClose();

        void onDragging(float fraction);
    }

    private OnPanelDragListener dragListener;

    public void setOnPanelDragListener(OnPanelDragListener listener) {
        this.dragListener = listener;
    }
}
