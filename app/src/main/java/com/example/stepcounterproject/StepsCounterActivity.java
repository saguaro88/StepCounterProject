package com.example.stepcounterproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StepsCounterActivity extends AppCompatActivity{

    private DateStepsModel today;
    private Button localizationButton;
    private TextView stepsCounter, dailyGoalTextView;
    private float progress;
    private int  dailyGoal;
    private ArrayList<DateStepsModel> mStepCountList;
    private StepsDBHelper mStepsDBHelper;
    private BroadcastReceiver receiver;
    private Calendar mCalendar;
    private String todayDate;
    private double longitude, latitude;
    private ProgressBar progressBar;
    Location gps_loc;
    Location network_loc;
    Criteria criteria;
    LocationManager locationManager;
    Location final_loc;


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fragment_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.history:
                Intent mIntent = new Intent(this, StepsHistoryActivity.class);
                startActivity(mIntent);
                finish();
                return true;
            case R.id.editGoal:
                    Intent intent = new Intent(this, EditStepsGoal.class);
                    startActivity(intent);
                    finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            final_loc = location;
            String localization = getCompleteAddressString(final_loc.getLatitude(), final_loc.getLongitude());
            PopUpClass popUpClass = new PopUpClass(localization);
            popUpClass.showPopupWindowLocalization(StepsCounterActivity.this.getWindow().getDecorView().getRootView());

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("Status Changed", String.valueOf(status));
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("Provider Enabled", provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("Provider Disabled", provider);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps_counter);
        ActivityCompat.requestPermissions(StepsCounterActivity.this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE}, 1);
        mCalendar = Calendar.getInstance();
        todayDate = mCalendar.get(Calendar.DAY_OF_MONTH) + "/" + mCalendar.get(Calendar.MONTH) + 1 + "/" + mCalendar.get(Calendar.YEAR);
        today = new DateStepsModel();
        getDataForList();
        for (DateStepsModel model : mStepCountList) {
            if (model.mDate.equals(todayDate)) {
                today = model;
                dailyGoal = today.dailyGoal;
            }
        }

        Intent intent = getIntent();
        if (intent.getIntExtra("new_steps_goal", 1) != 1) {
            dailyGoal = intent.getIntExtra("new_steps_goal", 1);
            mStepsDBHelper.createGoalEntry(dailyGoal);
        }
        localizationButton = findViewById(R.id.localization_button);
        final LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        final Looper looper = null;
        criteria = new Criteria();
        localizationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                locationManager.requestSingleUpdate(criteria, locationListener, looper);
            }
        });


        getCompleteAddressString(latitude, longitude);
        progressBar = findViewById(R.id.progressBar);
        dailyGoalTextView = findViewById(R.id.dailyGoal);
        String s = getResources().getString(R.string.Goal);
        dailyGoalTextView.setText(s + dailyGoal);
        Intent stepsIntent = new Intent(getApplicationContext(), StepsService.class);
        startService(stepsIntent);
        stepsCounter = findViewById(R.id.stepsCounter);
        stepsCounter.setText(String.valueOf(today.mStepCount));
        progress = (Float.valueOf(today.mStepCount) / Float.valueOf(dailyGoal)) * 100;
        progressBar.setProgress(Math.round(progress));
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(StepsService.COPA_MESSAGE);
                stepsCounter.setText(s);
                progress = (Float.valueOf(s) / Float.valueOf(dailyGoal)) * 100;
                progressBar.setProgress(Math.round(progress));
                if (Integer.valueOf(s) == dailyGoal) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isFinishing()){
                                new AlertDialog.Builder(StepsCounterActivity.this)
                                        .setTitle(R.string.GoalAchived)
                                        .setMessage(R.string.SetGoal)
                                        .setCancelable(false)
                                        .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        })
                                        .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent(StepsCounterActivity.this, EditStepsGoal.class);
                                                startActivity(intent);
                                            }
                                        }).show();
                            }
                        }
                    });
                }

            }
        };
    }


    public void getDataForList()
    {
        mStepsDBHelper = new StepsDBHelper(this);
        mStepCountList = mStepsDBHelper.readStepsEntries();

    }
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
               strAdd = addresses.get(0).getAddressLine(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }




    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(StepsService.COPA_RESULT)
        );
    }


}
