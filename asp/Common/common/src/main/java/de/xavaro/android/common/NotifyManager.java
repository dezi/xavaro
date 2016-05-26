package de.xavaro.android.common;

import java.util.ArrayList;

public class NotifyManager
{
    private static final String LOGTAG = NotifyManager.class.getSimpleName();

    private static final ArrayList<NotifyIntent> pendingIntents = new ArrayList<>();

    public static void addNotification(NotifyIntent intent)
    {
        if (intent == null) return;

        if (intent.key != null)
        {
            for (int inx = 0; inx < pendingIntents.size(); inx++)
            {
                NotifyIntent old = pendingIntents.get(inx);

                if (Simple.equals(intent.key, old.key)) pendingIntents.remove(inx--);
            }
        }

        pendingIntents.add(intent);
    }

    public static void removeNotification(NotifyIntent intent)
    {
        if (intent == null) return;

        if (intent.key != null)
        {
            for (int inx = 0; inx < pendingIntents.size(); inx++)
            {
                NotifyIntent old = pendingIntents.get(inx);

                if (Simple.equals(intent.key, old.key)) pendingIntents.remove(inx--);
            }
        }

        if (pendingIntents.contains(intent)) pendingIntents.remove(intent);
    }

    public static ArrayList<NotifyIntent> getPendingIntents()
    {
        return pendingIntents;
    }
}
