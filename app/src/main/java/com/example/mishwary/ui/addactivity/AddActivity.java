package com.example.mishwary.ui.addactivity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mishwary.Models.Trip;
import com.example.mishwary.R;

import java.util.Calendar;

public class AddActivity extends AppCompatActivity implements View.OnClickListener,AddContract.AddView {
    Button btnDatePicker, btnTimePicker, btnAdd ;
    TextView txtDate, txtTime;
    EditText titleTxt,startTxt,endTxt;
    final Calendar c = Calendar.getInstance();
    private int mYear, mMonth, mDay, mHour, mMinute;
    private AddPresenter addPresenter;
    private String id;
    private Spinner repeatSpinner,descSpinner;
    private ArrayAdapter<CharSequence> repeatAdapter,descAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        btnDatePicker=(Button) findViewById(R.id.btn_date);
        btnTimePicker=(Button) findViewById(R.id.btn_time);
        btnAdd = (Button) findViewById(R.id.btn_add);
        txtDate=(TextView)findViewById(R.id.in_date);
        txtTime=(TextView)findViewById(R.id.in_time);
        btnDatePicker.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);
        titleTxt = findViewById(R.id.trip_title);
        startTxt = findViewById(R.id.trip_start_point);
        endTxt = findViewById(R.id.trip_end_point);
        repeatSpinner = findViewById(R.id.spinner_repeat);
        descSpinner = findViewById(R.id.spinner_desc);
        btnAdd.setOnClickListener(this);
        repeatAdapter = ArrayAdapter.createFromResource(this,R.array.repeatSpinner,R.layout.support_simple_spinner_dropdown_item);
        repeatAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        repeatSpinner.setAdapter(repeatAdapter);
        descAdapter = ArrayAdapter.createFromResource(this,R.array.descSpinner,R.layout.support_simple_spinner_dropdown_item);
        descAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        descSpinner.setAdapter(descAdapter);
        addPresenter = new AddPresenter(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnDatePicker) {

            // Get Current Date
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            c.set(Calendar.YEAR, year);
                            c.set(Calendar.MONTH, monthOfYear);
                            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            txtDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
        if (v == btnTimePicker) {

            // Get Current Time
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            c.set(Calendar.MINUTE, minute);
                            c.set(Calendar.SECOND, 0);
                            txtTime.setText(hourOfDay + ":" + minute);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }

        if(v == btnAdd) {
           if(validateInputs()) {
                addTriptoFireBase();
            }
            startAlarm(c);
        }
    }

    private boolean validateInputs() {
        boolean flag = true;
        if(titleTxt.getText().toString().trim().isEmpty()){
            flag = false;
            titleTxt.setError("Enter Title");
            titleTxt.requestFocus();
        }
        if(startTxt.getText().toString().trim().isEmpty()){
            flag = false;
            startTxt.setError("Enter Start Point");
            startTxt.requestFocus();
        }
        if(endTxt.getText().toString().trim().isEmpty()){
            flag = false;
            endTxt.setError("Enter Destination");
            endTxt.requestFocus();
        }
        if(txtDate.getText().toString().trim().isEmpty()){
            flag = false;
            Toast.makeText(this,"Please Select Date",Toast.LENGTH_LONG).show();
        }
        if(txtTime.getText().toString().trim().isEmpty()){
            flag = false;
            Toast.makeText(this,"Please Select Time",Toast.LENGTH_LONG).show();
        }
        return flag;
    }

    private void addTriptoFireBase() {
        Trip trip = new Trip();
        trip.setUserId(id);
        trip.setTripName(titleTxt.getText().toString());
        trip.setStartPoint(startTxt.getText().toString());
        trip.setDestination(endTxt.getText().toString());
        trip.setTime(txtTime.getText().toString());
        trip.setDate(txtDate.getText().toString());
        trip.setRepeat(repeatSpinner.getSelectedItem().toString());
        trip.setDescription(descSpinner.getSelectedItem().toString());
        addPresenter.addTrip(trip);
    }

    private void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }

        alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }

    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        alarmManager.cancel(pendingIntent);
        Toast.makeText(this, "Alarm canceled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void gotoHome() {
        finish();
    }
}
