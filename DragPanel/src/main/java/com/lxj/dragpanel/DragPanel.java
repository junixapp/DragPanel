package com.lxj.dragpanel;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * Created by lixiaojun on 2018/8/17.
 */

public class DragPanel extends FrameLayout {
    private static final String TAG = "DragPanel";
    private View dragView;
    private View fixedView;
    private int maxTop, minTop, defaultTop, fixedViewMaxTop;
    private int defaultShowHeight;
    private ViewDragHelper dragHelper;
    private boolean hasShadow = true;
    private boolean isChangeAlpha = true;
    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private int endColor = Color.parseColor("#ee000000");
    private FloatEvaluator floatEvaluator = new FloatEvaluator();
    private int touchSlop;
    private int durationSlop = 250;

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
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
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
        if (headerView == null) {
            throw new IllegalArgumentException("must have a child that have a tag named HeaderView!");
        }

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

    private void calculateValues() {
        defaultTop = getMeasuredHeight() - defaultShowHeight - fixedView.getMeasuredHeight();
        minTop = getMeasuredHeight() - dragView.getMeasuredHeight() - fixedView.getMeasuredHeight();
        maxTop = getMeasuredHeight();
        fixedViewMaxTop = maxTop;
    }

    private boolean isFirstLayout = true;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!isFirstLayout) {
            // if new height is lt old height, the bottom maybe lt fixedView.getTop()
            int newBottom = dragView.getTop() + dragView.getMeasuredHeight();
            int offset = Math.max(fixedView.getTop() - newBottom, 0);
            dragView.layout(0, dragView.getTop() + offset, dragView.getRight(), newBottom + offset);
            fixedView.layout(0, fixedView.getTop(), fixedView.getRight(), fixedView.getBottom());
        } else {
            dragView.layout(0, maxTop, dragView.getMeasuredWidth(), maxTop + dragView.getMeasuredHeight());
            fixedView.layout(0, getMeasuredHeight(), fixedView.getMeasuredWidth(), getMeasuredHeight() + fixedView.getMeasuredHeight());
            isFirstLayout = false;
        }
        calculateValues();
        changeImageAlpha();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isTouchImage = isTouchInHeaderView((int) ev.getX(), (int) ev.getY());
        if (isTouchImage) {
            return headerView.getAlpha() > 0f;
        }

        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    private float downX, downY;
    private long downTime;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean isCapture = dragHelper.isViewUnder(dragView, (int) ev.getX(), (int) ev.getY());
        dragHelper.processTouchEvent(ev);

        boolean isTouchImage = isTouchInHeaderView((int) ev.getX(), (int) ev.getY());
        if (isTouchImage) {
            boolean isCanTap = headerView.getAlpha() > 0f;
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = ev.getX();
                    downY = ev.getY();
                    downTime = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_UP:
                    float dx = ev.getX() - downX;
                    float dy = ev.getY() - downY;
                    float duration = System.currentTimeMillis() - downTime;
                    float distance = (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
                    if (distance <= touchSlop && duration < durationSlop) {
                        // click event
                        if (headerClickListener != null) {
                            headerClickListener.onHeaderClick();
                        }
                    }
                    break;
            }
            return isCanTap;
        }

        return isCapture;
    }

    private boolean isTouchInHeaderView(int x, int y) {
        if (headerView.getVisibility() == GONE) {
            return false;
        }
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
            // minTop maybe gt 0.
            float startPoint = Math.max(0, minTop);
            float fraction = (dragView.getTop()-startPoint) * 1f / (maxTop - startPoint);
            if (fraction < 0f) {
                fraction = 0f;
            }

            changeShadow(fraction);

            changeImageAlpha();

            // notify listener
            notifyDragListener(fraction);

            if (changedView == dragView) {
                if (dy > 0 && top <= defaultTop) {
                    return;
                }
                moveFixedView(dy);
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            int start = Math.max(0, minTop);
            int middle = (start + defaultTop) / 2;
            if (releasedChild.getTop() > start && releasedChild.getTop() < middle) {
                dragHelper.smoothSlideViewTo(releasedChild, 0, start);
            } else if (releasedChild.getTop() > middle && releasedChild.getTop() < defaultTop) {
                dragHelper.smoothSlideViewTo(releasedChild, 0, defaultTop);
            } else if (releasedChild.getTop() > defaultTop) {
                dragHelper.smoothSlideViewTo(releasedChild, 0, maxTop);
            } else {
                dragHelper.flingCapturedView(0, minTop, 0, defaultTop);
            }
            ViewCompat.postInvalidateOnAnimation(DragPanel.this);
        }
    };

    private void notifyDragListener(float fraction) {
        if (listeners != null) {
            for (OnPanelDragListener listener : listeners){
                listener.onDragging(fraction);
                if (dragView.getTop() == defaultTop) {
                    listener.onOpen();
                } else if (dragView.getTop() == maxTop) {
                    listener.onClose();
                    reset();
                }
            }
        }
    }

    private void reset() {
        headerView.setAlpha(0);
    }

    private void changeShadow(float fraction) {
        if (!hasShadow) return;
        setBackgroundColor((Integer) argbEvaluator.evaluate(fraction, endColor, Color.TRANSPARENT));
    }

    private void changeImageAlpha() {
        if(!isChangeAlpha)return;
        //calculate drag fraction from defaultTop to minTop;
        if(dragView.getTop() > defaultTop) return;
        float distance = Math.min(defaultTop, defaultTop-minTop);
        float fraction = Math.abs(dragView.getTop()-defaultTop) * 1f / distance;
        if (fraction < 0f) {
            fraction = 0f;
        }
        if (fraction > 1f) {
            fraction = 1f;
        }
        Float val = floatEvaluator.evaluate(fraction, 0f, 1f);
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
     * @param hasShadow is show shadow below the content.
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

    private ArrayList<OnPanelDragListener> listeners;

    public void addOnPanelDragListener(OnPanelDragListener listener) {
        if(listeners==null){
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }

    public void removePanelDragListener(OnPanelDragListener listener){
        if(listeners!=null){
            listeners.remove(listener);
        }
    }

    public void removeAllListener(){
        if(listeners!=null){
            listeners.clear();
        }
        this.headerClickListener = null;
    }

    public interface OnHeaderClickListener {
        void onHeaderClick();
    }

    private OnHeaderClickListener headerClickListener;

    public void setOnHeaderClickListener(OnHeaderClickListener headerClickListener) {
        this.headerClickListener = headerClickListener;
    }

    public boolean isOpen() {
        return dragView.getTop() <= defaultTop;
    }
    public boolean isOpenToTop() {
        return dragView.getTop() == Math.max(minTop, 0);
    }

    public boolean isClose() {
        return dragView.getTop() == maxTop;
    }
    public void setIsChangeAlpha(boolean isChangeAlpha){
        this.isChangeAlpha = isChangeAlpha;
    }

}
