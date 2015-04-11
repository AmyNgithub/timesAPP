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


public class MainActivity extends ActionBarActivity implements SensorEventListener {
    private TextView infoView, rotationView;
    private SensorManager mSensorManager;
    private Sensor mGravity, mRotation;

    //All kod från: http://developer.android.com/guide/topics/sensors/sensors_overview.html

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoView = (TextView) findViewById(R.id.info_view);
        infoView.setText("Not on table");
        rotationView = (TextView) findViewById(R.id.rotation_view);
        rotationView.setText("Not changed");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null){
            // Success! There's a gravity sensor.
            mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        }
        else {
            // Failure! No gravity sensor.
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null){
            // Success! There's a gravity sensor.
            mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }
        else {
            // Failure! No rotation sensor.
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
        //Intent i = new Intent(getApplicationContext(), RotationActivity.class);
        if(event.sensor.getType() == Sensor.TYPE_GRAVITY){
            float axisz = event.values[2];
            float sum = event.values[0] + event.values[1] + event.values[2];
            float offset = 0.5f;

            if(Math.abs(axisz - sum) < offset){
                if(axisz > 0){
                    infoView.setText("On table");
                    //startActivity(i);
                }else{
                    infoView.setText("Upside down");
                }

            }else{
                infoView.setText("Not on table");
            }
        }else if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            float[] mRotationMatrix = new float[9];
            float[] orientationVals = new float[3];
            SensorManager.getRotationMatrixFromVector(mRotationMatrix,event.values);
            SensorManager.remapCoordinateSystem(mRotationMatrix,SensorManager.AXIS_X,SensorManager.AXIS_Z,mRotationMatrix);
            SensorManager.getOrientation(mRotationMatrix,orientationVals);

            orientationVals[0] = (float) Math.toDegrees(orientationVals[0]);
            orientationVals[1] = (float) Math.toDegrees(orientationVals[1]);
            orientationVals[2] = (float) Math.toDegrees(orientationVals[2]);

            rotationView.setText(String.valueOf(orientationVals[0]));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
