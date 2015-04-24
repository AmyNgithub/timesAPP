package com.example.iapps.timesapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.TextView;

public class TimerView extends TextView {
    private final static float OFFSET = 50f;
    Paint timePaint, bgPaint;
    long seconds;


    public TimerView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    private void init() {
        timePaint = new Paint();
        timePaint.setStyle(Paint.Style.STROKE);
        timePaint.setColor(Color.MAGENTA);
        timePaint.setStrokeWidth(13);
        timePaint.setAntiAlias(true);

        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setColor(Color.WHITE);
        bgPaint.setStrokeWidth(12);
        bgPaint.setAntiAlias(true);

        seconds = 0;
        updateDisplayedTime();
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        float left,top,right,bottom;
        if(canvas.getHeight() > canvas.getWidth()){
            //portrait
            left = OFFSET;
            right = canvas.getWidth()-OFFSET;

            float halfSide = (right-left)/2;
            float centerY = canvas.getHeight()/2;

            top = centerY - halfSide;
            bottom = centerY + halfSide;

        }else{
            //landscape (never used in this app)
            top = OFFSET;
            bottom = canvas.getHeight()-OFFSET;

            float halfSide = (bottom-top)/2;
            float centerX = canvas.getWidth()/2;

            right = centerX - halfSide;
            left = centerX + halfSide;
        }

        RectF oval = new RectF(left,top,right,bottom);

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
