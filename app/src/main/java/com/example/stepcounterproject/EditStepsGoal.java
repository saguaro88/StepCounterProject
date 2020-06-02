package com.example.stepcounterproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditStepsGoal extends AppCompatActivity {
    private Button submit_steps_goal;
    private EditText steps_goal_input;
    private int new_steps_goal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_steps_goal);
        setTitle(R.string.EditStepGoalTitle);
        submit_steps_goal = findViewById(R.id.submit_steps_goal);
        steps_goal_input = findViewById(R.id.steps_goal);
        submit_steps_goal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new_steps_goal = Integer.valueOf(steps_goal_input.getText().toString());
                Intent myIntent = new Intent(EditStepsGoal.this, StepsCounterActivity.class);
                myIntent.putExtra("new_steps_goal", new_steps_goal); //Optional parameters
                EditStepsGoal.this.startActivity(myIntent);
                finish();
            }
        });
    }
}
