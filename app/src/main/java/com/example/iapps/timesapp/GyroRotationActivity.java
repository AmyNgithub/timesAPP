package com.example.iapps.timesapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.TimerTask;


public class GyroRotationActivity extends ActionBarActivity implements SensorEventListener {
    private TextView rotationView;
    private TextView minutesView;
    private TextView countView;
    private SensorManager mSensorManager;
    private Sensor mGyro;
    int minutes = 0;
    float deltaRotation;


    private static final float NS2S = 1.0f / 1000000000.0f; //Convert nanoseconds to seconds
    private float timestamp = 0; // Last timestamp, needed for integration
    private float totalRotation = 0; //Total rotation so far

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyro_rotation);
        minutesView = (TextView) findViewById(R.id.minute_view);
        countView = (TextView) findViewById(R.id.count_view);
        rotationView = (TextView) findViewById(R.id.gyro_rot_view);
        rotationView.setText("Not changed");
        countDown counter = new countDown();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            // Success! There's a gravity sensor.
            mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            counter.run(); //Körs, men försvinner sen och dyker inte upp igen
        }
        else {
            // Failure! No gravity sensor.
            rotationView.setText("No gyro found");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gyro_rotation, menu);
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

        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if(timestamp != 0) {
                float dT = (event.timestamp - timestamp) * NS2S;
                deltaRotation = event.values[2]*dT;
                //Kanske en if sats här

                if(event.values[2] > 0.1f || event.values[2] < -0.1f){
                    totalRotation += deltaRotation;
                    rotationView.setText(String.valueOf(Math.round(Math.toDegrees(totalRotation))));
                    minutes = Math.round(Math.round(Math.toDegrees(totalRotation)) / 2);
                    minutesView.setText(String.valueOf(minutes + " minutes"));
                }
            }

            // measurement done, save current time for next interval
            timestamp = event.timestamp;
        }

    }




    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);

    }

    class countDown extends TimerTask{ //Vet ej om detta fungerar...

        long remaining = minutes; // How many milliseconds remain in the countdown.
        long now = System.currentTimeMillis(); // current time in ms
        long lastUpdate = now; // When count was last updated
        long elapsed = now - lastUpdate; // ms elapsed since last update

        public void run(){
            if(deltaRotation == 0 ){
                float tempTime = System.currentTimeMillis() + 2000;
                while(System.currentTimeMillis()!=tempTime && Math.round(Math.toDegrees(totalRotation)) != 0){
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(System.currentTimeMillis()==tempTime){
                    remaining -= elapsed; // adjust remaining time
                    lastUpdate = now; // remember this update time
                    // Convert remaining milliseconds to mm:ss format and display
                    if (remaining < 0) remaining = 0;
                    int minutes = (int)(remaining/60000);
                    int seconds = (int)((remaining)/1000);

                    minutesView.setText(String.valueOf(minutes + " : " + seconds));
                }


            }
        }


    }

}
