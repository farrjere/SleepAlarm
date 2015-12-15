package org.farrellcrafts.sleepalarm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

/**
 * Created by Jeremy on 12/14/2015.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Time to wake up",
                Toast.LENGTH_LONG).show();
        SleepAlarm.instance().handleAlarm();
    }
}
