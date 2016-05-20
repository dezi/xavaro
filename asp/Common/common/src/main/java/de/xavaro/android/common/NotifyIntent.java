package de.xavaro.android.common;

import android.support.annotation.Nullable;

public class NotifyIntent
{
    public static final int INFOONLY = 1;
    public static final int REMINDER = 2;
    public static final int WARNING = 3;
    public static final int ASSISTANCE = 4;

    public String key;
    public String title;
    public String summary;
    public String iconres;
    public String followText;
    public String declineText;

    public int importance;

    public String spokenTimePref;
    public int spokenRepeatMinutes;

    public Runnable followRunner;
    public Runnable declineRunner;

    public interface NotifiyService
    {
        @Nullable
        NotifyIntent onGetNotifiyIntent();
    }
}
