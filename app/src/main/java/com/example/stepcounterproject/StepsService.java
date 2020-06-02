package com.example.stepcounterproject;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Calendar;

public class StepsService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mStepDetectorSensor;
    private StepsDBHelper mStepsDBHelper;
    private ArrayList<DateStepsModel> mStepCountList = new ArrayList<DateStepsModel>();
    private DateStepsModel today;
    protected static int steps;
    public static LocalBroadcastManager broadcaster, broadcaster2;
    static final public String COPA_RESULT = "com.example.stepcounterproject,StepsService.REQUEST_PROCESSED";
    static final public String COPA_MESSAGE = "com.example.stepcounterproject.StepsService.COPA_MSG";

    private Calendar mCalendar;
    private String todayDate;

    @Override
    public void onCreate() {
        super.onCreate();
        mCalendar = Calendar.getInstance();
        today = new DateStepsModel();
        todayDate = mCalendar.get(Calendar.DAY_OF_MONTH) + "/" + mCalendar.get(Calendar.MONTH) + 1 + "/" + mCalendar.get(Calendar.YEAR);
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            mSensorManager.registerListener(this, mStepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
            mStepsDBHelper = new StepsDBHelper(this);
        }
        broadcaster = LocalBroadcastManager.getInstance(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }
    public void sendResult(String message) {
        Intent intent = new Intent(COPA_RESULT);
        if(message != null)
            intent.putExtra(COPA_MESSAGE, message);
        broadcaster.sendBroadcast(intent);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        mStepsDBHelper.createStepsEntry();
        mStepCountList = mStepsDBHelper.readStepsEntries();
        for (DateStepsModel model : mStepCountList) {
            if (model.mDate.equals(todayDate)) {
                today = model;
            }
        }
        sendResult(String.valueOf(today.mStepCount));
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


}