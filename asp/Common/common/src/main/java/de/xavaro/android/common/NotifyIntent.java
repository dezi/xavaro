package de.xavaro.android.common;

import android.support.annotation.Nullable;

public class NotifyIntent
{
    public static final int INFOONLY = 1;
    public static final int REMINDER = 2;
    public static final int WARNING = 3;

    public boolean actionDue;
    public boolean speakDat;
    public long actionDueTime;
    public int importance;

    public String followText;
    public String declineText;
    public String title;
    public String summary;

    public interface NotifiyService
    {
        @Nullable
        NotifyIntent onGetNotifiyIntent();
    }
}
