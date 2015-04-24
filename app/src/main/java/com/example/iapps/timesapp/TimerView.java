package com.example.iapps.timesapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class TimerView extends TextView {
    Paint mPaint;
    public TimerView(Context context, AttributeSet attrs){
        super(context,attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(8);
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        float centerX = canvas.getWidth()/2;
        float centerY = canvas.getHeight()/2;
        float radius = centerX - 50;

        canvas.drawCircle(centerX, centerY, radius, mPaint);
    }
}
