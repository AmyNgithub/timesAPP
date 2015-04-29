package com.example.iapps.timesapp;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.os.Vibrator;

import java.io.IOException;


public class MainActivity extends ActionBarActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mGravity;
    private Sensor mProximity; //Proximity sensor
    private TimerView timerView;
    private ImageView infoGraphics;
    private Sensor mGyro;
    private MediaPlayer mPlayer;
    private Vibrator vib;
    private PowerManager.WakeLock fullWakeLock;
    private PowerManager.WakeLock partialWakeLock;
    //private SoundPool sp;

    private boolean onTable = false;
    private boolean timerOn = false;
    private boolean alarmOn = false;
    private boolean rotationLock = false;

    private long minutes = 0;
    private long seconds = 0;
    private static final float NS2S = 1.0f / 1000000000.0f; //Convert nanoseconds to seconds
    private float timestamp = 0; // Last timestamp, needed for integration
    private float totalRotation = 0; //Total rotation so far
    float deltaRotation;
    private final static int DEGREES_PER_MINUTE = 5;
    private final static int UPDATES_BEFORE_TIMER_START = 13; //About 0.78 sec (1 == 60 ms)
    private final static int VIBRATE_DURATION = 50;
    private final static int TICKS_TO_LOCK = 3;

    private int emptyUpdates = 0;
    private int lockTicks = 0;

    CountDownTimer countDown = null;


    //All kod från: http://developer.android.com/guide/topics/sensors/sensors_overview.html
    // Och massa andra ställen :D:D

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timerView = (TimerView) findViewById(R.id.activity_main_minutes);
        infoGraphics = (ImageView) findViewById(R.id.imageViewPhoneRot);
        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
        mPlayer.setLooping(true);
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        createWakeLocks();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            // Success! There's a gravity sensor.
            mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        } else {
            // Failure! No gravity sensor.
        }


        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            // Success! There's a gravity sensor.
            mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        } else {
            // Failure! No gyro sensor.
            timerView.setText("No gyro found");
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
            // Success! There's a Proximity sensor.
            mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            //mSensorManager.registerListener(this,mProximity,SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            //failure! No proximity sensor.
            timerView.setText("No proximity found");
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
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            float axisz = event.values[2];
            float sum = event.values[0] + event.values[1] + event.values[2];
            float offset = 0.5f;

            if (Math.abs(axisz - sum) < offset) {
                if (axisz > 0) {
                    onTable = true;
                }

            } else {
                onTable = false;
                //totalRotation = 0;
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if (timestamp != 0 && onTable && !alarmOn && !rotationLock) { //Lock rotation is done here!
                float dT = (event.timestamp - timestamp) * NS2S;
                deltaRotation = event.values[2] * dT;
                //Kanske en if sats här

                if (event.values[2] > 0.1f || event.values[2] < -0.1f) {
                    totalRotation += Math.toDegrees(deltaRotation);
                    emptyUpdates = 0;
                    if (totalRotation > DEGREES_PER_MINUTE) {
                        totalRotation = 0;

                        if (countDown != null) {
                            countDown.cancel();
                            timerOn = false;
                        }

                        if (minutes > 0) {
                            seconds = 0;
                            minutes--;
                            vib.vibrate(VIBRATE_DURATION);
                        } else if (seconds > 0) {
                            seconds = 0;
                        }
                        timerView.setTime(minutes, seconds);
                    } else if (totalRotation < -DEGREES_PER_MINUTE) {
                        if (countDown != null) {
                            countDown.cancel();
                            timerOn = false;
                        }

                        totalRotation = 0;
                        seconds = 0;
                        minutes++;
                        vib.vibrate(VIBRATE_DURATION);

                        timerView.setTime(minutes, seconds);

                    }
                } else if (!timerOn) {
                    emptyUpdates++;
                    if (emptyUpdates > UPDATES_BEFORE_TIMER_START && (minutes != 0 || seconds != 0)) {
                        emptyUpdates = 0;
                        long millis = minutes * 60000 + seconds * 1000;
                        countDown = new CountDownTimer(millis, 1000) {

                            public void onTick(long millisUntilFinished) {
                                long totalSeconds = Math.round(millisUntilFinished / 1000.0);
                                minutes = totalSeconds / 60;
                                seconds = totalSeconds % 60;
                                timerView.setTime(minutes, seconds);

                                if (!rotationLock) {
                                    lockTicks++;
                                    if (lockTicks >= TICKS_TO_LOCK) {
                                        rotationLock = true;
                                        lockTicks = 0;
                                    }
                                }
                            }

                            public void onFinish() {
                                alarmOn = true;

                                //Wakes up phone
                                wakeDevice();
                                //Sets the app in foreground
                                //http://stackoverflow.com/questions/12074980/bring-application-to-front-after-user-clicks-on-home-button
                                //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                //startActivity(intent);

                                seconds = 0;
                                timerView.setTime(minutes, seconds);

                                mPlayer.start();
                            }
                        };
                        timerOn = true;
                        rotationLock = true;
                        hideImage();
                        countDown.start();

                    }
                }
            }

            // measurement done, save current time for next interval
            timestamp = event.timestamp;
        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY && event.values[0] == 0) {
            if (alarmOn) {
                mPlayer.stop();
                alarmOn = false;
                timerOn = false;
                rotationLock = false;

                try {
                    mPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (timerOn) {
                rotationLock = false;
            }
        }

    }

    private void hideImage() {
        if (infoGraphics.getVisibility() == View.VISIBLE) {
            infoGraphics.setVisibility(View.INVISIBLE);
        }
    }
    // Found at http://stackoverflow.com/questions/14741612/android-wake-up-and-unlock-device
    // Called from onCreate
    protected void createWakeLocks(){
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        fullWakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "Loneworker - FULL WAKE LOCK");
        partialWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Loneworker - PARTIAL WAKE LOCK");
    }
    // Called whenever we need to wake up the device
    public void wakeDevice() {
        fullWakeLock.acquire();

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
        keyguardLock.disableKeyguard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_UI);

        // Called implicitly when device is about to wake up or foregrounded
        if(fullWakeLock.isHeld()){
            fullWakeLock.release();
        }
        if(partialWakeLock.isHeld()){
            partialWakeLock.release();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        // Called implicitly when device is about to sleep or application is backgrounded
        partialWakeLock.acquire();
    }
}

