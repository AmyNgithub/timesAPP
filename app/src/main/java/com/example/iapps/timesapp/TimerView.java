package com.example.iapps.timesapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.TextView;

public class TimerView extends TextView {
    Paint timePaint, bgPaint;
    long seconds;
    RectF oval;


    public TimerView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    private void init() {
        timePaint = new Paint();
        timePaint.setStyle(Paint.Style.STROKE);
        timePaint.setColor(Color.argb(1000,198,17,236));
        timePaint.setStrokeWidth(21);
        timePaint.setAntiAlias(true);

        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setColor(Color.WHITE);
        bgPaint.setStrokeWidth(20);
        bgPaint.setAntiAlias(true);

        seconds = 0;
        updateDisplayedTime();
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        float left,top,right,bottom;
        if(yNew > xNew){
            //portrait
            left = getPaddingLeft();
            right = xNew - getPaddingRight();

            float halfSide = (right-left)/2;
            float centerY = yNew/2;

            top = centerY - halfSide;
            bottom = centerY + halfSide;

        }else{
            //landscape (never used in this app)
            top = getPaddingTop();
            bottom = yNew-getPaddingBottom();

            float halfSide = (bottom-top)/2;
            float centerX = xNew/2;

            right = centerX - halfSide;
            left = centerX + halfSide;
        }

        oval = new RectF(left,top,right,bottom);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        float sweepAng = seconds/10.0f;

        canvas.drawArc(oval,0,360,false,bgPaint);
        canvas.drawArc(oval,270,sweepAng,false, timePaint);
    }

    public void setTime(long minutes,long seconds){
        this.seconds = seconds + minutes*60;
        updateDisplayedTime();
    }

    private void updateDisplayedTime(){
        long min = seconds/60;
        long sec = seconds%60;

        setText(String.valueOf(String.format("%02d", min) + ":" + String.format("%02d", sec)));
    }
}
