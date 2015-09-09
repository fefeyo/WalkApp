package com.archetypenova.samplewatchapp;

import android.app.Activity;
import android.app.Notification;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

    private TextView mTextView, mTargetText;
    private SensorManager sm;
    private Sensor step;

    private SharedPreferences mPreferences;

    private float mCount;
    private int targetCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mTargetText = (TextView) stub.findViewById(R.id.target);
                mTextView.setText("現在"+mCount+"歩");
                mTargetText.setText("目標まで残り" + targetCount + "歩");
                mTargetText.setTypeface(Typeface.createFromAsset(getAssets(), "sao.otf"));
                mTextView.setTypeface(Typeface.createFromAsset(getAssets(), "sao.otf"));
                sm.registerListener(
                        MainActivity.this,
                        step,
                        SensorManager.SENSOR_DELAY_UI
                );
            }
        });

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        step = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        mPreferences = getSharedPreferences("pocket", MODE_PRIVATE);
        final float prevWalkCount = mPreferences.getFloat("walk_count", -1);
        if (-1 != prevWalkCount) {
            mCount = prevWalkCount;
        } else {
            mCount = 0;
        }
        final int prevTargetCount = mPreferences.getInt("target_count", -1);
        if (-1 != prevTargetCount) {
            targetCount = prevTargetCount;
        }else{
            targetCount = 30;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            mCount += event.values[0];
            mTextView.setText(mCount + "");
        }
        if(mCount > 30){
            final Notification n = new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle("おめでとうございます！")
                    .setContentText("目標値に到達しました！")
                    .setSmallIcon(R.drawable.ic_full_sad)
                    .build();
            NotificationManagerCompat.from(getApplicationContext()).notify(0, n);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putFloat("walk_count", mCount);
        editor.putInt("target_count", targetCount);
        editor.apply();
    }
}
