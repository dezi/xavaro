package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import org.json.JSONObject;

import java.util.Iterator;

import de.xavaro.android.common.ActivityOldManager;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class LaunchFrameTodayOld extends LaunchFrame
        implements ActivityOldManager.ActivityMessageCallback
{
    private static final String LOGTAG = LaunchFrameTodayOld.class.getSimpleName();

    public LaunchFrameTodayOld(Context context, LaunchItem parent)
    {
        super(context, parent);

        myInit();
    }

    private FrameLayout topscreen;
    private ChatDialog scrollview;
    private boolean isinitialized;

    private void myInit()
    {
        topscreen = new FrameLayout(getContext());
        topscreen.setBackgroundColor(GlobalConfigs.LaunchPageBackgroundColor);
        addView(topscreen);

        scrollview = new ChatDialog(getContext());
        scrollview.setIsToday(true);

        topscreen.addView(scrollview);

        ActivityOldManager.subscribe(this);
    }

    public void onProtocollMessages(JSONObject protocoll)
    {
        if (isinitialized) return;
        isinitialized = true;

        Log.d(LOGTAG, "onProtocollMessages: " + protocoll.toString());

        JSONObject outgoing = Json.getObject(protocoll, "outgoing");
        JSONObject incoming = Json.getObject(protocoll, "incoming");

        Iterator<String> outgoingIter = (outgoing != null) ? outgoing.keys() : null;
        Iterator<String> incomingIter = (incoming != null) ? incoming.keys() : null;

        String outgoingUuid = null;
        String incomingUuid = null;

        while (true)
        {
            if ((outgoingUuid == null) && (outgoingIter != null) && outgoingIter.hasNext())
            {
                outgoingUuid = outgoingIter.next();
            }

            if ((incomingUuid == null) && (incomingIter != null) && incomingIter.hasNext())
            {
                incomingUuid = incomingIter.next();
            }

            if ((outgoingUuid == null) && (incomingUuid == null)) break;

            boolean nextOutgoing = true;
            boolean nextIncoming = true;

            if ((outgoingUuid != null) && (incomingUuid != null))
            {
                String outgoingDate = Json.getString(Json.getObject(outgoing, outgoingUuid), "date");
                String incomingDate = Json.getString(Json.getObject(incoming, incomingUuid), "date");

                if (Simple.compareTo(outgoingDate, incomingDate) > 0)
                {
                    nextOutgoing = false;
                }
                else
                {
                    nextIncoming = false;
                }
            }

            if (nextOutgoing && (outgoingUuid != null))
            {
                scrollview.createOutgoingMessage(Json.getObject(outgoing,outgoingUuid));
                outgoingUuid = null;
            }

            if (nextIncoming && (incomingUuid != null))
            {
                scrollview.createIncomingMessage(Json.getObject(incoming,incomingUuid));
                incomingUuid = null;
            }
        }

        post(new Runnable()
        {
            @Override
            public void run()
            {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    public void onIncomingMessage(JSONObject message)
    {
        final JSONObject pmessage = message;

        post(new Runnable()
        {
            @Override
            public void run()
            {
                scrollview.createIncomingMessage(pmessage);
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    public void onOutgoingMessage(JSONObject message)
    {
        final JSONObject pmessage = message;

        post(new Runnable()
        {
            @Override
            public void run()
            {
                scrollview.createOutgoingMessage(pmessage);
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}