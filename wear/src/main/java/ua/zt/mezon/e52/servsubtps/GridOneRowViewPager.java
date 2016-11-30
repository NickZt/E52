package ua.zt.mezon.e52.servsubtps;

import android.content.Context;
import android.support.wearable.view.GridViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by MezM on 24.11.2016.
 */


public class GridOneRowViewPager extends GridViewPager {


    public GridOneRowViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridOneRowViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public GridOneRowViewPager(Context context) {
        super(context);
    }
    private float xDistance, yDistance, lastX, lastY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setDownPosition(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isVerticalScroll(ev))
                    return false;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setDownPosition(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isVerticalScroll(ev))
                    return false;
        }

        return super.onTouchEvent(ev);
    }

    private void setDownPosition(MotionEvent ev){
        xDistance = yDistance = 0f;
        lastX = ev.getX();
        lastY = ev.getY();
    }

    private boolean isVerticalScroll(MotionEvent ev){
        final float curX = ev.getX();
        final float curY = ev.getY();
        xDistance += Math.abs(curX - lastX);
        yDistance += Math.abs(curY - lastY);
        lastX = curX;
        lastY = curY;
        if(xDistance < yDistance) {
            return true;
        }
        return false;
    }
}