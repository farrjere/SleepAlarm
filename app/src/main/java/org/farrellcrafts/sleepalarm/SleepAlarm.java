package org.farrellcrafts.sleepalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Calendar;

public class SleepAlarm extends AppCompatActivity {
    private long milliInCycle = 1000 * 60 * 90L;
    private long milliToSleep = 1000 * 60 * 15L;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private static SleepAlarm inst;
    private Ringtone ringtone;
    private boolean vibrate;
    public static SleepAlarm instance(){
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        setContentView(R.layout.activity_sleep_alarm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setPreferences();
        setSupportActionBar(toolbar);
    }

    private void setRingtone() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        String strRingtonePreference = sharedPref.getString("notifications_new_message_ringtone", alarmUri.toString());
        Uri uri = Uri.parse(strRingtonePreference);
        ringtone = RingtoneManager.getRingtone(this, uri);
    }

    @Override
    public void onResume(){
        super.onResume();
        setPreferences();
    }

    private void setPreferences(){
        setRingtone();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int cycle = Integer.parseInt(sharedPref.getString("general_cycle", "90"));
        int sleep = Integer.parseInt(sharedPref.getString("general_sleep", "15"));
        milliInCycle = cycle * 60 * 1000;
        milliToSleep = sleep * 60 * 1000;
        vibrate = sharedPref.getBoolean("notifications_new_message_vibrate", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sleep_alarm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void setAlarm(View view){
        //Obtain user input and calculate wake time
        Calendar selected = getSelectedCalendar();
        long wakeTime = getWakeTime(selected);
        selected.setTimeInMillis(wakeTime);
        Log.i("sleepAlarm.setAlarm", selected.getTime().toString());
        //Make Cancel visible if it isn't
        Button cancel = (Button)findViewById(R.id.cancel);
        cancel.setVisibility(View.VISIBLE);
        Log.d("sleepAlarm.setAlarm", "Setting cancel button to visible");

        Intent intent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), 0, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, wakeTime, pendingIntent);
        Log.d("sleepAlarm.setAlarm", "Setting alarm");
    }

    public void cancelAlarm(View view){
        alarmManager.cancel(pendingIntent);
        Log.d("sleepAlarm.cancelAlarm", "Setting cancel button to invisible");
        hideCancel();
    }

    private void hideCancel() {
        Button cancel = (Button)findViewById(R.id.cancel);
        cancel.setVisibility(View.INVISIBLE);
    }

    @NonNull
    private Calendar getSelectedCalendar() {
        TimePicker picker = (TimePicker)findViewById(R.id.timePicker);
        Calendar selected = Calendar.getInstance();
        selected.set(Calendar.MINUTE, picker.getCurrentMinute());
        selected.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
        return selected;
    }

    private long getWakeTime(Calendar selected) {
        Calendar now = Calendar.getInstance();
        long selTime = selected.getTimeInMillis();
        long curTime = now.getTimeInMillis();
        long diff = Math.abs(selTime - curTime);
        long cycles = diff/ milliInCycle;
        return curTime + cycles * milliInCycle + milliToSleep;
    }

    public void handleAlarm(){
        hideCancel();
        if(vibrate) {
            Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if(vib.hasVibrator()) {
                vib.vibrate(2000);
            }
        }
        Button cancel = (Button)findViewById(R.id.dismiss);
        cancel.setVisibility(View.VISIBLE);
        ringtone.play();
    }

    public void dismiss(View view){
        ringtone.stop();
        Button cancel = (Button)findViewById(R.id.dismiss);
        cancel.setVisibility(View.INVISIBLE);
    }

}
