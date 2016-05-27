package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.widget.FrameLayout;

public class NotifyIntent
{
    public static final int INFOONLY = 1;
    public static final int REMINDER = 2;
    public static final int WARNING = 3;
    public static final int ASSISTANCE = 4;
    public static final int URGENT = 5;

    public String dst = Simple.nowAsISO();

    public String key;
    public String title;
    public String summary;
    public String followText;
    public String declineText;

    public int importance;

    public boolean speakOnce;
    public String spokenTimePref;
    public int spokenRepeatMinutes;

    public Runnable followRunner;
    public Runnable declineRunner;

    public int iconres;
    public String iconpath;
    public boolean iconcircle;
    public FrameLayout iconFrame;
    public NotifyChecker checkCondition;

    public interface NotifyService
    {
        @Nullable
        NotifyIntent onGetNotifiyIntent();
    }
}
