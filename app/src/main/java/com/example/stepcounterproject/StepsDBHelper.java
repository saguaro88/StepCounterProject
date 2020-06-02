package com.example.stepcounterproject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

public class StepsDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "StepsDatabase";
    private static final String TABLE_STEPS_SUMMARY = "StepsSummary";
    private static final String ID = "id";
    private static final String STEPS_COUNT = "stepscount";
    private static final String CREATION_DATE = "creationdate";
    private static final String DAILY_GOAL = "dailygoal";

    private static final String CREATE_TABLE_STEPS_SUMMARY = "CREATE TABLE "
            + TABLE_STEPS_SUMMARY + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            CREATION_DATE + " TEXT,"+ STEPS_COUNT + " INTEGER,"+ DAILY_GOAL +" INTEGER"+")";
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_STEPS_SUMMARY);
    }

    public boolean createGoalEntry(int dailygoal) {
        boolean isDateAlreadyPresent = false;
        boolean createSuccessful = false;
        Calendar mCalendar = Calendar.getInstance();
        String todayDate = mCalendar.get(Calendar.DAY_OF_MONTH) + "/" + mCalendar.get(Calendar.MONTH) + 1 + "/" + mCalendar.get(Calendar.YEAR);
        String selectQuery = "SELECT " + DAILY_GOAL + " FROM " + TABLE_STEPS_SUMMARY + " WHERE " + CREATION_DATE + " = '" + todayDate + "'";
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    isDateAlreadyPresent = true;
                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CREATION_DATE, todayDate);
            if (isDateAlreadyPresent) {
                values.put(DAILY_GOAL, dailygoal);
                int row = db.update(TABLE_STEPS_SUMMARY, values, CREATION_DATE + " = '" + todayDate + "'", null);
                if (row == 1) {
                    createSuccessful = true;
                }
                db.close();
            } else {
                values.put(DAILY_GOAL, 1000);
                long row = db.insert(TABLE_STEPS_SUMMARY, null, values);
                if (row != -1) {
                    createSuccessful = true;
                }
                db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createSuccessful;
    }
    public boolean createStepsEntry() {
        boolean isDateAlreadyPresent = false;
        boolean createSuccessful = false;
        int currentDateStepCounts = 0;
        Calendar mCalendar = Calendar.getInstance();
        String todayDate = mCalendar.get(Calendar.DAY_OF_MONTH) + "/" + mCalendar.get(Calendar.MONTH) + 1 + "/" + mCalendar.get(Calendar.YEAR);
        String selectQuery = "SELECT " + STEPS_COUNT + " FROM " + TABLE_STEPS_SUMMARY + " WHERE " + CREATION_DATE + " = '" + todayDate + "'";
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    isDateAlreadyPresent = true;
                    currentDateStepCounts = c.getInt((c.getColumnIndex(STEPS_COUNT)));
                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CREATION_DATE, todayDate);
            if (isDateAlreadyPresent) {
                values.put(STEPS_COUNT, ++currentDateStepCounts);
                int row = db.update(TABLE_STEPS_SUMMARY, values, CREATION_DATE + " = '" + todayDate + "'", null);
                if (row == 1) {
                    createSuccessful = true;
                }
                db.close();
            } else {
                values.put(STEPS_COUNT, 1);
                values.put(DAILY_GOAL, 1000);
                long row = db.insert(TABLE_STEPS_SUMMARY, null, values);
                if (row != -1) {
                    createSuccessful = true;
                }
                db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createSuccessful;
    }
    public ArrayList<DateStepsModel> readStepsEntries()
    {
        ArrayList<DateStepsModel> mStepCountList = new ArrayList<DateStepsModel>();
        String selectQuery = "SELECT * FROM " + TABLE_STEPS_SUMMARY;
        try {

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    DateStepsModel mDateStepsModel = new DateStepsModel();
                    mDateStepsModel.mDate = c.getString((c.getColumnIndex(CREATION_DATE)));
                    mDateStepsModel.mStepCount = c.getInt((c.getColumnIndex(STEPS_COUNT)));
                    mDateStepsModel.dailyGoal=c.getInt((c.getColumnIndex(DAILY_GOAL)));
                    mStepCountList.add(mDateStepsModel);
                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mStepCountList;
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STEPS_SUMMARY);
        this.onCreate(db);
    }

    public StepsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
