package com.inhalertracker.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Keeps the reminder plan in SharedPreferences (so it survives reboots)
 * and mirrors it into AlarmManager as one daily alarm per inhaler+time.
 */
public final class ReminderScheduler {

    private static final String PREFS = "reminder_store";
    private static final String KEY = "payload";
    private static final String ACTION = "com.inhalertracker.app.REMINDER";

    private ReminderScheduler() {
    }

    /** Replace the whole plan: cancel the old alarms, store, schedule the new ones. */
    public static void applyFromJson(Context ctx, String json) {
        cancelAllFromStore(ctx);
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY, json == null ? "[]" : json).apply();
        scheduleFromStore(ctx);
    }

    /** Re-arm every stored alarm (used after boot / time changes). */
    public static void scheduleFromStore(Context ctx) {
        forEachStored(ctx, false);
    }

    private static void cancelAllFromStore(Context ctx) {
        forEachStored(ctx, true);
    }

    private static void forEachStored(Context ctx, boolean cancel) {
        String json = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String id = o.optString("id", "");
                String label = o.optString("label", "Inhaler");
                int puffs = o.optInt("puffs", 1);
                JSONArray times = o.optJSONArray("times");
                if (id.length() == 0 || times == null) continue;
                for (int j = 0; j < times.length(); j++) {
                    String time = times.getString(j);
                    if (cancel) cancelOne(ctx, id, time);
                    else scheduleOne(ctx, id, label, puffs, time);
                }
            }
        } catch (Exception ignored) {
        }
    }

    /** Schedule the next occurrence of one HH:mm reminder (today if still ahead, else tomorrow). */
    public static void scheduleOne(Context ctx, String id, String label, int puffs, String time) {
        long at = nextTrigger(time);
        if (at <= 0) return;
        PendingIntent pi = pending(ctx, id, label, puffs, time);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        try {
            if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                // Exact alarms not granted on this device: fall back to a 10-minute window.
                am.setWindow(AlarmManager.RTC_WAKEUP, at, 10 * 60 * 1000L, pi);
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            }
        } catch (SecurityException e) {
            am.setWindow(AlarmManager.RTC_WAKEUP, at, 10 * 60 * 1000L, pi);
        }
    }

    private static void cancelOne(Context ctx, String id, String time) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        am.cancel(pending(ctx, id, "", 1, time));
    }

    private static PendingIntent pending(Context ctx, String id, String label, int puffs, String time) {
        Intent it = new Intent(ctx, AlarmReceiver.class);
        it.setAction(ACTION);
        it.putExtra("id", id);
        it.putExtra("label", label);
        it.putExtra("puffs", puffs);
        it.putExtra("time", time);
        int requestCode = (id + "|" + time).hashCode();
        return PendingIntent.getBroadcast(ctx, requestCode, it,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static long nextTrigger(String hhmm) {
        try {
            String[] p = hhmm.split(":");
            int h = Integer.parseInt(p[0].trim());
            int m = Integer.parseInt(p[1].trim());
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, h);
            c.set(Calendar.MINUTE, m);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            if (c.getTimeInMillis() <= System.currentTimeMillis()) {
                c.add(Calendar.DAY_OF_YEAR, 1);
            }
            return c.getTimeInMillis();
        } catch (Exception e) {
            return -1;
        }
    }
}
