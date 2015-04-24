package com.example.iapps.timesapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.os.Vibrator;
import android.widget.TextView;

import java.io.IOException;


public class MainActivity extends ActionBarActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mGravity;
    private TimerView timerView;
    private ImageView infoGraphics;
    private Sensor mGyro;
    private MediaPlayer mPlayer;
    private Vibrator vib;
    //private SoundPool sp;

    private boolean onTable = false;
    private boolean timerOn = false;
    private boolean alarmOn = false;

    private long minutes = 0;
    private long seconds = 0;
    private static final float NS2S = 1.0f / 1000000000.0f; //Convert nanoseconds to seconds
    private float timestamp = 0; // Last timestamp, needed for integration
    private float totalRotation = 0; //Total rotation so far
    float deltaRotation;
    private final static int DEGREES_PER_MINUTE = 5;
    private final static int UPDATES_BEFORE_TIMER_START = 13; //About 0.78 sec (1 == 60 ms)
    private final static int VIBRATE_DURATION = 50;
    private int emptyUpdates = 0;

    CountDownTimer countDown = null;


    //All kod från: http://developer.android.com/guide/topics/sensors/sensors_overview.html
    // Och massa andra ställen :D:D

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timerView = (TimerView) findViewById(R.id.activity_main_minutes);
        infoGraphics = (ImageView) findViewById(R.id.imageViewPhoneRot);
        updateTime();
        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
        mPlayer.setLooping(true);
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null){
            // Success! There's a gravity sensor.
            mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        }
        else {
            // Failure! No gravity sensor.
        }


        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            // Success! There's a gravity sensor.
            mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        else {
            // Failure! No gravity sensor.
            timerView.setText("No gyro found");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Vaskad
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            float axisz = event.values[2];
            float sum = event.values[0] + event.values[1] + event.values[2];
            float offset = 0.5f;

            if (Math.abs(axisz - sum) < offset) {
                if (axisz > 0) {
                    onTable = true;
                } else {
                    //Upside down
                    mPlayer.stop();
                    alarmOn = false;
                    try {
                        mPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                onTable = false;
                //totalRotation = 0;
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if(timestamp != 0 && onTable && !alarmOn) {
                float dT = (event.timestamp - timestamp) * NS2S;
                deltaRotation = event.values[2]*dT;
                //Kanske en if sats här

                if(event.values[2] > 0.1f || event.values[2] < -0.1f){
                    totalRotation += Math.toDegrees(deltaRotation);
                    emptyUpdates = 0;
                    if(totalRotation > DEGREES_PER_MINUTE){
                        totalRotation = 0;

                        if(countDown != null){
                            countDown.cancel();
                            timerOn = false;
                            countDown = null;
                        }

                        if(minutes > 0){
                            seconds = 0;
                            minutes--;
                            vib.vibrate(VIBRATE_DURATION);
                        }else if(seconds > 0){
                            seconds = 0;
                        }
                        updateTime();
                    }else if (totalRotation < -DEGREES_PER_MINUTE){
                        if(countDown != null){
                            countDown.cancel();
                            timerOn = false;
                            countDown = null;
                        }

                        totalRotation = 0;
                        seconds = 0;
                        minutes++;
                        vib.vibrate(VIBRATE_DURATION);

                        updateTime();
                    }
                }else if(!timerOn){
                    emptyUpdates++;
                    if(emptyUpdates > UPDATES_BEFORE_TIMER_START && (minutes != 0 || seconds != 0)){
                        emptyUpdates = 0;
                        long millis = minutes * 60000 + seconds * 1000;
                        countDown = new CountDownTimer(millis, 1000) {

                            public void onTick(long millisUntilFinished) {
                                long totalSeconds = Math.round(millisUntilFinished / 1000.0);
                                minutes = totalSeconds/60;
                                seconds = totalSeconds%60;
                                updateTime();
                            }

                            public void onFinish() {
                                alarmOn = true;

                                seconds = 0;
                                updateTime();

                                mPlayer.start();
                            }
                        };
                        timerOn = true;
                        hideImage();
                        countDown.start();
                    }
                }
            }

            // measurement done, save current time for next interval
            timestamp = event.timestamp;
        }

    }

    private void updateTime(){
        String min = String.format("%02d", minutes);
        String sec = String.format("%02d", seconds);

        timerView.setText(String.valueOf(min + ":" + sec));
        //Lägg till tick ljud och sånt
    }

    private void hideImage(){
        if (infoGraphics.getVisibility() == View.VISIBLE) {
            infoGraphics.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}

