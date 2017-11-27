package com.example.waffledefender.emotiondetectormobile;

import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * Created by waffl on 11/26/2017.
 */

public class SwipingListener implements View.OnTouchListener{

    private GestureDetector swipingListener;
    private Context _context;

    public SwipingListener(Context context) {
        swipingListener = new GestureDetector(context, new GesturesMade());
        _context = context;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return swipingListener.onTouchEvent(motionEvent);
    }

    private final class GesturesMade extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown (MotionEvent event){
        return true;
    }

        @Override
        public boolean onFling (MotionEvent mE1, MotionEvent mE2,float x, float y){
        boolean flung = false;

        try {
            float mEDiffX = mE2.getX() - mE1.getX();
            float mEDiffY = mE2.getY() - mE1.getY();

            if (Math.abs(mEDiffX) > Math.abs(mEDiffY)) {
                if (Math.abs(mEDiffX) > SWIPE_THRESHOLD && Math.abs(x) > SWIPE_VELOCITY_THRESHOLD) {
                    if (mEDiffX > 0) {
                        swipedRight();
                    } else {
                        swipedLeft();
                    }
                    flung = true;
                }
            } else if (Math.abs(mEDiffY) > SWIPE_THRESHOLD && Math.abs(y) > SWIPE_VELOCITY_THRESHOLD) {
                if (mEDiffY > 0) {
                    swipedBottom();
                } else {
                    swipedTop();
                }
                flung = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return flung;
    }

    public void swipedRight() {
    }

    public void swipedLeft() {
    }

    public void swipedBottom() {

    }

    public void swipedTop() {
        if(_context instanceof MainActivity){
            Intent intent = new Intent(_context, ChartActivity.class);
            _context.startActivity(intent);
        }
    }
}
}
