package de.xavaro.android.common;

import android.support.annotation.Nullable;

public class NotifyIntent
{
    public boolean actionDue;
    public long actionDueTime;

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
