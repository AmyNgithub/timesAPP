package com.example.iapps.timesapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class TimerView extends TextView {
    private Paint mPaint;

    public TimerView(Context context)
    {
        super(context);
        init();
    }
    public TimerView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }
    public TimerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
    }



    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(10, 10, 30, mPaint);

    }

}
