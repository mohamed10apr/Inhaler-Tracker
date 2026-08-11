package com.inhalertracker.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Fires at a reminder time: shows the notification, then re-arms itself for tomorrow. */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String id = intent.getStringExtra("id");
        String label = intent.getStringExtra("label");
        int puffs = intent.getIntExtra("puffs", 1);
        String time = intent.getStringExtra("time");
        if (id == null || time == null) return;

        Notifications.show(ctx, (id + "|" + time).hashCode(), label, puffs);
        ReminderScheduler.scheduleOne(ctx, id, label, puffs, time);
    }
}
