package com.example.stepcounterproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;


public class StepsHistoryActivity extends AppCompatActivity{

    StepsDBHelper mStepsDBHelper;
    ListView mSensorListView;
    ListAdapter mListAdapter;
    ArrayList<DateStepsModel> mStepCountList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps_history);
        setTitle(R.string.History);
    mSensorListView = findViewById(R.id.steps_list);
        getDataForList();
        mListAdapter = new ListAdapter(mStepCountList, this);
        mSensorListView.setAdapter(mListAdapter);
        Intent stepsIntent = new Intent(getApplicationContext(), StepsService.class);
        startService(stepsIntent);
    }

    public void getDataForList()
    {
        mStepsDBHelper = new StepsDBHelper(this);
        mStepCountList = mStepsDBHelper.readStepsEntries();
    }

}
