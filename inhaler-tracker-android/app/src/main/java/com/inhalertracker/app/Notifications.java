package com.inhalertracker.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public final class Notifications {

    public static final String CHANNEL = "inhaler_reminders";

    private Notifications() {
    }

    public static void ensureChannel(Context ctx) {
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm == null) return;
        NotificationChannel ch = new NotificationChannel(
                CHANNEL, "Inhaler reminders", NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Scheduled dose reminders");
        nm.createNotificationChannel(ch);
    }

    public static void show(Context ctx, int notificationId, String label, int puffs) {
        ensureChannel(ctx);
        Intent open = new Intent(ctx, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(ctx, notificationId, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title = ((label == null || label.length() == 0) ? "Inhaler" : label) + " reminder";
        String text = puffs + (puffs == 1 ? " puff" : " puffs") + " due - open to log it";

        Notification n = new Notification.Builder(ctx, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_inhaler)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm == null) return;
        try {
            nm.notify(notificationId, n);
        } catch (SecurityException ignored) {
            // Notification permission declined on Android 13+.
        }
    }
}
