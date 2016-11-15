package ua.zt.mezon.e52;

import android.net.Uri;

/**
 * Created by MezM on 07.10.2016.
 */
public final class Constants {
    private static final String ALARM_PATH = "/alarm-phone";
    private static final String HOURS_KEY = "hours";
    private static final String MINUTES_KEY = "minutes";
    public static final String DATA_ITEM_PATH = "/timer";
    public static final Uri URI_PATTERN_DATA_ITEMS =
            Uri.fromParts("wear", DATA_ITEM_PATH, null);

    public static final int NOTIFICATION_TIMER_COUNTDOWN = 1;
    public static final int NOTIFICATION_TIMER_EXPIRED = 2;
    public static final int ALARM_TRIGGERED = 3;
    public static final String ACTION_SHOW_ALARM
            = "com.android.example.clockwork.timer.ACTION_SHOW";
    public static final String ACTION_DELETE_ALARM
            = "com.android.example.clockwork.timer.ACTION_DELETE";
    public static final String ACTION_RESTART_ALARM
            = "com.android.example.clockwork.timer.ACTION_RESTART";

    private Constants() {
    }
}
