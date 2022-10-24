package com.example.remoteac;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    static int temperature, fan, mode, tnow_units, tnow_tens, tnow_hours, checksum, tsleep_hours, tsleep_tens, twake_hours, twake_tens;
    static boolean power, swing, sleep, wake, tnow_pm, tsleep_pm, twake_pm;

    private static final int FAN_AUTO = 0, FAN_1 = 1, FAN_2 = 2, FAN_3 = 3;
    private static final int MODE_DRY = 0, MODE_HOT = 1, MODE_COLD = 2, MODE_FAN = 3;
    private static final int TEMP_16 = 0;

    static SharedPreferences sharedPref;
    static SharedPreferences.Editor prefEditor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Read message from firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://remoteac-e5c64-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference myPower = database.getReference("Power_state");
        DatabaseReference myTemp = database.getReference("Temperature_state");
        DatabaseReference myMode = database.getReference("Mode_state");
        DatabaseReference myFan = database.getReference("Fan_state");
        DatabaseReference mySwing = database.getReference("Swing_state");
        DatabaseReference myCommand = database.getReference("command");
        DatabaseReference myTempValue = database.getReference("temp");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        prefEditor = sharedPref.edit();

        restore_data();
        update_view();

        findViewById(R.id.powerButton).setOnClickListener(view -> {
            power = !power;
            update_view();
            send();
            prefEditor.putBoolean("Power", power);
            prefEditor.apply();


            //Firebase send
            if(power){
                myPower.setValue("Online");
                myCommand.setValue(1);
            }
            if(!power){
                myPower.setValue("Offline");
                myCommand.setValue(1);
            }
        });

        findViewById(R.id.tempUpButton).setOnClickListener(view -> {
            temperature = Math.min(temperature + 1, TEMP_16 + 14);
            update_view();
            send();
            prefEditor.putInt("Temperature", temperature);
            prefEditor.apply();

            //Firebase send
            myTemp.setValue("Temperature Up");
            myCommand.setValue(2);
            myTempValue.setValue(temperature + (16 - TEMP_16));
        });

        findViewById(R.id.tempDownButton).setOnClickListener(view -> {
            temperature = Math.max(temperature - 1, TEMP_16);
            update_view();
            send();
            prefEditor.putInt("Temperature", temperature);
            prefEditor.apply();


            //Firebase send
            myTemp.setValue("Temperature Down");
            myCommand.setValue(2);
            myTempValue.setValue(temperature + (16 - TEMP_16));

        });

        findViewById(R.id.swingButton).setOnClickListener(view -> {
            swing = !swing;
            update_view();
            send();
            prefEditor.putBoolean("Swing", swing);
            prefEditor.apply();


            //Firebase send
            if(swing){
                mySwing.setValue("Online");
                myCommand.setValue(5);
            }
            if(!swing){
                mySwing.setValue("Offline");
                myCommand.setValue(5);
            }
        });

        findViewById(R.id.modeButton).setOnClickListener(view -> {
            mode = (mode + 1) % 4;
            if (mode == MODE_FAN && fan == FAN_AUTO) {
                fan = (fan + 1) % 4;
            }
            update_view();
            send();
            prefEditor.putInt("Mode", mode);
            prefEditor.apply();


            //Firebase send
            if (mode == 0){
                myMode.setValue("Dry");
                myCommand.setValue(4);
            }
            if (mode == 1){
                myMode.setValue("Hot");
                myCommand.setValue(4);
            }
            if (mode == 2){
                myMode.setValue("Cold");
                myCommand.setValue(4);
            }
            if (mode == 3){
                myMode.setValue("Fan");
                myCommand.setValue(4);
            }
        });

        findViewById(R.id.fanButton).setOnClickListener(view -> {
            fan = (fan + 1) % 4;
            if (mode == MODE_FAN && fan == FAN_AUTO)
                fan = (fan + 1) % 4;

            update_view();
            send();
            prefEditor.putInt("Fan", fan);
            prefEditor.apply();


            //Firebase send
            if (fan == 0){
                myFan.setValue("Auto");
                myCommand.setValue(3);
            }
            if (fan == 1){
                myFan.setValue("Low");
                myCommand.setValue(3);
            }
            if (fan == 2){
                myFan.setValue("Medium");
                myCommand.setValue(3);
            }
            if (fan == 3){
                myFan.setValue("High");
                myCommand.setValue(3);
            }
        });

        findViewById(R.id.sleepButton).setOnClickListener(view -> {
            if (sleep) {
                sleep = false;
                tsleep_hours = 10;
                tsleep_tens = 0;
                tsleep_pm = true;
                update_view();
                send();
                prefEditor.putBoolean("Sleep", sleep);
                prefEditor.putInt("SleepHour", tsleep_hours);
                prefEditor.putInt("SleepTens", tsleep_tens);
                prefEditor.putBoolean("SleepPM", tsleep_pm);
                prefEditor.apply();
            }
            else
            {
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);
                TimePickerDialog timepicker = new TimePickerDialog(MainActivity.this, (timePicker, hourofday, selectedminute) -> {
                    sleep = true;
                    tsleep_hours = hourofday % 12;
                    tsleep_pm = hourofday >= 12;
                    tsleep_tens = selectedminute / 10;
                    update_view();
                    send();
                    prefEditor.putBoolean("Sleep", sleep);
                    prefEditor.putInt("SleepHour", tsleep_hours);
                    prefEditor.putInt("SleepTens", tsleep_tens);
                    prefEditor.putBoolean("SleepPM", tsleep_pm);
                    prefEditor.apply();
                }, hour, minute, false);
                timepicker.setTitle("Select Sleep Time");
                timepicker.show();
            }
        });

        findViewById(R.id.wakeButton).setOnClickListener(view -> {
            if (wake) {
                wake = false;
                twake_hours = 6;
                twake_tens = 0;
                twake_pm = false;
                update_view();
                send();
                prefEditor.putBoolean("Wake", wake);
                prefEditor.putInt("WakeHour", twake_hours);
                prefEditor.putInt("WakeTens", twake_tens);
                prefEditor.putBoolean("WakePM", twake_pm);
                prefEditor.apply();
            }
            else
            {
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);
                TimePickerDialog timepicker = new TimePickerDialog(MainActivity.this, (timePicker, hourofday, selectedminute) -> {
                    wake = true;
                    twake_hours = hourofday % 12;
                    twake_pm = hourofday >= 12;
                    twake_tens = selectedminute / 10;
                    update_view();
                    send();
                    prefEditor.putBoolean("Wake", wake);
                    prefEditor.putInt("WakeHour", twake_hours);
                    prefEditor.putInt("WakeTens", twake_tens);
                    prefEditor.putBoolean("WakePM", twake_pm);
                    prefEditor.apply();
                }, hour, minute, false);
                timepicker.setTitle("Select Wake up Time");
                timepicker.show();
            }
        });

        findViewById(R.id.sendButton).setOnClickListener(view -> send());

    }

    @SuppressLint("SetTextI18n")
    //update UI di smartphone
    void update_view() {

        ((TextView)findViewById(R.id.powerText)).setText(power? "On": "Off");
        ((TextView)findViewById(R.id.tempratureText)).setText(Integer.toString(temperature + (16 - TEMP_16)));

        String state = "Cold";
        switch (mode)
        {
            case MODE_COLD:
                state = "Cold";
                break;
            case MODE_HOT:
                state = "Hot";
                break;
            case MODE_DRY:
                state = "Dry";
                break;
            case MODE_FAN:
                state = "Fan";
                break;
        }

        ((TextView)findViewById(R.id.modeText)).setText(state);


        ((TextView)findViewById(R.id.swinText)).setText(swing? "On": "Off");

        switch (fan)
        {
            case FAN_AUTO:
                findViewById(R.id.fanImage).setVisibility(View.INVISIBLE);
                findViewById(R.id.autoText).setVisibility(View.VISIBLE);
                break;
            case FAN_1:
                findViewById(R.id.fanImage).setVisibility(View.VISIBLE);
                findViewById(R.id.autoText).setVisibility(View.INVISIBLE);
                ((ImageView)findViewById(R.id.fanImage)).setImageResource(R.drawable.fan1);
                break;
            case FAN_2:
                findViewById(R.id.fanImage).setVisibility(View.VISIBLE);
                findViewById(R.id.autoText).setVisibility(View.INVISIBLE);
                ((ImageView)findViewById(R.id.fanImage)).setImageResource(R.drawable.fan2);
                break;
            case FAN_3:
                findViewById(R.id.fanImage).setVisibility(View.VISIBLE);
                findViewById(R.id.autoText).setVisibility(View.INVISIBLE);
                ((ImageView)findViewById(R.id.fanImage)).setImageResource(R.drawable.fan3);
                break;
        }

        if (sleep) {
            TextView sleepText = findViewById(R.id.sleepText);
            sleepText.setVisibility(View.VISIBLE);
            sleepText.setText("Sleep at " + tsleep_hours + ":" + tsleep_tens * 10 + " " + (tsleep_pm? "PM": "AM"));
        }
        else {
            findViewById(R.id.sleepText).setVisibility(View.INVISIBLE);
        }

        if (wake) {
            TextView wakeText = findViewById(R.id.wakeText);
            wakeText.setVisibility(View.VISIBLE);
            wakeText.setText("Wake up at " + twake_hours + ":" + twake_tens * 10 + " " + (twake_pm? "PM": "AM"));
        }
        else {
            findViewById(R.id.wakeText).setVisibility(View.INVISIBLE);
        }
    }

    void restore_data() {
        power = sharedPref.getBoolean("Power", false);
        temperature = sharedPref.getInt("Temperature", TEMP_16 + 8);
        fan = sharedPref.getInt("Fan", FAN_1);
        mode = sharedPref.getInt("Mode", MODE_COLD);
        swing = sharedPref.getBoolean("Swing", false);
        sleep = sharedPref.getBoolean("Sleep", false);
        wake = sharedPref.getBoolean("Wake", false);
        tsleep_hours = sharedPref.getInt("SleepHour", 10);
        tsleep_tens = sharedPref.getInt("SleepTens", 0);
        tsleep_pm = sharedPref.getBoolean("SleepPM", true);
        twake_hours = sharedPref.getInt("WakeHour", 6);
        twake_tens = sharedPref.getInt("WakeTens", 0);
        twake_pm = sharedPref.getBoolean("WakePM", false);
    }

    void send() {
        set_time();
        set_checksum();
    }

    static void set_checksum() {
        checksum = (tsleep_hours & 0b1111) + (tsleep_pm? 8: 0) + (tsleep_tens & 0b111)
                + (twake_hours & 0b1111) + (twake_pm? 8: 0) + (twake_tens & 0b111)
                + (tnow_hours & 0b1111) + (tnow_pm? 8: 0) + (tnow_tens & 0b111)
                + (wake? 4: 0) + (sleep? 2: 0) + (power? 1: 0) + (tnow_units & 0b1111)
                + (swing? 2: 0) + (temperature & 0b1111) + (fan & 0b11) * 4 + (mode & 0b11);
        checksum = checksum % 16;
    }

    static void set_time() {
        Calendar currentTime = Calendar.getInstance();
        tnow_hours = currentTime.get(Calendar.HOUR);
        tnow_pm = currentTime.get(Calendar.AM_PM) == Calendar.PM;
        tnow_tens = currentTime.get(Calendar.MINUTE) / 10;
        tnow_units = currentTime.get(Calendar.MINUTE) % 10;
    }


}
