package com.example.iapps.timesapp;

/**
 * Taken from http://stackoverflow.com/questions/6864712/android-alarmmanager-not-waking-phone-up
 * Handles waking up the phone when alarm goes off
 */

        import android.content.Context;
        import android.os.PowerManager;


public abstract class WakeLocker {
    private static PowerManager.WakeLock wakeLock;

    public static void acquire(Context ctx) {
        if (wakeLock != null) wakeLock.release();

        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"timesAPP");
        wakeLock.acquire();
    }

    public static void release() {
        if (wakeLock != null) wakeLock.release(); wakeLock = null;
    }
}
